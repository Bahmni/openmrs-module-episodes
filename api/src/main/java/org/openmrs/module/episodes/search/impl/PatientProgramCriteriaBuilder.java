/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.openmrs.module.episodes.search.model.Condition;
import org.openmrs.module.episodes.search.model.ConditionOperator;
import org.openmrs.module.episodes.search.model.FieldComparator;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientProgramCriteriaBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void applyCondition(Criteria criteria, Condition condition) {
        Set<String> createdAliases = new HashSet<>();
        Criterion criterion = buildCriterion(criteria, condition, createdAliases);
        if (criterion != null) {
            criteria.add(criterion);
        }
    }

    private Criterion buildCriterion(Criteria criteria, Condition condition, Set<String> createdAliases) {
        if (condition.isLeaf()) {
            return buildLeafCriterion(criteria, condition, createdAliases);
        }

        List<Criterion> childCriteria = new ArrayList<>();
        for (Condition child : condition.getConditions()) {
            Criterion resolved = buildCriterion(criteria, child, createdAliases);
            if (resolved != null) {
                childCriteria.add(resolved);
            }
        }
        if (childCriteria.isEmpty()) {
            return null;
        }
        if (childCriteria.size() == 1) {
            return childCriteria.get(0);
        }

        Junction junction = (condition.getOperator() == ConditionOperator.OR)
                ? Restrictions.disjunction()
                : Restrictions.conjunction();
        for (Criterion c : childCriteria) {
            junction.add(c);
        }
        return junction;
    }

    private Criterion buildLeafCriterion(Criteria criteria, Condition leaf, Set<String> createdAliases) {
        String field = leaf.getField();
        FieldComparator comparator = leaf.getComparator();
        String rawValue = leaf.getValue();

        if (comparator == null) {
            throw new InvalidSearchCriteriaException(
                    "Missing comparator for field '" + field + "'. Supported: eq, gt, lt",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        switch (field) {
            // --- Episode of Care fields (use subqueries) ---
            case SearchFields.EpisodeOfCare.START_DATE:
                requireDateComparator(field, comparator);
                return episodeDateSubquery("e.dateStarted", comparator, parseDate(rawValue));

            case SearchFields.EpisodeOfCare.END_DATE:
                requireDateComparator(field, comparator);
                return episodeDateSubquery("e.dateEnded", comparator, parseDate(rawValue));

            case SearchFields.EpisodeOfCare.CARE_MANAGER:
                requireEqComparator(field, comparator);
                return episodeCareManagerSubquery(rawValue);

            // --- Program fields (direct on patient_program) ---
            case SearchFields.Program.UUID:
                requireEqComparator(field, comparator);
                return Restrictions.eq("pp.program.uuid", rawValue);

            case SearchFields.Program.TYPE:
                requireEqComparator(field, comparator);
                return Restrictions.eq("pp.program.concept.uuid", rawValue);

            case SearchFields.Program.LOCATION:
                requireEqComparator(field, comparator);
                return Restrictions.eq("pp.location.uuid", rawValue);

            // --- Program state fields (need states JOIN) ---
            case SearchFields.Program.STATUS:
                requireEqComparator(field, comparator);
                ensureStatesAlias(criteria, createdAliases);
                return Restrictions.eq("ps.state.concept.uuid", rawValue);

            case SearchFields.Program.STATUS_DATE:
                requireDateComparator(field, comparator);
                ensureStatesAlias(criteria, createdAliases);
                return dateRestriction("ps.startDate", comparator, parseDate(rawValue));

            // --- Patient identifier fields (need patient + identifiers JOINs) ---
            case SearchFields.Patient.IDENTIFIER_KIND:
                requireEqComparator(field, comparator);
                ensurePatientIdentifierAliases(criteria, createdAliases);
                return Restrictions.or(
                        Restrictions.eq("pi.identifierType.uuid", rawValue),
                        Restrictions.eq("pi.identifierType.name", rawValue));

            case SearchFields.Patient.IDENTIFIER_VALUE:
                requireEqComparator(field, comparator);
                ensurePatientIdentifierAliases(criteria, createdAliases);
                return Restrictions.eq("pi.identifier", rawValue);

            // --- Program attribute fields (need attributes JOIN) ---
            case SearchFields.Program.ATTRIBUTE_KIND:
                requireEqComparator(field, comparator);
                ensureAttributesAlias(criteria, createdAliases);
                return Restrictions.eq("ppa.attributeType.uuid", rawValue);

            case SearchFields.Program.ATTRIBUTE_VALUE:
                requireEqComparator(field, comparator);
                ensureAttributesAlias(criteria, createdAliases);
                return Restrictions.eq("ppa.valueReference", rawValue);

            default:
                throw new InvalidSearchCriteriaException(
                        "Unknown search field: '" + field + "'", SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    // ---- Alias helpers (each alias created at most once) ----

    private void ensureStatesAlias(Criteria criteria, Set<String> created) {
        if (created.add("ps")) {
            criteria.createAlias("pp.states", "ps", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("ps.voided", false));
        }
    }

    private void ensurePatientIdentifierAliases(Criteria criteria, Set<String> created) {
        if (created.add("pat")) {
            criteria.createAlias("pp.patient", "pat", JoinType.INNER_JOIN);
        }
        if (created.add("pi")) {
            criteria.createAlias("pat.identifiers", "pi", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("pi.voided", false));
        }
    }

    private void ensureAttributesAlias(Criteria criteria, Set<String> created) {
        if (created.add("ppa")) {
            criteria.createAlias("pp.attributes", "ppa", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("ppa.voided", false));
        }
    }

    // ---- Episode subqueries ----

    private Criterion episodeDateSubquery(String dateProperty, FieldComparator comparator, Date value) {
        DetachedCriteria subquery = DetachedCriteria.forClass(Episode.class, "e")
                .createAlias("e.patientPrograms", "epp")
                .add(Restrictions.eqProperty("epp.patientProgramId", "pp.patientProgramId"))
                .add(Restrictions.eq("e.voided", false))
                .add(dateRestriction(dateProperty, comparator, value))
                .setProjection(Projections.id());
        return Subqueries.exists(subquery);
    }

    private Criterion episodeCareManagerSubquery(String careManagerUuid) {
        DetachedCriteria subquery = DetachedCriteria.forClass(Episode.class, "e")
                .createAlias("e.patientPrograms", "epp")
                .createAlias("e.careManager", "ecm")
                .add(Restrictions.eqProperty("epp.patientProgramId", "pp.patientProgramId"))
                .add(Restrictions.eq("e.voided", false))
                .add(Restrictions.eq("ecm.uuid", careManagerUuid))
                .setProjection(Projections.id());
        return Subqueries.exists(subquery);
    }

    // ---- Value parsing and comparator validation ----

    private Criterion dateRestriction(String property, FieldComparator comparator, Date value) {
        switch (comparator) {
            case GT: return Restrictions.gt(property, value);
            case LT: return Restrictions.lt(property, value);
            default: throw new IllegalArgumentException("Unsupported date comparator: " + comparator);
        }
    }

    private Date parseDate(String value) {
        try {
            LocalDate localDate = LocalDate.parse(value, DATE_FORMAT);
            return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
        } catch (DateTimeParseException e) {
            throw new InvalidSearchCriteriaException(
                    "Invalid date format: '" + value + "'. Expected yyyy-MM-dd",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void requireEqComparator(String field, FieldComparator comparator) {
        if (comparator != FieldComparator.EQ) {
            throw new InvalidSearchCriteriaException(
                    "Only 'eq' comparator is supported for field '" + field + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void requireDateComparator(String field, FieldComparator comparator) {
        if (comparator != FieldComparator.GT && comparator != FieldComparator.LT) {
            throw new InvalidSearchCriteriaException(
                    "Only 'gt' and 'lt' comparators are supported for field '" + field + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
