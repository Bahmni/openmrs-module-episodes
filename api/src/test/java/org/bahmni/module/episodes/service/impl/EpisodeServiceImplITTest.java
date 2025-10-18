package org.bahmni.module.episodes.service.impl;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.EpisodeReason;
import org.openmrs.module.episodes.EpisodeStatusHistory;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Locale;
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

    @Autowired
    private ConceptService conceptService;

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

    @Test
    public void shouldCreateANewEpisodeForPatient() {
        Episode episode = createAnEpisodeForPatientWithTypeAndReason();
        assertThat(episode.getId(), is(notNullValue()));
        Episode savedEpisode = episodeService.get(episode.getId());
        assertThat(savedEpisode.getEncounters(), is(notNullValue()));
    }

    private Episode createAnEpisodeForPatientWithTypeAndReason() {
        Episode episode = new Episode();
        episode.setPatient(new Patient());
        episode.setDateCreated(new Date());
        episode.setStatus(Episode.Status.ACTIVE);
        episode.setConcept(createConcept("hospitalization"));
        EpisodeReason reason = new EpisodeReason();
        episode.addEpisodeReason(reason);
        reason.setValueConcept(createConcept("accident"));
        EpisodeStatusHistory statusHistory = new EpisodeStatusHistory();
        statusHistory.setStatus(Episode.Status.ACTIVE);
        statusHistory.setDateStarted(new Date());
        episode.addEpisodeStatusHistory(statusHistory);
        episodeService.save(episode);
        return episode;
    }

    private ConceptClass findConceptClass(String className) {
        return conceptService.getConceptClassByName(className);
    }

    private Concept createConcept(String name) {
        Concept concept = new Concept();
        ConceptName cn = new ConceptName();
        cn.setName(name);
        cn.setLocale(Locale.ENGLISH);
        ConceptDatatype naDataType = conceptService.getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID);
        ConceptClass miscClass = findConceptClass("Misc");
        concept.addName(cn);
        concept.setConceptClass(miscClass);
        concept.setDatatype(naDataType);
        return conceptService.saveConcept(concept);
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