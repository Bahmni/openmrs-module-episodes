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
        return queryContext.join("careManager", () -> JoinResolvers.findExistingFetchOrJoin(queryContext.episodeRoot, "careManager", JoinType.INNER));
    }

    From<?, ?> joinProgram(EpisodeQueryContext queryContext) {
        return queryContext.join("program", () -> JoinResolvers.findExistingFetchOrJoin(queryContext.patientProgramJoin, "program", JoinType.INNER));
    }

    From<?, ?> joinProgramConcept(EpisodeQueryContext queryContext) {
        From<?, ?> programJoin = joinProgram(queryContext);
        return queryContext.join("programConcept", () -> JoinResolvers.findExistingFetchOrJoin(programJoin, "concept", JoinType.INNER));
    }

    From<?, ?> joinLocation(EpisodeQueryContext queryContext) {
        return queryContext.join("location", () -> JoinResolvers.findExistingFetchOrJoin(queryContext.patientProgramJoin, "location", JoinType.INNER));
    }

    From<?, ?> joinStates(EpisodeQueryContext queryContext) {
        From<?, ?> patientProgramForFilter = joinPatientProgramForFilter(queryContext);
        return queryContext.join("patientState", () -> {
            Join<?, ?> statesJoin = patientProgramForFilter.join("states", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(statesJoin.get(VOIDED)));
            queryContext.predicates.add(buildCurrentStateRestriction(queryContext, statesJoin));
            return statesJoin;
        });
    }

    private From<?, ?> joinPatientProgramForFilter(EpisodeQueryContext queryContext) {
        return queryContext.join("patientProgramFilter", () -> {
            Join<?, ?> filterJoin = queryContext.episodeRoot.join("patientPrograms", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(filterJoin.get(VOIDED)));
            return filterJoin;
        });
    }

    /**
     * Restricts the joined patient state to the "current" state of its program, matching the
     * semantics of selectCurrentState: active states (no end date) outrank ended states; within
     * a group the latest date wins (startDate for active, endDate for ended, with non-null
     * startDate preferred over null); date ties are broken by the highest patientStateId.
     */
    private Predicate buildCurrentStateRestriction(EpisodeQueryContext queryContext, From<?, ?> statesJoin) {
        CriteriaBuilder cb = queryContext.criteriaBuilder;
        Subquery<Integer> subquery = queryContext.query.subquery(Integer.class);
        Root<PatientState> stateA = subquery.from(PatientState.class);
        From<?, ?> stateB = statesJoin;

        subquery.select(stateA.get(STATE_ID));
        subquery.where(
                cb.equal(stateA.get(PATIENT_PROGRAM), stateB.get(PATIENT_PROGRAM)),
                cb.isFalse(stateA.get(VOIDED)),
                cb.notEqual(stateA.get(STATE_ID), stateB.get(STATE_ID)),
                isMoreRecentThan(cb, stateA, stateB));

        // If no other state is more recent than stateB, then stateB is the current one — keep it.
        return cb.not(cb.exists(subquery));
    }

    // Does stateA come later (chronologically) than stateB?
    private Predicate isMoreRecentThan(CriteriaBuilder cb, From<?, ?> stateA, From<?, ?> stateB) {
        Path<Date> stateAEndDate = stateA.get(END_DATE);
        Path<Date> stateBEndDate = stateB.get(END_DATE);
        Path<Date> stateAStartDate = stateA.get(START_DATE);
        Path<Date> stateBStartDate = stateB.get(START_DATE);
        Path<Integer> stateAId = stateA.get(STATE_ID);
        Path<Integer> stateBId = stateB.get(STATE_ID);

        Predicate stateAHasNoEndDate = cb.isNull(stateAEndDate);
        Predicate stateBHasNoEndDate = cb.isNull(stateBEndDate);

        // A state with no end date (still active) is always more recent than one that has already ended.
        Predicate currentActiveState = cb.and(stateAHasNoEndDate, cb.isNotNull(stateBEndDate));

        Predicate isBothActive = cb.and(stateAHasNoEndDate, stateBHasNoEndDate);
        Predicate stateAStartedRecently = cb.and(isBothActive, cb.or(
                cb.and(cb.isNotNull(stateAStartDate), cb.isNull(stateBStartDate)),
                cb.and(cb.isNotNull(stateAStartDate), cb.isNotNull(stateBStartDate),
                        cb.greaterThan(stateAStartDate, stateBStartDate)),
                cb.and(cb.isNotNull(stateAStartDate), cb.isNotNull(stateBStartDate),
                        cb.equal(stateAStartDate, stateBStartDate), cb.greaterThan(stateAId, stateBId)),
                cb.and(cb.isNull(stateAStartDate), cb.isNull(stateBStartDate), cb.greaterThan(stateAId, stateBId))));

        Predicate bothEnded = cb.and(cb.isNotNull(stateAEndDate), cb.isNotNull(stateBEndDate));
        Predicate stateAEndedRecently = cb.and(bothEnded, cb.or(
                cb.greaterThan(stateAEndDate, stateBEndDate),
                cb.and(cb.equal(stateAEndDate, stateBEndDate), cb.greaterThan(stateAId, stateBId))));

        return cb.or(currentActiveState, stateAStartedRecently, stateAEndedRecently);
    }

    From<?, ?> joinStateConcept(EpisodeQueryContext queryContext) {
        From<?, ?> statesJoin = joinStates(queryContext);
        return queryContext.join("stateConcept", () -> {
            Join<?, ?> workflowStateJoin = statesJoin.join("state", JoinType.INNER);
            return workflowStateJoin.join("concept", JoinType.INNER);
        });
    }

    From<?, ?> joinPatient(EpisodeQueryContext queryContext) {
        return queryContext.join("patient", () -> JoinResolvers.findExistingFetchOrJoin(queryContext.patientProgramJoin, "patient", JoinType.INNER));
    }

    From<?, ?> joinPatientIdentifiers(EpisodeQueryContext queryContext) {
        From<?, ?> patientJoin = joinPatient(queryContext);
        return queryContext.join("patientIdentifier", () -> {
            Join<?, ?> identifiersJoin = patientJoin.join("identifiers", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(identifiersJoin.get(VOIDED)));
            return identifiersJoin;
        });
    }

    From<?, ?> joinIdentifierType(EpisodeQueryContext queryContext) {
        From<?, ?> identifiersJoin = joinPatientIdentifiers(queryContext);
        return queryContext.join("identifierType", () -> identifiersJoin.join("identifierType", JoinType.INNER));
    }

    From<?, ?> joinAttributes(EpisodeQueryContext queryContext) {
        return queryContext.join("programAttribute", () -> {
            Join<?, ?> attributesJoin = queryContext.patientProgramJoin.join("attributes", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(VOIDED)));
            return attributesJoin;
        });
    }

    From<?, ?> joinAttributeType(EpisodeQueryContext queryContext) {
        From<?, ?> attributesJoin = joinAttributes(queryContext);
        return queryContext.join("programAttributeType", () -> attributesJoin.join("attributeType", JoinType.INNER));
    }
}
