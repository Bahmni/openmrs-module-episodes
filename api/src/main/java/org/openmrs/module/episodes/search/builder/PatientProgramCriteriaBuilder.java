/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.openmrs.module.episodes.search.model.ConditionOperator;
import org.openmrs.module.episodes.search.model.FieldComparator;
import org.openmrs.module.episodes.search.model.SearchCriteria;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientProgramCriteriaBuilder {

    /** Root entity alias: Episode */
    public static final String ROOT_ALIAS = "ep";

    public static final String PATIENT_PROGRAM_ALIAS = "pp";

    public static final String PATIENT_ALIAS = "pat";

    private static final DateTimeFormatter ISO_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public void applyCondition(Criteria criteria, SearchCriteria condition) {
        applyCondition(criteria, condition, new HashSet<>());
    }

    public void applyCondition(Criteria criteria, SearchCriteria condition, Set<String> preCreatedAliases) {
        Set<String> createdAliases = new HashSet<>(preCreatedAliases);
        Criterion criterion = buildCriterion(criteria, condition, createdAliases);
        if (criterion != null) {
            criteria.add(criterion);
        }
    }

    private Criterion buildCriterion(Criteria criteria, SearchCriteria condition, Set<String> createdAliases) {
        if (condition.isLeaf()) {
            return buildLeafCriterion(criteria, condition, createdAliases);
        }

        List<Criterion> childCriteria = new ArrayList<>();
        for (SearchCriteria child : condition.getConditions()) {
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
        for (Criterion criterion : childCriteria) {
            junction.add(criterion);
        }
        return junction;
    }

    private Criterion buildLeafCriterion(Criteria criteria, SearchCriteria leaf, Set<String> createdAliases) {
        String field = leaf.getField();
        FieldComparator comparator = leaf.getComparator();
        String rawValue = leaf.getValue();

        if (comparator == null) {
            throw new InvalidSearchCriteriaException(
                    "Missing comparator for field '" + field + "'. Supported: eq, gt, lt",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        switch (field) {
            // --- Episode of Care fields (direct on root Episode entity) ---
            case SearchFields.EOC_START_DATE:
                validateDateOnly(field, comparator);
                return buildDateRestriction(ROOT_ALIAS + ".dateStarted", comparator, parseDate(rawValue));

            case SearchFields.EOC_END_DATE:
                validateDateOnly(field, comparator);
                return buildDateRestriction(ROOT_ALIAS + ".dateEnded", comparator, parseDate(rawValue));

            case SearchFields.EOC_CARE_MANAGER:
                validateEqOnly(field, comparator);
                joinCareManager(criteria, createdAliases);
                return Restrictions.eq("cm.uuid", rawValue);

            // --- Program fields (via enrollment alias) ---
            case SearchFields.PROGRAM_UUID:
                validateEqOnly(field, comparator);
                joinProgram(criteria, createdAliases);
                return Restrictions.eq("prog.uuid", rawValue);

            case SearchFields.PROGRAM_TYPE:
                validateEqOnly(field, comparator);
                joinProgramConcept(criteria, createdAliases);
                return Restrictions.eq("progConcept.uuid", rawValue);

            case SearchFields.PROGRAM_LOCATION:
                validateEqOnly(field, comparator);
                joinLocation(criteria, createdAliases);
                return Restrictions.eq("loc.uuid", rawValue);

            // --- Program state fields ---
            case SearchFields.PROGRAM_STATUS:
                validateEqOnly(field, comparator);
                joinStateConcept(criteria, createdAliases);
                return Restrictions.eq("psConcept.uuid", rawValue);

            case SearchFields.PROGRAM_STATUS_DATE:
                validateDateOnly(field, comparator);
                joinStates(criteria, createdAliases);
                return buildDateRestriction("ps.startDate", comparator, parseDate(rawValue));

            // --- Patient identifier fields ---
            case SearchFields.PATIENT_IDENTIFIER_KIND:
                validateEqOnly(field, comparator);
                joinIdentifierType(criteria, createdAliases);
                return Restrictions.or(
                        Restrictions.eq("pit.uuid", rawValue),
                        Restrictions.eq("pit.name", rawValue));

            case SearchFields.PATIENT_IDENTIFIER_VALUE:
                validateEqOnly(field, comparator);
                joinPatientIdentifiers(criteria, createdAliases);
                return Restrictions.eq("pi.identifier", rawValue);

            // --- Program attribute fields ---
            case SearchFields.PROGRAM_ATTRIBUTE_KIND:
                validateEqOnly(field, comparator);
                joinAttributeType(criteria, createdAliases);
                return Restrictions.eq("ppat.uuid", rawValue);

            case SearchFields.PROGRAM_ATTRIBUTE_VALUE:
                validateEqOnly(field, comparator);
                joinAttributes(criteria, createdAliases);
                return Restrictions.eq("ppa.valueReference", rawValue);

            default:
                throw new InvalidSearchCriteriaException(
                        "Unknown search field: '" + field + "'", SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    // ---- Join helpers (each alias created at most once) ----

    private void joinCareManager(Criteria criteria, Set<String> created) {
        if (created.add("cm")) {
            criteria.createAlias(ROOT_ALIAS + ".careManager", "cm", JoinType.INNER_JOIN);
        }
    }

    private void joinProgram(Criteria criteria, Set<String> created) {
        if (created.add("prog")) {
            criteria.createAlias(PATIENT_PROGRAM_ALIAS + ".program", "prog", JoinType.INNER_JOIN);
        }
    }

    private void joinProgramConcept(Criteria criteria, Set<String> created) {
        joinProgram(criteria, created);
        if (created.add("progConcept")) {
            criteria.createAlias("prog.concept", "progConcept", JoinType.INNER_JOIN);
        }
    }

    private void joinLocation(Criteria criteria, Set<String> created) {
        if (created.add("loc")) {
            criteria.createAlias(PATIENT_PROGRAM_ALIAS + ".location", "loc", JoinType.INNER_JOIN);
        }
    }

    private void joinStates(Criteria criteria, Set<String> created) {
        if (created.add("ps")) {
            criteria.createAlias(PATIENT_PROGRAM_ALIAS + ".states", "ps", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("ps.voided", false));
        }
    }

    private void joinWorkflowState(Criteria criteria, Set<String> created) {
        joinStates(criteria, created);
        if (created.add("psState")) {
            criteria.createAlias("ps.state", "psState", JoinType.INNER_JOIN);
        }
    }

    private void joinStateConcept(Criteria criteria, Set<String> created) {
        joinWorkflowState(criteria, created);
        if (created.add("psConcept")) {
            criteria.createAlias("psState.concept", "psConcept", JoinType.INNER_JOIN);
        }
    }

    private void joinPatientIdentifiers(Criteria criteria, Set<String> created) {
        if (created.add("pat")) {
            criteria.createAlias(PATIENT_PROGRAM_ALIAS + ".patient", "pat", JoinType.INNER_JOIN);
        }
        if (created.add("pi")) {
            criteria.createAlias("pat.identifiers", "pi", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("pi.voided", false));
        }
    }

    private void joinIdentifierType(Criteria criteria, Set<String> created) {
        joinPatientIdentifiers(criteria, created);
        if (created.add("pit")) {
            criteria.createAlias("pi.identifierType", "pit", JoinType.INNER_JOIN);
        }
    }

    private void joinAttributes(Criteria criteria, Set<String> created) {
        if (created.add("ppa")) {
            criteria.createAlias(PATIENT_PROGRAM_ALIAS + ".attributes", "ppa", JoinType.INNER_JOIN);
            criteria.add(Restrictions.eq("ppa.voided", false));
        }
    }

    private void joinAttributeType(Criteria criteria, Set<String> created) {
        joinAttributes(criteria, created);
        if (created.add("ppat")) {
            criteria.createAlias("ppa.attributeType", "ppat", JoinType.INNER_JOIN);
        }
    }

    // ---- Value parsing and comparator validation ----

    private Criterion buildDateRestriction(String property, FieldComparator comparator, Date value) {
        switch (comparator) {
            case GT: return Restrictions.gt(property, value);
            case LT: return Restrictions.lt(property, value);
            default: throw new IllegalArgumentException("Unsupported date comparator: " + comparator);
        }
    }

    private Date parseDate(String value) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(value, ISO_DATETIME_FORMAT);
            return Date.from(odt.toInstant());
        } catch (DateTimeParseException e) {
            throw new InvalidSearchCriteriaException(
                    "Invalid date format: '" + value + "'. Expected yyyy-MM-dd'T'HH:mm:ss.SSSZ (e.g. 2024-01-01T10:30:00.000+0530)",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateEqOnly(String field, FieldComparator comparator) {
        if (comparator != FieldComparator.EQ) {
            throw new InvalidSearchCriteriaException(
                    "Only 'eq' comparator is supported for field '" + field + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateDateOnly(String field, FieldComparator comparator) {
        if (comparator != FieldComparator.GT && comparator != FieldComparator.LT) {
            throw new InvalidSearchCriteriaException(
                    "Only 'gt' and 'lt' comparators are supported for field '" + field + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
