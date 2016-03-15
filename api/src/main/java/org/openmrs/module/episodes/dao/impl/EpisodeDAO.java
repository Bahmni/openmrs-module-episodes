package org.openmrs.module.episodes.dao.impl;

import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;

public interface EpisodeDAO {
    public void save(Episode episode);

    public Episode get(Integer episodeId);

    public Episode getEpisodeForPatientProgram(PatientProgram patientProgram);
}
