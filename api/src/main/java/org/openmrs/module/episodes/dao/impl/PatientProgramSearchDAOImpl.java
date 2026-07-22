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
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.query.PatientProgramQueryBuilder;
import org.openmrs.module.episodes.search.criteria.Condition;

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
    @SuppressWarnings("unchecked")
    public List<PatientProgram> search(Condition criteria) {
        Criteria c = sessionFactory.getCurrentSession().createCriteria(PatientProgram.class, "pp");
        c.add(Restrictions.eq("pp.voided", false));
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        queryBuilder.applyCondition(c, criteria);
        return c.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Episode> getEpisodesForPatientProgramIds(Set<Integer> patientProgramIds) {
        if (patientProgramIds.isEmpty()) {
            return Collections.emptyList();
        }
        Criteria c = sessionFactory.getCurrentSession().createCriteria(Episode.class, "ep");
        c.createAlias("ep.patientPrograms", "epp");
        c.add(Restrictions.eq("ep.voided", false));
        c.add(Restrictions.in("epp.patientProgramId", patientProgramIds));
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return c.list();
    }
}
