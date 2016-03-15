package org.openmrs.module.episodes.service;

import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;

public interface EpisodeService {
    void save(Episode episode);

    Episode get(Integer episodeId);

    Episode getEpisodeForPatientProgram(PatientProgram patientProgram);
}
