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
import java.util.function.Supplier;


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

    /**
     * Returns the cached join for {@code key}, creating it with {@code resolver} on first use.
     * Deliberately not {@code joinCache.computeIfAbsent}: the resolver may itself resolve other
     * joins through this same cache, and a nested computeIfAbsent on one map throws
     * ConcurrentModificationException on JDK 9+. get/put is safe under that re-entrancy.
     */
    public From<?, ?> join(String key, Supplier<? extends From<?, ?>> resolver) {
        From<?, ?> existing = joinCache.get(key);
        if (existing == null) {
            existing = resolver.get();
            joinCache.put(key, existing);
        }
        return existing;
    }
}
