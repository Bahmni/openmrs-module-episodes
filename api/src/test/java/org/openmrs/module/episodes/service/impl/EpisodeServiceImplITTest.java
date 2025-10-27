package org.openmrs.module.episodes.service.impl;

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
import org.openmrs.module.episodes.EpisodeAttribute;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.EpisodeReason;
import org.openmrs.module.episodes.EpisodeStatusHistory;
import org.openmrs.module.episodes.service.EpisodeAttributeTypeService;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.episodes.service.impl.TestHelper.exampleAttributeTypeInsuranceCaseNumber;
import static org.openmrs.module.episodes.service.impl.TestHelper.exampleAttributeTypeIsAccidentCase;

public class EpisodeServiceImplITTest extends BaseModuleContextSensitiveTest {
    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EpisodeAttributeTypeService attributeTypeService;

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
        Episode episode = createAnEpisodeForPatientWithTypeAndReason(Collections.emptyList());
        assertThat(episode.getId(), is(notNullValue()));
        Episode savedEpisode = episodeService.get(episode.getId());
        assertThat(savedEpisode.getEncounters(), is(notNullValue()));
    }

    @Test
    public void shouldCreateNewEpisodeForPatientWithAttribute() {
        EpisodeAttributeType caseNumberAttributeType = attributeTypeService.save(exampleAttributeTypeInsuranceCaseNumber());
        EpisodeAttributeType accidentCaseAttributeType = attributeTypeService.save(exampleAttributeTypeIsAccidentCase());

        List<EpisodeAttribute> episodeAttributes = Arrays.asList(
                exampleAttributeFromType(caseNumberAttributeType, "CN-1234"),
                exampleAttributeFromType(accidentCaseAttributeType, Boolean.TRUE.toString())
        );
        Episode episode = createAnEpisodeForPatientWithTypeAndReason(episodeAttributes);
        assertEquals(2, episode.getActiveAttributes().size());
        Optional<EpisodeAttribute> caseNumber = episode.getActiveAttributes().stream().filter(e -> e.getValueReference().equals("CN-1234")).findFirst();
        assertTrue(caseNumber.isPresent());
        assertTrue(!caseNumber.get().getUuid().isEmpty());
    }

    private EpisodeAttribute exampleAttributeFromType(EpisodeAttributeType caseNumberAttributeType, String value) {
        EpisodeAttribute attribute = new EpisodeAttribute();
        attribute.setAttributeType(caseNumberAttributeType);
        attribute.setValueReferenceInternal(value);
        return attribute;
    }

    private Episode createAnEpisodeForPatientWithTypeAndReason(List<EpisodeAttribute> episodeAttributes) {
        final Episode episode = new Episode();
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
        episodeAttributes.forEach(attribute -> episode.addAttribute(attribute));
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