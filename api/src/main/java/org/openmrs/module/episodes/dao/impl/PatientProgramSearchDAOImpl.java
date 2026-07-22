/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder;
import static org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder.ROOT_ALIAS;
import org.openmrs.module.episodes.search.model.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PatientProgramSearchDAOImpl implements PatientProgramSearchDAO {

    private static final Logger log = LoggerFactory.getLogger(PatientProgramSearchDAOImpl.class);

    private final SessionFactory sessionFactory;
    private final PatientProgramCriteriaBuilder criteriaBuilder;

    public PatientProgramSearchDAOImpl(SessionFactory sessionFactory,
            PatientProgramCriteriaBuilder criteriaBuilder) {
        this.sessionFactory = sessionFactory;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PatientProgram> search(Condition condition) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientProgram.class, ROOT_ALIAS);
        criteria.add(Restrictions.eq(ROOT_ALIAS + ".voided", false));

        // Eagerly fetch associations used by the handler's toMap() to avoid N+1 queries
        criteria.setFetchMode("patient", FetchMode.JOIN);
        criteria.setFetchMode("program", FetchMode.JOIN);
        criteria.setFetchMode("location", FetchMode.JOIN);

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteriaBuilder.applyCondition(criteria, condition);
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Episode> getEpisodesForPatientProgramIds(Set<Integer> patientProgramIds) {
        if (patientProgramIds.isEmpty()) {
            return Collections.emptyList();
        }
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Episode.class, "ep");
        criteria.createAlias("ep.patientPrograms", "epp");
        criteria.add(Restrictions.eq("ep.voided", false));
        criteria.add(Restrictions.in("epp.patientProgramId", patientProgramIds));

        // Eagerly fetch careManager used by the handler's toEpisodeMap()
        criteria.setFetchMode("careManager", FetchMode.JOIN);
        criteria.setFetchMode("patientPrograms", FetchMode.JOIN);

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }
}
