/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class PatientProgramJoinResolver {

    private static final String VOIDED = "voided";

    From<?, ?> joinCareManager(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("careManager",
                key -> findExistingFetchOrJoin(queryContext.episodeRoot, "careManager", JoinType.INNER));
    }

    From<?, ?> joinProgram(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("program",
                key -> findExistingFetchOrJoin(queryContext.patientProgramJoin, "program", JoinType.INNER));
    }

    From<?, ?> joinProgramConcept(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("programConcept",
                key -> findExistingFetchOrJoin(joinProgram(queryContext), "concept", JoinType.INNER));
    }

    From<?, ?> joinLocation(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("location",
                key -> findExistingFetchOrJoin(queryContext.patientProgramJoin, "location", JoinType.INNER));
    }

    From<?, ?> joinStates(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patientState", key -> {
            Join<?, ?> statesJoin = queryContext.patientProgramJoin.join("states", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(statesJoin.get(VOIDED)));
            return statesJoin;
        });
    }

    From<?, ?> joinStateConcept(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("stateConcept", key -> {
            Join<?, ?> workflowStateJoin = joinStates(queryContext).join("state", JoinType.INNER);
            return workflowStateJoin.join("concept", JoinType.INNER);
        });
    }

    From<?, ?> joinPatient(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patient",
                key -> findExistingFetchOrJoin(queryContext.patientProgramJoin, "patient", JoinType.INNER));
    }

    From<?, ?> joinPatientIdentifiers(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patientIdentifier", key -> {
            Join<?, ?> identifiersJoin = joinPatient(queryContext).join("identifiers", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(identifiersJoin.get(VOIDED)));
            return identifiersJoin;
        });
    }

    From<?, ?> joinIdentifierType(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("identifierType",
                key -> joinPatientIdentifiers(queryContext).join("identifierType", JoinType.INNER));
    }

    From<?, ?> joinAttributes(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("programAttribute", key -> {
            Join<?, ?> attributesJoin = queryContext.patientProgramJoin.join("attributes", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(VOIDED)));
            return attributesJoin;
        });
    }

    From<?, ?> joinAttributeType(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("programAttributeType",
                key -> joinAttributes(queryContext).join("attributeType", JoinType.INNER));
    }

    @SuppressWarnings("unchecked")
    private From<?, ?> findExistingFetchOrJoin(From<?, ?> parent, String attributeName, JoinType joinType) {
        for (Fetch<?, ?> fetch : parent.getFetches()) {
            if (attributeName.equals(fetch.getAttribute().getName())) {
                return (From<?, ?>) fetch;
            }
        }
        return parent.join(attributeName, joinType);
    }
}
