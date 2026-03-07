/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.service.impl;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeDAO;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class EpisodeServiceImpl implements EpisodeService {

    private EpisodeDAO episodeDAO;

    public EpisodeServiceImpl(EpisodeDAO episodeDAO) {
        this.episodeDAO = episodeDAO;
    }

    @Override
    public void save(Episode episode) {
        episodeDAO.save(episode);
    }

    @Override
    public Episode get(Integer episodeId) {
        return episodeDAO.get(episodeId);
    }

    @Override
    public Episode getEpisodeByUuid(String uuid) {
        return episodeDAO.getEpisodeByUuid(uuid);
    }

    @Override
    public Episode getEpisodeForPatientProgram(PatientProgram patientProgram) {
        return episodeDAO.getEpisodeForPatientProgram(patientProgram);
    }

    @Override
    public Episode getEpisodeForEncounter(Encounter encounter) {
        return episodeDAO.getEpisodeForEncounter(encounter);
    }

    @Override
    public List<Episode> getAllEpisodes() {
        return episodeDAO.getAllEpisodes();
    }
}
