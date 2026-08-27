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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class PatientProgramSearchDAOImpl implements PatientProgramSearchDAO {

    private final SessionFactory sessionFactory;
    private final PatientProgramCriteriaBuilder criteriaBuilder;

    public PatientProgramSearchDAOImpl(SessionFactory sessionFactory,
            PatientProgramCriteriaBuilder criteriaBuilder) {
        this.sessionFactory = sessionFactory;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    public List<Episode> search(SearchCondition searchCriteria) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Episode> query = cb.createQuery(Episode.class);

        Root<Episode> root = query.from(Episode.class);

        Fetch<Episode, ?> patientProgramFetch = root.fetch("patientPrograms", JoinType.INNER);
        Join<Episode, ?> patientProgram = (Join<Episode, ?>) patientProgramFetch;

        root.fetch("careManager", JoinType.LEFT);                                           // Episode → careManager (Provider)
        patientProgramFetch.fetch("patient", JoinType.INNER);                               // PatientProgram → patient
        patientProgramFetch.fetch("program", JoinType.LEFT).fetch("concept", JoinType.LEFT); // PatientProgram → program → concept
        patientProgramFetch.fetch("location", JoinType.LEFT);                               // PatientProgram → location

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get("voided")));
        predicates.add(cb.isFalse(patientProgram.get("voided")));

        EpisodeQueryContext context = new EpisodeQueryContext(cb, root, patientProgram, predicates, query);
        criteriaBuilder.apply(context, searchCriteria);
        query.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));

        return session.createQuery(query)
                .setHint("hibernate.query.passDistinctThrough", false)
                .getResultList();
    }
}
