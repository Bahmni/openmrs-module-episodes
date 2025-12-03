package org.openmrs.module.episodes.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.dao.EpisodeAttributeTypeDao;
import org.springframework.stereotype.Repository;

import java.util.List;

public class EpisodeAttributeTypeDaoImpl implements EpisodeAttributeTypeDao {

    private static final String HQL_SELECT_ALL_EPISODE_ATTRIBUTE_TYPE = "FROM EpisodeAttributeType";
    private static final String HQL_SELECT_UN_RETIRED_EPISODE_ATTRIBUTE_TYPE = "FROM EpisodeAttributeType WHERE retired = false";
    private static final String HQL_SELECT_EPISODE_ATTRIBUTE_TYPE_BY_UUID = "FROM EpisodeAttributeType WHERE uuid = :uuid";
    private static final String HQL_SELECT_EPISODE_ATTRIBUTE_TYPE_BY_NAME = "FROM EpisodeAttributeType WHERE UPPER(name) LIKE :pattern";


    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public EpisodeAttributeTypeDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<EpisodeAttributeType> getAllAttributeTypes(boolean includeRetired) {
        String hql = includeRetired ? HQL_SELECT_ALL_EPISODE_ATTRIBUTE_TYPE : HQL_SELECT_UN_RETIRED_EPISODE_ATTRIBUTE_TYPE;
        Query<EpisodeAttributeType> episodeAttributeTypeQuery = sessionFactory.getCurrentSession().createQuery(hql, EpisodeAttributeType.class);
        return episodeAttributeTypeQuery.list();
    }

    @Override
    public EpisodeAttributeType getAttributeTypeByUuid(String uuid) {
        Query<EpisodeAttributeType> episodeAttributeTypeQuery = sessionFactory.getCurrentSession().createQuery(HQL_SELECT_EPISODE_ATTRIBUTE_TYPE_BY_UUID, EpisodeAttributeType.class);
        episodeAttributeTypeQuery.setParameter("uuid", uuid);
        List<EpisodeAttributeType> episodeAttributeTypes = episodeAttributeTypeQuery.list();
        return !episodeAttributeTypes.isEmpty() ? episodeAttributeTypes.get(0) : null;
    }

    @Override
    public EpisodeAttributeType getAttributeTypeById(Integer id) {
        return sessionFactory.getCurrentSession().get(EpisodeAttributeType.class, id);
    }

    @Override
    public EpisodeAttributeType save(EpisodeAttributeType attributeType) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(attributeType);
        return attributeType;
    }

    @Override
    public List<EpisodeAttributeType> findByName(String name) {
        Query<EpisodeAttributeType> episodeAttributeTypeQuery = sessionFactory.getCurrentSession().createQuery(HQL_SELECT_EPISODE_ATTRIBUTE_TYPE_BY_NAME, EpisodeAttributeType.class);
        String likePattern = (name + "%").toUpperCase();
        episodeAttributeTypeQuery.setParameter("pattern", likePattern);
        return episodeAttributeTypeQuery.list();
    }
}
