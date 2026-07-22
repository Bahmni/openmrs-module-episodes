/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao.impl;

import org.hibernate.SessionFactory;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.query.PatientProgramQueryBuilder;
import org.openmrs.module.episodes.search.criteria.Condition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PatientProgramSearchDAOImpl implements PatientProgramSearchDAO {

    private final SessionFactory sessionFactory;
    private final PatientProgramQueryBuilder queryBuilder;

    public PatientProgramSearchDAOImpl(SessionFactory sessionFactory,
            PatientProgramQueryBuilder queryBuilder) {
        this.sessionFactory = sessionFactory;
        this.queryBuilder = queryBuilder;
    }

    @Override
    public List<PatientProgram> search(Condition criteria) {
        CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<PatientProgram> cq = cb.createQuery(PatientProgram.class);
        Root<PatientProgram> root = cq.from(PatientProgram.class);

        List<Predicate> voidedFilters = new ArrayList<>();
        List<Predicate> searchPredicates = queryBuilder.buildPredicates(cb, root, cq, voidedFilters, criteria);

        List<Predicate> allPredicates = new ArrayList<>();
        allPredicates.add(cb.equal(root.get("voided"), false));
        allPredicates.addAll(voidedFilters);
        allPredicates.addAll(searchPredicates);

        cq.select(root).distinct(true).where(cb.and(allPredicates.toArray(new Predicate[0])));

        return sessionFactory.getCurrentSession().createQuery(cq).getResultList();
    }

    @Override
    public List<Episode> getEpisodesForPatientProgramIds(Set<Integer> patientProgramIds) {
        if (patientProgramIds.isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Episode> cq = cb.createQuery(Episode.class);
        Root<Episode> root = cq.from(Episode.class);
        Join<Episode, PatientProgram> ppJoin = root.join("patientPrograms", JoinType.INNER);

        cq.select(root).distinct(true).where(cb.and(
                cb.equal(root.get("voided"), false),
                ppJoin.get("patientProgramId").in(patientProgramIds)
        ));

        return sessionFactory.getCurrentSession().createQuery(cq).getResultList();
    }
}
