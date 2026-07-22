/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.impl;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.model.FieldComparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class PatientProgramFieldRegistry {

    enum FieldType { TEXT, DATE }

    /**
     * Describes a Hibernate alias (createAlias call) required before a field can be queried.
     */
    static class AliasDescriptor {
        final String associationPath;
        final String alias;
        final org.hibernate.sql.JoinType joinType;
        final boolean filterVoided;

        AliasDescriptor(String associationPath, String alias,
                org.hibernate.sql.JoinType joinType, boolean filterVoided) {
            this.associationPath = associationPath;
            this.alias = alias;
            this.joinType = joinType;
            this.filterVoided = filterVoided;
        }
    }

    @FunctionalInterface
    interface CriterionBuilder {
        Criterion build(FieldComparator comparator, Object value);
    }

    static class FieldDescriptor {
        final Set<FieldComparator> allowedComparators;
        final List<AliasDescriptor> requiredAliases;
        final CriterionBuilder criterionFn;
        final FieldType valueType;

        FieldDescriptor(Set<FieldComparator> comparators, List<AliasDescriptor> aliases,
                CriterionBuilder fn, FieldType valueType) {
            this.allowedComparators = comparators;
            this.requiredAliases = aliases;
            this.criterionFn = fn;
            this.valueType = valueType;
        }
    }

    // ── Shared alias descriptors ─────────────────────────────────────────────

    private static final AliasDescriptor ALIAS_STATES = new AliasDescriptor(
            "pp.states", "ps", org.hibernate.sql.JoinType.INNER_JOIN, true);

    private static final AliasDescriptor ALIAS_PATIENT = new AliasDescriptor(
            "pp.patient", "pat", org.hibernate.sql.JoinType.INNER_JOIN, false);

    private static final AliasDescriptor ALIAS_PATIENT_IDENTIFIERS = new AliasDescriptor(
            "pat.identifiers", "pi", org.hibernate.sql.JoinType.INNER_JOIN, true);

    private static final AliasDescriptor ALIAS_ATTRIBUTES = new AliasDescriptor(
            "pp.attributes", "ppa", org.hibernate.sql.JoinType.INNER_JOIN, true);

    // ── Field registry ───────────────────────────────────────────────────────

    private static final Map<String, FieldDescriptor> REGISTRY = new HashMap<>();

    static {
        // ── episodeOfCare.* fields: correlated EXISTS subquery via Episode → patientPrograms

        REGISTRY.put(SearchFields.EpisodeOfCare.START_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                Collections.<AliasDescriptor>emptyList(),
                (comparator, value) -> episodeExists("e.dateStarted", comparator, (Date) value),
                FieldType.DATE
        ));
        REGISTRY.put(SearchFields.EpisodeOfCare.END_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                Collections.<AliasDescriptor>emptyList(),
                (comparator, value) -> episodeExists("e.dateEnded", comparator, (Date) value),
                FieldType.DATE
        ));
        REGISTRY.put(SearchFields.EpisodeOfCare.CARE_MANAGER, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.<AliasDescriptor>emptyList(),
                (comparator, value) -> episodeExistsEq("ecm.uuid", value),
                FieldType.TEXT
        ));

        // ── program.* fields: direct navigation from root (pp). No aliases needed.

        REGISTRY.put(SearchFields.Program.UUID, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.<AliasDescriptor>emptyList(),
                (comparator, value) -> Restrictions.eq("pp.program.uuid", value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.TYPE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.<AliasDescriptor>emptyList(),
                (comparator, value) -> Restrictions.eq("pp.program.concept.uuid", value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.LOCATION, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.<AliasDescriptor>emptyList(),
                (comparator, value) -> Restrictions.eq("pp.location.uuid", value),
                FieldType.TEXT
        ));

        // ── program.status / statusDate: require states join (ps).

        REGISTRY.put(SearchFields.Program.STATUS, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.singletonList(ALIAS_STATES),
                (comparator, value) -> Restrictions.eq("ps.state.concept.uuid", value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.STATUS_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                Collections.singletonList(ALIAS_STATES),
                (comparator, value) -> applyDateComparator("ps.startDate", comparator, (Date) value),
                FieldType.DATE
        ));

        // ── patient.identifiers.*: require patient + identifiers joins (pat, pi).

        REGISTRY.put(SearchFields.Patient.IDENTIFIER_KIND, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Arrays.asList(ALIAS_PATIENT, ALIAS_PATIENT_IDENTIFIERS),
                (comparator, value) -> Restrictions.or(
                        Restrictions.eq("pi.identifierType.uuid", value),
                        Restrictions.eq("pi.identifierType.name", value)
                ),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Patient.IDENTIFIER_VALUE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Arrays.asList(ALIAS_PATIENT, ALIAS_PATIENT_IDENTIFIERS),
                (comparator, value) -> Restrictions.eq("pi.identifier", value),
                FieldType.TEXT
        ));

        // ── program.attributeType.*: require attributes join (ppa).

        REGISTRY.put(SearchFields.Program.ATTRIBUTE_KIND, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.singletonList(ALIAS_ATTRIBUTES),
                (comparator, value) -> Restrictions.eq("ppa.attributeType.uuid", value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.ATTRIBUTE_VALUE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                Collections.singletonList(ALIAS_ATTRIBUTES),
                (comparator, value) -> Restrictions.eq("ppa.valueReference", value),
                FieldType.TEXT
        ));
    }

    static FieldDescriptor get(String field) {
        return REGISTRY.get(field);
    }

    /**
     * Correlated EXISTS subquery for episode date fields.
     *
     *   EXISTS (
     *     SELECT e.id FROM Episode e
     *     INNER JOIN e.patientPrograms epp
     *     WHERE epp.patientProgramId = pp.patientProgramId
     *       AND e.voided = false
     *       AND e.{dateProperty} {comparator} :value
     *   )
     */
    private static Criterion episodeExists(String dateProperty, FieldComparator comparator, Date value) {
        DetachedCriteria sub = DetachedCriteria.forClass(Episode.class, "e")
                .createAlias("e.patientPrograms", "epp")
                .add(Restrictions.eqProperty("epp.patientProgramId", "pp.patientProgramId"))
                .add(Restrictions.eq("e.voided", false))
                .add(applyDateComparator(dateProperty, comparator, value))
                .setProjection(Projections.id());
        return Subqueries.exists(sub);
    }

    /**
     * Correlated EXISTS subquery for episode equality fields (e.g. careManager.uuid).
     */
    private static Criterion episodeExistsEq(String property, Object value) {
        DetachedCriteria sub = DetachedCriteria.forClass(Episode.class, "e")
                .createAlias("e.patientPrograms", "epp")
                .createAlias("e.careManager", "ecm")
                .add(Restrictions.eqProperty("epp.patientProgramId", "pp.patientProgramId"))
                .add(Restrictions.eq("e.voided", false))
                .add(Restrictions.eq(property, value))
                .setProjection(Projections.id());
        return Subqueries.exists(sub);
    }

    static Criterion applyDateComparator(String property, FieldComparator comparator, Date value) {
        switch (comparator) {
            case GT: return Restrictions.gt(property, value);
            case LT: return Restrictions.lt(property, value);
            default: throw new IllegalArgumentException("Unsupported date comparator: " + comparator);
        }
    }

    private PatientProgramFieldRegistry() {}
}
