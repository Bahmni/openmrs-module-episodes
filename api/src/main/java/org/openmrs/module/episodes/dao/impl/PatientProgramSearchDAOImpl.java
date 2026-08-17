/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.builder.EpisodeQueryContext;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.pagination.PaginationHelper;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class PatientProgramSearchDAOImpl implements PatientProgramSearchDAO {

    private static final String FIELD_VOIDED = "voided";
    private static final String FIELD_EPISODE_ID = "episodeId";
    private static final String JOIN_PATIENT_PROGRAMS = "patientPrograms";
    private static final String FETCH_CARE_MANAGER = "careManager";
    private static final String FETCH_PATIENT = "patient";
    private static final String FETCH_PROGRAM = "program";
    private static final String FETCH_CONCEPT = "concept";
    private static final String FETCH_LOCATION = "location";

    private final SessionFactory sessionFactory;
    private final PatientProgramCriteriaBuilder criteriaBuilder;

    public PatientProgramSearchDAOImpl(SessionFactory sessionFactory,
            PatientProgramCriteriaBuilder criteriaBuilder) {
        this.sessionFactory = sessionFactory;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    public List<Episode> search(SearchCondition searchCriteria, Long cursorId,
                                 String sortOrder, String direction, int limit) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Episode> query = cb.createQuery(Episode.class);
        Root<Episode> root = query.from(Episode.class);

        Join<Episode, ?> patientProgram = addFetchJoinsAndGetProgramJoin(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get(FIELD_VOIDED)));
        predicates.add(cb.isFalse(patientProgram.get(FIELD_VOIDED)));

        EpisodeQueryContext context = new EpisodeQueryContext(cb, root, patientProgram, predicates);
        criteriaBuilder.apply(context, searchCriteria);

        boolean queryDescending = PaginationHelper.resolveQueryDescending(sortOrder, direction);

        if (cursorId != null) {
            if (queryDescending) {
                predicates.add(cb.lessThan(root.get(FIELD_EPISODE_ID), cursorId.intValue()));
            } else {
                predicates.add(cb.greaterThan(root.get(FIELD_EPISODE_ID), cursorId.intValue()));
            }
        }

        query.select(root).distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(queryDescending
                        ? cb.desc(root.get(FIELD_EPISODE_ID))
                        : cb.asc(root.get(FIELD_EPISODE_ID)));

        return session.createQuery(query)
                .setHint(PaginationHelper.HINT_PASS_DISTINCT_THROUGH, false)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long count(SearchCondition searchCriteria) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Episode> root = query.from(Episode.class);

        From<?, ?> patientProgram = root.join(JOIN_PATIENT_PROGRAMS, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get(FIELD_VOIDED)));
        predicates.add(cb.isFalse(patientProgram.get(FIELD_VOIDED)));

        EpisodeQueryContext context = new EpisodeQueryContext(cb, root, patientProgram, predicates);
        criteriaBuilder.apply(context, searchCriteria);

        query.select(cb.countDistinct(root))
                .where(predicates.toArray(new Predicate[0]));

        return session.createQuery(query).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    private Join<Episode, ?> addFetchJoinsAndGetProgramJoin(Root<Episode> root) {
        Fetch<Episode, ?> patientProgramFetch = root.fetch(JOIN_PATIENT_PROGRAMS, JoinType.INNER);
        Join<Episode, ?> patientProgram = (Join<Episode, ?>) patientProgramFetch;

        root.fetch(FETCH_CARE_MANAGER, JoinType.LEFT);
        patientProgramFetch.fetch(FETCH_PATIENT, JoinType.INNER);
        patientProgramFetch.fetch(FETCH_PROGRAM, JoinType.LEFT).fetch(FETCH_CONCEPT, JoinType.LEFT);
        patientProgramFetch.fetch(FETCH_LOCATION, JoinType.LEFT);

        return patientProgram;
    }
}
