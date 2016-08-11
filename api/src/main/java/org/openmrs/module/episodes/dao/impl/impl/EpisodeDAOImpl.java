package org.openmrs.module.episodes.dao.impl.impl;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.impl.EpisodeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EpisodeDAOImpl implements EpisodeDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Episode episode) {
        session().save(episode);
    }

    @Override
    public Episode get(Integer episodeId) {
        return (Episode) session().get(Episode.class, episodeId);
    }

    @Override
    public Episode getEpisodeForPatientProgram(PatientProgram patientProgram) {
        return (Episode) session().createQuery(
                "SELECT e FROM Episode e " +
                        "INNER JOIN e.patientPrograms pp " +
                        "WHERE pp = :patientProgram")
                .setParameter("patientProgram", patientProgram)
                .uniqueResult();
    }

    @Override
    public Episode getEpisodeForEncounter(Encounter encounter) {
        return (Episode) session().createQuery(
                "SELECT e FROM Episode e " +
                        "INNER JOIN e.encounters ee " +
                        "WHERE ee = :encounter")
                .setParameter("encounter", encounter)
                .uniqueResult();
    }

    @Override
    public List<Episode> getAllEpisodes() {
        return session().createCriteria(Episode.class).list();
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }
}
