/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao;

import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;

import java.util.List;

public interface EpisodeDAO {
    void save(Episode episode);

    Episode get(Integer episodeId);

    Episode getEpisodeByUuid(String uuid);

    Episode getEpisodeForPatientProgram(PatientProgram patientProgram);

    Episode getEpisodeForEncounter(Encounter encounter);

    List<Episode> getAllEpisodes();
}
