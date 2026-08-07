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

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class PatientProgramJoinResolver {

    private static final String VOIDED = "voided";

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
            Join<?, ?> statesJoin = queryContext.patientProgramJoin.join("states", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(statesJoin.get(VOIDED)));
            return statesJoin;
        });
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
