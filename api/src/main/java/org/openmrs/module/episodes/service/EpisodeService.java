package org.openmrs.module.episodes.service;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.episodes.Episode;

import java.util.List;

public interface EpisodeService {
    void save(Episode episode);

    Episode get(Integer episodeId);

    @Authorized({"Get Episodes"})
    Episode getEpisodeByUuid(String uuid);

    Episode getEpisodeForPatientProgram(PatientProgram patientProgram);

    Episode getEpisodeForEncounter(Encounter encounter);

    List<Episode> getAllEpisodes();
}
