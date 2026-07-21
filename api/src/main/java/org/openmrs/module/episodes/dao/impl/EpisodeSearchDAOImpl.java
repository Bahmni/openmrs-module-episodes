/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao.impl;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeSearchDAO;
import org.openmrs.module.episodes.search.BuiltQuery;

import java.util.List;
import java.util.Map;

public class EpisodeSearchDAOImpl implements EpisodeSearchDAO {

    private final SessionFactory sessionFactory;

    public EpisodeSearchDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Episode> search(BuiltQuery builtQuery) {
        Query query = sessionFactory.getCurrentSession().createQuery(builtQuery.getHql());
        for (Map.Entry<String, Object> entry : builtQuery.getParameters().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.list();
    }
}
