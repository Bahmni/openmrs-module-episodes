/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao.impl;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeDAO;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

public class EpisodeDaoTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private EpisodeDAO episodeDAO;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private PatientService patientService;

    @Test
    public void shouldCreateANewEpisode() {
        Episode episode = new Episode();
        episodeDAO.save(episode);
        assertThat(episode.getId(), is(notNullValue()));
        Episode savedEpisode = episodeDAO.get(episode.getId());
        assertThat(savedEpisode.getEncounters(), is(notNullValue()));
    }

    @Test
    public void shouldCreateANewEpisodeWithEncounter() {
        Episode episode = new Episode();
        episode.addEncounter(encounterService.getEncounter(3));
        episode.addEncounter(encounterService.getEncounter(4));
        episode.addEncounter(encounterService.getEncounter(5));
        episodeDAO.save(episode);

        Episode savedEpisode = episodeDAO.get(episode.getId());
        assertThat(savedEpisode.getEncounters().size(), is(3));
    }

    @Test
    public void shouldCreateANewEpisodeWithEncounterAndPatientProgram() {
        Episode episode = new Episode();
        episode.addEncounter(encounterService.getEncounter(3));
        episode.addEncounter(encounterService.getEncounter(4));
        episode.addEncounter(encounterService.getEncounter(5));

        episode.addPatientProgram(programWorkflowService.getPatientProgram(1));
        episodeDAO.save(episode);

        Episode savedEpisode = episodeDAO.get(episode.getId());
        assertThat(savedEpisode.getPatientPrograms().size(), is(1));
    }

    @Test
    public void shouldRetrieveEpisodeForAProgram() {
        Episode episode = new Episode();
        episode.addPatientProgram(programWorkflowService.getPatientProgram(1));
        episodeDAO.save(episode);
        PatientProgram patientProgram = programWorkflowService.getPatientProgram(1);

        Episode episodeForPatientProgram = episodeDAO.getEpisodeForPatientProgram(patientProgram);

        Set<PatientProgram> patientPrograms = episodeForPatientProgram.getPatientPrograms();
        assertThat(patientPrograms.size(), is(equalTo(1)));
        assertThat(patientPrograms.iterator().next().getUuid(), is(equalTo(patientProgram.getUuid())));
    }

    @Test
    public void shouldReturnNullIfEpisodeNotFoundForProgram() {
        PatientProgram patientProgram = programWorkflowService.getPatientProgram(1);
        assertThat(patientProgram, is(notNullValue()));

        Episode episodeForPatientProgram = episodeDAO.getEpisodeForPatientProgram(patientProgram);

        assertThat(episodeForPatientProgram, is(nullValue()));
    }

    @Test (expected = Exception.class)
    public void shouldThrowExceptionIfTransientProgramInstanceUsedToRetrieveEpisode() {
        episodeDAO.getEpisodeForPatientProgram(new PatientProgram());
    }

    @Test
    public void shouldReturnNullIfProgramToFetchEpisodeIsNull() {
        Episode episodeForPatientProgram = episodeDAO.getEpisodeForPatientProgram(null);

        assertThat(episodeForPatientProgram, is(nullValue()));
    }

    @Test
    public void shouldReturnEpisodeForAnEncounter() {
        Encounter encounter = encounterService.getEncounter(3);
        Episode episode = new Episode();
        episode.addEncounter(encounter);
        episode.addPatientProgram(programWorkflowService.getPatientProgram(1));
        episodeDAO.save(episode);

        Episode episodeForEncounter = episodeDAO.getEpisodeForEncounter(encounter);

        assertThat(episodeForEncounter, is(episode));
    }

    @Test
    public void shouldReturnNullIfThereIsNoEpisodeAssociatedWithAnEncounter() {
        Encounter encounter = encounterService.getEncounter(8);

        Episode episodeForEncounter = episodeDAO.getEpisodeForEncounter(encounter);

        assertThat(episodeForEncounter, is(nullValue()));
    }

    @Test
    public void shouldReturnSameEpisodeForMultipleEncountersAssociatedWithIt() {
        Encounter encounter1 = encounterService.getEncounter(3);
        Encounter encounter2 = encounterService.getEncounter(4);
        Episode episode = new Episode();
        episode.addEncounter(encounter1);
        episode.addEncounter(encounter2);
        episode.addPatientProgram(programWorkflowService.getPatientProgram(1));
        episodeDAO.save(episode);

        Episode episodeForEncounter1 = episodeDAO.getEpisodeForEncounter(encounter1);
        Episode episodeForEncounter2 = episodeDAO.getEpisodeForEncounter(encounter2);

        assertThat(episodeForEncounter1, is(episode));
        assertThat(episodeForEncounter2, is(episode));
    }

    @Test
    public void shouldReturnDifferentEpisodeIfTheEncounterIsNotAssociatedWithIt() {
        Encounter encounter1 = encounterService.getEncounter(3);
        Encounter encounter2 = encounterService.getEncounter(4);
        Episode episode = new Episode();
        episode.addEncounter(encounter2);
        episode.addPatientProgram(programWorkflowService.getPatientProgram(1));
        episodeDAO.save(episode);

        Episode episodeForEncounter = episodeDAO.getEpisodeForEncounter(encounter1);

        assertThat(episodeForEncounter, is(not(episode)));
    }

    @Test
    public void shouldReturnEpisodesForGivenPatientsOnly() {
        Patient patient1 = patientService.getPatient(2);
        Patient patient2 = patientService.getPatient(7);
        Patient patient3 = patientService.getPatient(8);
        Episode episode1 = episodeForPatient(patient1);
        Episode episode2 = episodeForPatient(patient2);
        episodeForPatient(patient3);

        List<Episode> episodes = episodeDAO.getEpisodesForPatients(Arrays.asList(patient1, patient2));

        assertThat(episodes, containsInAnyOrder(episode1, episode2));
    }

    @Test
    public void shouldReturnAllEpisodesOfAPatient() {
        Patient patient = patientService.getPatient(2);
        Episode episode1 = episodeForPatient(patient);
        Episode episode2 = episodeForPatient(patient);

        List<Episode> episodes = episodeDAO.getEpisodesForPatients(Collections.singletonList(patient));

        assertThat(episodes, containsInAnyOrder(episode1, episode2));
    }

    @Test
    public void shouldReturnEmptyListIfPatientsHaveNoEpisodes() {
        List<Episode> episodes = episodeDAO.getEpisodesForPatients(
                Collections.singletonList(patientService.getPatient(2)));

        assertThat(episodes, is(empty()));
    }

    @Test
    public void shouldReturnEmptyListIfPatientsAreNullOrEmpty() {
        assertThat(episodeDAO.getEpisodesForPatients(null), is(empty()));
        assertThat(episodeDAO.getEpisodesForPatients(Collections.<Patient>emptyList()), is(empty()));
    }

    private Episode episodeForPatient(Patient patient) {
        Episode episode = new Episode();
        episode.setPatient(patient);
        episodeDAO.save(episode);
        return episode;
    }
}