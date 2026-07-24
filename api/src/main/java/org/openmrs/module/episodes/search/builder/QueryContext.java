/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.openmrs.module.episodes.Episode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryContext {

    final CriteriaBuilder criteriaBuilder;
    final Root<Episode> episodeRoot;
    final From<?, ?> patientProgramJoin;
    final List<Predicate> predicates;
    final Map<String, From<?, ?>> joinCache = new HashMap<>();

    public QueryContext(CriteriaBuilder criteriaBuilder, Root<Episode> episodeRoot,
                        From<?, ?> patientProgramJoin, List<Predicate> predicates) {
        this.criteriaBuilder = criteriaBuilder;
        this.episodeRoot = episodeRoot;
        this.patientProgramJoin = patientProgramJoin;
        this.predicates = predicates;
    }
}
