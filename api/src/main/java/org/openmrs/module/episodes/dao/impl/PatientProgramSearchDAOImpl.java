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
import org.hibernate.sql.JoinType;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder;
import org.openmrs.module.episodes.search.model.SearchCriteria;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder.ROOT_ALIAS;
import static org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder.PATIENT_PROGRAM_ALIAS;
import static org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder.PATIENT_ALIAS;

public class PatientProgramSearchDAOImpl implements PatientProgramSearchDAO {

    private final SessionFactory sessionFactory;
    private final PatientProgramCriteriaBuilder criteriaBuilder;

    public PatientProgramSearchDAOImpl(SessionFactory sessionFactory,
            PatientProgramCriteriaBuilder criteriaBuilder) {
        this.sessionFactory = sessionFactory;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Episode> search(SearchCriteria searchCriteria) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Episode.class, ROOT_ALIAS);
        criteria.add(Restrictions.eq(ROOT_ALIAS + ".voided", false));

        criteria.createAlias(ROOT_ALIAS + ".patientPrograms", PATIENT_PROGRAM_ALIAS, JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq(PATIENT_PROGRAM_ALIAS + ".voided", false));

        criteria.createAlias(PATIENT_PROGRAM_ALIAS + ".patient", PATIENT_ALIAS, JoinType.INNER_JOIN);

        criteria.setFetchMode("careManager", FetchMode.JOIN);
        criteria.setFetchMode("patientPrograms", FetchMode.JOIN);

        Set<String> preCreatedAliases = new HashSet<>(Arrays.asList(PATIENT_ALIAS));

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteriaBuilder.applyCondition(criteria, searchCriteria, preCreatedAliases);
        return criteria.list();
    }
}
