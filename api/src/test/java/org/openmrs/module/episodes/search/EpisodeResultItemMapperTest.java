package org.openmrs.module.episodes.search;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.EpisodeAttribute;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.EpisodeStatusHistory;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EpisodeResultItemMapperTest {

    private final EpisodeResultItemMapper mapper = new EpisodeResultItemMapper();

    @Test
    public void shouldMapTheBasicEpisodeFieldsOntoTheResultItem() {
        Episode episode = new Episode();
        episode.setUuid("episode-uuid");
        episode.setStatus(Episode.Status.ACTIVE);
        Date startDate = new Date();
        Date endDate = new Date();
        episode.setDateStarted(startDate);
        episode.setDateEnded(endDate);

        EpisodeResultItem item = mapper.map(episode);

        assertEquals("episode-uuid", item.getUuid());
        assertEquals("ACTIVE", item.getStatus());
        assertEquals(startDate, item.getEnrollmentDate());
        assertEquals(endDate, item.getCompletionDate());
    }

    @Test
    public void shouldReturnNullStatusWhenTheEpisodeHasNoStatus() {
        Episode episode = new Episode();
        episode.setStatus(null);

        EpisodeResultItem item = mapper.map(episode);

        assertNull(item.getStatus());
    }

    @Test
    public void shouldMapTheFirstPatientProgramsProgramAsTheProgramme() {
        Episode episode = new Episode();
        Program program = new Program();
        program.setUuid("program-uuid");
        program.setName("HA");
        program.setDescription("Home Assistance");
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setProgram(program);
        episode.addPatientProgram(patientProgram);

        EpisodeResultItem item = mapper.map(episode);

        assertEquals("program-uuid", item.getProgramme().getUuid());
        assertEquals("HA", item.getProgramme().getName());
        assertEquals("Home Assistance", item.getProgramme().getDescription());
    }

    @Test
    public void shouldReturnNullProgrammeWhenThereAreNoPatientPrograms() {
        Episode episode = new Episode();

        EpisodeResultItem item = mapper.map(episode);

        assertNull(item.getProgramme());
    }

    @Test
    public void shouldMapTheFirstPatientProgramsLocation() {
        Episode episode = new Episode();
        Location location = new Location();
        location.setUuid("location-uuid");
        location.setName("MHAC-1");
        PatientProgram patientProgram = new PatientProgram();
        patientProgram.setLocation(location);
        episode.addPatientProgram(patientProgram);

        EpisodeResultItem item = mapper.map(episode);

        assertEquals("location-uuid", item.getLocation().getUuid());
        assertEquals("MHAC-1", item.getLocation().getName());
    }

    @Test
    public void shouldMapEachStatusHistoryEntryToAWorkflowState() {
        Episode episode = new Episode();
        EpisodeStatusHistory history = new EpisodeStatusHistory();
        history.setStatus(Episode.Status.ONHOLD);
        Date started = new Date();
        Date ended = new Date();
        history.setDateStarted(started);
        history.setDateEnded(ended);
        episode.addEpisodeStatusHistory(history);

        EpisodeResultItem item = mapper.map(episode);

        assertEquals(1, item.getWorkflowStates().size());
        EpisodeResultItem.WorkflowState workflowState = item.getWorkflowStates().get(0);
        assertEquals("ONHOLD", workflowState.getState());
        assertEquals(started, workflowState.getStartDate());
        assertEquals(ended, workflowState.getEndDate());
    }

    @Test
    public void shouldReturnAnEmptyWorkflowStateListWhenThereIsNoStatusHistory() {
        Episode episode = new Episode();

        EpisodeResultItem item = mapper.map(episode);

        assertTrue(item.getWorkflowStates().isEmpty());
    }

    @Test
    public void shouldMapTheDestinationCountryAttributeByAttributeTypeName() {
        Episode episode = new Episode();
        EpisodeAttributeType destinationCountryType = new EpisodeAttributeType();
        destinationCountryType.setName("Destination Country");
        EpisodeAttribute attribute = new EpisodeAttribute();
        attribute.setAttributeType(destinationCountryType);
        attribute.setValueReferenceInternal("New Zealand");
        episode.addAttribute(attribute);

        EpisodeResultItem item = mapper.map(episode);

        assertEquals("New Zealand", item.getDestinationCountry());
    }

    @Test
    public void shouldReturnNullDestinationCountryWhenNoMatchingAttributeExists() {
        Episode episode = new Episode();
        EpisodeAttributeType otherType = new EpisodeAttributeType();
        otherType.setName("Identifier Type Source");
        EpisodeAttribute attribute = new EpisodeAttribute();
        attribute.setAttributeType(otherType);
        attribute.setValueReferenceInternal("some-source");
        episode.addAttribute(attribute);

        EpisodeResultItem item = mapper.map(episode);

        assertNull(item.getDestinationCountry());
    }

    @Test
    public void shouldMapPatientPersonAndIdentifierDetails() {
        Episode episode = new Episode();
        Patient patient = new Patient();
        patient.setUuid("patient-uuid");
        PersonName personName = new PersonName();
        personName.setGivenName("John");
        personName.setFamilyName("Doe");
        patient.addName(personName);
        patient.setGender("M");
        patient.setBirthdate(new Date());
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid("identifier-type-uuid");
        identifierType.setName("UMI");
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("UMI-12345");
        identifier.setIdentifierType(identifierType);
        identifier.setPreferred(true);
        patient.addIdentifier(identifier);
        episode.setPatient(patient);

        EpisodeResultItem item = mapper.map(episode);

        assertEquals("patient-uuid", item.getPatient().getUuid());
        assertEquals("John", item.getPatient().getPerson().getGivenName());
        assertEquals("Doe", item.getPatient().getPerson().getFamilyName());
        assertEquals("M", item.getPatient().getPerson().getGender());
        assertEquals(1, item.getPatient().getIdentifiers().size());
        EpisodeResultItem.Identifier mappedIdentifier = item.getPatient().getIdentifiers().get(0);
        assertEquals("UMI-12345", mappedIdentifier.getIdentifier());
        assertTrue(mappedIdentifier.isPreferred());
        assertEquals("UMI", mappedIdentifier.getType().getName());
    }

    @Test
    public void shouldReturnNullPatientWhenTheEpisodeHasNoPatient() {
        Episode episode = new Episode();
        episode.setPatient(null);

        EpisodeResultItem item = mapper.map(episode);

        assertNull(item.getPatient());
    }

    @Test
    public void shouldReturnAnEmptyIdentifierListWhenThePatientHasNoIdentifiers() {
        Episode episode = new Episode();
        Patient patient = new Patient();
        patient.setIdentifiers(Collections.emptySet());
        episode.setPatient(patient);

        EpisodeResultItem item = mapper.map(episode);

        assertTrue(item.getPatient().getIdentifiers().isEmpty());
    }
}
