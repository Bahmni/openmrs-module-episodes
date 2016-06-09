package org.openmrs.module.episodes.service.impl;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.impl.EpisodeDAO;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class EpisodeServiceImpl implements EpisodeService {
    @Autowired
    private EpisodeDAO episodeDAO;

    @Override
    public void save(Episode episode) {
        episodeDAO.save(episode);
    }

    @Override
    public Episode get(Integer episodeId) {
        return episodeDAO.get(episodeId);
    }

    @Override
    public Episode getEpisodeForPatientProgram(PatientProgram patientProgram) {
        return episodeDAO.getEpisodeForPatientProgram(patientProgram);
    }

    @Override
    public Episode getEpisodeForEncounter(Encounter encounter) {
        return episodeDAO.getEpisodeForEncounter(encounter);
    }
}
