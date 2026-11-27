/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeDAO;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeServiceImplTest {

    @InjectMocks
    private EpisodeServiceImpl episodeService;

    @Mock
    EpisodeDAO episodeDAO;

    @Test
    public void shouldGetEncounterForAnEpisode() {
        Encounter encounter = new Encounter();

        episodeService.getEpisodeForEncounter(encounter);

        verify(episodeDAO, times(1)).getEpisodeForEncounter(encounter);
    }

    @Test
    public void shouldGetEpisodesForPatients() {
        List<Patient> patients = Arrays.asList(new Patient(), new Patient());
        List<Episode> episodes = Arrays.asList(new Episode());
        when(episodeDAO.getEpisodesForPatients(patients)).thenReturn(episodes);

        List<Episode> result = episodeService.getEpisodesForPatients(patients);

        assertSame(episodes, result);
        verify(episodeDAO, times(1)).getEpisodesForPatients(patients);
    }
}