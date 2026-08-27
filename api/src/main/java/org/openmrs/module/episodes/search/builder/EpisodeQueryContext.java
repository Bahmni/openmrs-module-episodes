/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.bahmni.search.builder.QueryContext;
import org.openmrs.module.episodes.Episode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;


public class EpisodeQueryContext extends QueryContext<Episode> {

    public final Root<Episode> episodeRoot;
    public final From<?, ?> patientProgramJoin;
    public final CriteriaQuery<?> query;

    public EpisodeQueryContext(CriteriaBuilder criteriaBuilder,
                               Root<Episode> episodeRoot,
                               From<?, ?> patientProgramJoin,
                               List<Predicate> predicates,
                               CriteriaQuery<?> query) {
        super(criteriaBuilder, episodeRoot, predicates);
        this.episodeRoot = episodeRoot;
        this.patientProgramJoin = patientProgramJoin;
        this.query = query;
    }
}
