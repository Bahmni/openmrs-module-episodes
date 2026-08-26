/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.bahmni.search.builder.JoinResolvers;
import org.openmrs.PatientState;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Date;

public class PatientProgramJoinResolver {

    private static final String VOIDED = "voided";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String STATE_ID = "patientStateId";
    private static final String PATIENT_PROGRAM = "patientProgram";

    From<?, ?> joinCareManager(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("careManager",
                key -> JoinResolvers.findExistingFetchOrJoin(queryContext.episodeRoot, "careManager", JoinType.INNER));
    }

    From<?, ?> joinProgram(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("program",
                key -> JoinResolvers.findExistingFetchOrJoin(queryContext.patientProgramJoin, "program", JoinType.INNER));
    }

    From<?, ?> joinProgramConcept(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("programConcept",
                key -> JoinResolvers.findExistingFetchOrJoin(joinProgram(queryContext), "concept", JoinType.INNER));
    }

    From<?, ?> joinLocation(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("location",
                key -> JoinResolvers.findExistingFetchOrJoin(queryContext.patientProgramJoin, "location", JoinType.INNER));
    }

    From<?, ?> joinStates(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patientState", key -> {
            From<?, ?> patientProgramForFilter = joinPatientProgramForFilter(queryContext);
            Join<?, ?> statesJoin = patientProgramForFilter.join("states", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(statesJoin.get(VOIDED)));
            queryContext.predicates.add(buildCurrentStateRestriction(queryContext, statesJoin));
            return statesJoin;
        });
    }

    private From<?, ?> joinPatientProgramForFilter(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patientProgramFilter", key -> {
            Join<?, ?> filterJoin = queryContext.episodeRoot.join("patientPrograms", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(filterJoin.get(VOIDED)));
            return filterJoin;
        });
    }

    /**
     * Restricts the joined patient state to the "current" state of its program, matching the
     * semantics of selectCurrentState: active states (no end date) outrank ended states; within
     * a group the latest date wins (startDate for active, endDate for ended, with non-null
     * startDate preferred over null); date ties are broken by the lowest patientStateId.
     */
    private Predicate buildCurrentStateRestriction(EpisodeQueryContext queryContext, From<?, ?> statesJoin) {
        CriteriaBuilder cb = queryContext.criteriaBuilder;
        Subquery<Integer> subquery = queryContext.query.subquery(Integer.class);
        Root<PatientState> other = subquery.from(PatientState.class);
        From<?, ?> current = statesJoin;

        subquery.select(other.<Integer>get(STATE_ID));
        subquery.where(
                cb.equal(other.get(PATIENT_PROGRAM), current.get(PATIENT_PROGRAM)),
                cb.isFalse(other.get(VOIDED)),
                cb.notEqual(other.get(STATE_ID), current.get(STATE_ID)),
                otherOutranksCurrent(cb, other, current));

        return cb.not(cb.exists(subquery));
    }

    private Predicate otherOutranksCurrent(CriteriaBuilder cb, From<?, ?> other, From<?, ?> current) {
        Path<Date> otherEnd = other.get(END_DATE);
        Path<Date> currentEnd = current.get(END_DATE);
        Path<Date> otherStart = other.get(START_DATE);
        Path<Date> currentStart = current.get(START_DATE);
        Path<Integer> otherId = other.get(STATE_ID);
        Path<Integer> currentId = current.get(STATE_ID);

        Predicate otherActive = cb.isNull(otherEnd);
        Predicate currentActive = cb.isNull(currentEnd);

        Predicate otherActiveOutranksEnded = cb.and(otherActive, cb.isNotNull(currentEnd));

        Predicate bothActive = cb.and(otherActive, currentActive);
        Predicate otherIsMoreRecentActiveState = cb.and(bothActive, cb.or(
                cb.and(cb.isNotNull(otherStart), cb.isNull(currentStart)),
                cb.and(cb.isNotNull(otherStart), cb.isNotNull(currentStart),
                        cb.greaterThan(otherStart, currentStart)),
                cb.and(cb.isNotNull(otherStart), cb.isNotNull(currentStart),
                        cb.equal(otherStart, currentStart), cb.lessThan(otherId, currentId)),
                cb.and(cb.isNull(otherStart), cb.isNull(currentStart), cb.lessThan(otherId, currentId))));

        Predicate bothEnded = cb.and(cb.isNotNull(otherEnd), cb.isNotNull(currentEnd));
        Predicate otherIsMoreRecentEndedState = cb.and(bothEnded, cb.or(
                cb.greaterThan(otherEnd, currentEnd),
                cb.and(cb.equal(otherEnd, currentEnd), cb.lessThan(otherId, currentId))));

        return cb.or(otherActiveOutranksEnded, otherIsMoreRecentActiveState, otherIsMoreRecentEndedState);
    }

    From<?, ?> joinStateConcept(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("stateConcept", key -> {
            Join<?, ?> workflowStateJoin = joinStates(queryContext).join("state", JoinType.INNER);
            return workflowStateJoin.join("concept", JoinType.INNER);
        });
    }

    From<?, ?> joinPatient(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patient",
                key -> JoinResolvers.findExistingFetchOrJoin(queryContext.patientProgramJoin, "patient", JoinType.INNER));
    }

    From<?, ?> joinPatientIdentifiers(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patientIdentifier", key -> {
            Join<?, ?> identifiersJoin = joinPatient(queryContext).join("identifiers", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(identifiersJoin.get(VOIDED)));
            return identifiersJoin;
        });
    }

    From<?, ?> joinIdentifierType(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("identifierType",
                key -> joinPatientIdentifiers(queryContext).join("identifierType", JoinType.INNER));
    }

    From<?, ?> joinAttributes(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("programAttribute", key -> {
            Join<?, ?> attributesJoin = queryContext.patientProgramJoin.join("attributes", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(VOIDED)));
            return attributesJoin;
        });
    }

    From<?, ?> joinAttributeType(EpisodeQueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("programAttributeType",
                key -> joinAttributes(queryContext).join("attributeType", JoinType.INNER));
    }
}
