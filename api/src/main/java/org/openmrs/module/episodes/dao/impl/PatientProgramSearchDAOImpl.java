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
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodePatientProgram;
import org.openmrs.module.episodes.search.builder.EpisodeQueryContext;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.pagination.PaginationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PatientProgramSearchDAOImpl implements PatientProgramSearchDAO {

    private static final Logger log = LoggerFactory.getLogger(PatientProgramSearchDAOImpl.class);

    private static final String FIELD_VOIDED = "voided";
    private static final String FIELD_PATIENT_PROGRAM_ID = "patientProgramId";
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
    public List<Integer> findMatchingIds(SearchCondition searchCriteria, Long cursorId,
                                          String sortOrder, String direction, int limit) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<Episode> root = query.from(Episode.class);

        From<?, ?> patientProgram = root.join(JOIN_PATIENT_PROGRAMS, JoinType.INNER);
        List<Predicate> predicates = buildPredicates(cb, root, patientProgram, searchCriteria);

        boolean queryDescending = PaginationHelper.shouldSortQueryDescending(sortOrder, direction);

        if (cursorId != null) {
            if (queryDescending) {
                predicates.add(cb.lessThan(patientProgram.get(FIELD_PATIENT_PROGRAM_ID), cursorId));
            } else {
                predicates.add(cb.greaterThan(patientProgram.get(FIELD_PATIENT_PROGRAM_ID), cursorId));
            }
        }

        query.select(patientProgram.get(FIELD_PATIENT_PROGRAM_ID)).distinct(true);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(queryDescending
                ? cb.desc(patientProgram.get(FIELD_PATIENT_PROGRAM_ID))
                : cb.asc(patientProgram.get(FIELD_PATIENT_PROGRAM_ID)));

        List<Integer> matchingIds = session.createQuery(query)
                .setMaxResults(limit)
                .getResultList();

        log.debug("DEBUG findMatchingIds: found {} ids -> {}", matchingIds.size(), matchingIds);

        return matchingIds;
    }

    @Override
    public List<EpisodePatientProgram> findByIds(List<Integer> patientProgramIds) {
        if (patientProgramIds == null || patientProgramIds.isEmpty()) {
            return new ArrayList<>();
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Episode> query = cb.createQuery(Episode.class);
        Root<Episode> root = query.from(Episode.class);

        Join<Episode, ?> patientProgram = addFetchJoinsAndGetProgramJoin(root);

        query.select(root).distinct(true);
        query.where(
                patientProgram.get(FIELD_PATIENT_PROGRAM_ID).in(patientProgramIds),
                cb.isFalse(patientProgram.get(FIELD_VOIDED)));

        List<Episode> episodes = session.createQuery(query)
                .setHint(PaginationHelper.HINT_PASS_DISTINCT_THROUGH, false)
                .getResultList();

        log.debug("DEBUG findByIds: episodes query returned {} episode(s)", episodes.size());

        Set<Integer> requestedIds = new HashSet<>(patientProgramIds);
        List<EpisodePatientProgram> pairs = new ArrayList<>();
        for (Episode episode : episodes) {
            for (PatientProgram matchedProgram : episode.getPatientPrograms()) {
                if (requestedIds.contains(matchedProgram.getPatientProgramId())) {
                    pairs.add(new EpisodePatientProgram(episode, matchedProgram));
                }
            }
        }

        return PaginationHelper.reorderByIds(pairs, patientProgramIds,
                pair -> pair.getPatientProgram().getPatientProgramId());
    }


    @Override
    public long count(SearchCondition searchCriteria) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Episode> root = query.from(Episode.class);

        From<?, ?> patientProgram = root.join(JOIN_PATIENT_PROGRAMS, JoinType.INNER);
        List<Predicate> predicates = buildPredicates(cb, root, patientProgram, searchCriteria);

        query.select(cb.countDistinct(patientProgram.get(FIELD_PATIENT_PROGRAM_ID)))
                .where(predicates.toArray(new Predicate[0]));

        return session.createQuery(query).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Episode> root, From<?, ?> patientProgram, SearchCondition searchCriteria) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get(FIELD_VOIDED)));
        predicates.add(cb.isFalse(patientProgram.get(FIELD_VOIDED)));

        EpisodeQueryContext context = new EpisodeQueryContext(cb, root, patientProgram, predicates, query);
        criteriaBuilder.apply(context, searchCriteria);

        return predicates;
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
