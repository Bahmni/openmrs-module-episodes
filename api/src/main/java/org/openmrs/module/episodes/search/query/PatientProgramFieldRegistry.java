/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.query;

import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.criteria.FieldComparator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class PatientProgramFieldRegistry {

    enum JoinKey { PS, PI, PPA }

    enum FieldType { TEXT, DATE }

    @FunctionalInterface
    interface PredicateBuilder {
        Predicate build(CriteriaBuilder cb, Root<PatientProgram> root, CriteriaQuery<?> cq,
                Map<JoinKey, Join<?, ?>> joins, FieldComparator comparator, Object value);
    }

    static class FieldDescriptor {
        final Set<FieldComparator> allowedComparators;
        final Set<JoinKey> requiredJoins;
        final PredicateBuilder predicateFn;
        final FieldType valueType;

        FieldDescriptor(Set<FieldComparator> comparators, Set<JoinKey> joins,
                PredicateBuilder fn, FieldType valueType) {
            this.allowedComparators = comparators;
            this.requiredJoins = joins;
            this.predicateFn = fn;
            this.valueType = valueType;
        }
    }

    private static final Map<String, FieldDescriptor> REGISTRY = new HashMap<>();

    static {
        // ── episodeOfCare.* fields: no direct path from PatientProgram → Episode.
        //    Use a correlated EXISTS subquery via the episode_patient_program junction.

        REGISTRY.put(SearchFields.EpisodeOfCare.START_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                EnumSet.noneOf(JoinKey.class),
                (cb, root, cq, joins, comparator, value) ->
                        episodeExists(cb, root, cq, e -> applyDateComparator(
                                cb, e.get("dateStarted"), comparator, (Date) value)),
                FieldType.DATE
        ));
        REGISTRY.put(SearchFields.EpisodeOfCare.END_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                EnumSet.noneOf(JoinKey.class),
                (cb, root, cq, joins, comparator, value) ->
                        episodeExists(cb, root, cq, e -> applyDateComparator(
                                cb, e.get("dateEnded"), comparator, (Date) value)),
                FieldType.DATE
        ));
        REGISTRY.put(SearchFields.EpisodeOfCare.CARE_MANAGER, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.noneOf(JoinKey.class),
                (cb, root, cq, joins, comparator, value) ->
                        episodeExists(cb, root, cq, e ->
                                cb.equal(e.get("careManager").get("uuid"), value)),
                FieldType.TEXT
        ));

        // ── program.* fields: direct navigation from PatientProgram root. No joins needed.

        REGISTRY.put(SearchFields.Program.UUID, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.noneOf(JoinKey.class),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(root.get("program").get("uuid"), value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.TYPE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.noneOf(JoinKey.class),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(root.get("program").get("concept").get("uuid"), value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.LOCATION, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.noneOf(JoinKey.class),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(root.get("location").get("uuid"), value),
                FieldType.TEXT
        ));

        // ── program.status / statusDate: join PatientProgram → states directly.

        REGISTRY.put(SearchFields.Program.STATUS, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PS),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(joins.get(JoinKey.PS).get("state").get("concept").get("uuid"), value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.STATUS_DATE, new FieldDescriptor(
                EnumSet.of(FieldComparator.GT, FieldComparator.LT),
                EnumSet.of(JoinKey.PS),
                (cb, root, cq, joins, comparator, value) ->
                        applyDateComparator(cb, joins.get(JoinKey.PS).get("startDate"), comparator, (Date) value),
                FieldType.DATE
        ));

        // ── patient.identifiers.*: PatientProgram → patient → identifiers.

        REGISTRY.put(SearchFields.Patient.IDENTIFIER_KIND, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PI),
                (cb, root, cq, joins, comparator, value) -> cb.or(
                        cb.equal(joins.get(JoinKey.PI).get("identifierType").get("uuid"), value),
                        cb.equal(joins.get(JoinKey.PI).get("identifierType").get("name"), value)
                ),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Patient.IDENTIFIER_VALUE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PI),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(joins.get(JoinKey.PI).get("identifier"), value),
                FieldType.TEXT
        ));

        // ── program.attributeType.*: PatientProgram → attributes directly.

        REGISTRY.put(SearchFields.Program.ATTRIBUTE_KIND, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PPA),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(joins.get(JoinKey.PPA).get("attributeType").get("uuid"), value),
                FieldType.TEXT
        ));
        REGISTRY.put(SearchFields.Program.ATTRIBUTE_VALUE, new FieldDescriptor(
                EnumSet.of(FieldComparator.EQ),
                EnumSet.of(JoinKey.PPA),
                (cb, root, cq, joins, comparator, value) ->
                        cb.equal(joins.get(JoinKey.PPA).get("valueReference"), value),
                FieldType.TEXT
        ));
    }

    static FieldDescriptor get(String field) {
        return REGISTRY.get(field);
    }

    /**
     * Correlated EXISTS subquery: finds PatientPrograms linked to at least one non-voided
     * Episode that satisfies the given episode-level predicate.
     *
     *   EXISTS (
     *     SELECT 1 FROM Episode e
     *     INNER JOIN e.patientPrograms pp
     *     WHERE pp = :outerRoot
     *       AND e.voided = false
     *       AND {episodePredicate}
     *   )
     */
    static Predicate episodeExists(CriteriaBuilder cb, Root<PatientProgram> outerRoot,
            CriteriaQuery<?> cq, Function<Root<Episode>, Predicate> episodePredicate) {
        Subquery<Integer> sub = cq.subquery(Integer.class);
        Root<Episode> e = sub.from(Episode.class);
        Join<Episode, PatientProgram> ppJoin = e.join("patientœPrograms", JoinType.INNER);

        sub.select(cb.literal(1))
           .where(cb.and(
               cb.equal(ppJoin, outerRoot),
               cb.equal(e.get("voided"), false),
               episodePredicate.apply(e)
           ));

        return cb.exists(sub);
    }

    @SuppressWarnings("unchecked")
    static Predicate applyDateComparator(CriteriaBuilder cb, Path<?> path,
            FieldComparator comparator, Date value) {
        Path<Date> datePath = (Path<Date>) path;
        switch (comparator) {
            case GT: return cb.greaterThan(datePath, value);
            case LT: return cb.lessThan(datePath, value);
            default: throw new IllegalArgumentException("Unsupported date comparator: " + comparator);
        }
    }

    private PatientProgramFieldRegistry() {}
}
