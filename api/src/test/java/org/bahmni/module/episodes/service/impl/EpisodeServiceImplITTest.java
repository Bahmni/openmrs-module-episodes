package org.bahmni.module.episodes.service.impl;

import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class EpisodeServiceImplITTest extends BaseModuleContextSensitiveTest {
    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private EncounterService encounterService;

    @Test
    public void shouldCreateANewEpisode() {
        Episode episode = createAnEpisode();
        assertThat(episode.getId(), is(notNullValue()));
        Episode savedEpisode = episodeService.get(episode.getId());
        assertThat(savedEpisode.getEncounters(), is(notNullValue()));
    }

    @Test
    public void shouldRetrieveEpisodeForAProgram() {
        createAnEpisode();
        PatientProgram patientProgram = testPatientProgram();

        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(patientProgram);

        Set<PatientProgram> patientPrograms = episodeForPatientProgram.getPatientPrograms();
        assertThat(patientPrograms.size(), is(equalTo(1)));
        assertThat(patientPrograms.iterator().next().getUuid(), is(equalTo(patientProgram.getUuid())));
    }

    @Test
    public void shouldReturnNullIfPatientProgramIsNotLinkedToAnEpisode() {
        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(testPatientProgram());

        assertThat(episodeForPatientProgram, is(nullValue()));
    }

    @Test
    public void shouldReturnNullEpisodeIfPatientProgramIsNull() {
        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(null);

        assertThat(episodeForPatientProgram, is(nullValue()));
    }

    @Test
    public void shouldReturnEpisodeRelatedToAnEncounter() {
        Episode episode = new Episode();
        Encounter encounter = encounterService.getEncounter(3);
        episode.addEncounter(encounter);
        episode.addPatientProgram(testPatientProgram());
        episodeService.save(episode);

        Episode episodeForEncounter = episodeService.getEpisodeForEncounter(encounter);

        assertThat(episodeForEncounter, is(episode));
    }

    private Episode createAnEpisode() {
        Episode episode = new Episode();
        episode.addPatientProgram(testPatientProgram());
        episodeService.save(episode);
        return episode;
    }

    private PatientProgram testPatientProgram() {
        return programWorkflowService.getPatientProgram(1);
    }
}