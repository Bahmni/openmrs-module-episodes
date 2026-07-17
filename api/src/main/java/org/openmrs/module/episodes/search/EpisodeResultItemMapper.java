package org.openmrs.module.episodes.search;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.EpisodeAttribute;
import org.openmrs.module.episodes.EpisodeStatusHistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EpisodeResultItemMapper {

    private static final String DESTINATION_COUNTRY_ATTRIBUTE_TYPE = "Destination Country";

    public EpisodeResultItem map(Episode episode) {
        EpisodeResultItem item = new EpisodeResultItem();
        item.setUuid(episode.getUuid());
        item.setProgramme(mapProgramme(episode));
        item.setStatus(mapStatus(episode));
        item.setEnrollmentDate(episode.getDateStarted());
        item.setCompletionDate(episode.getDateEnded());
        item.setPatient(mapPatient(episode.getPatient()));
        item.setWorkflowStates(mapWorkflowStates(episode));
        item.setLocation(mapLocation(episode));
        item.setDestinationCountry(mapDestinationCountry(episode));
        return item;
    }

    private String mapStatus(Episode episode) {
        Episode.Status status = episode.getStatus();
        return status == null ? null : status.name();
    }

    private PatientProgram firstPatientProgram(Episode episode) {
        Set<PatientProgram> patientPrograms = episode.getPatientPrograms();
        return (patientPrograms == null || patientPrograms.isEmpty()) ? null : patientPrograms.iterator().next();
    }

    private EpisodeResultItem.Programme mapProgramme(Episode episode) {
        PatientProgram patientProgram = firstPatientProgram(episode);
        Program program = patientProgram == null ? null : patientProgram.getProgram();
        if (program == null) {
            return null;
        }
        EpisodeResultItem.Programme programme = new EpisodeResultItem.Programme();
        programme.setUuid(program.getUuid());
        programme.setName(program.getName());
        programme.setDescription(program.getDescription());
        return programme;
    }

    private EpisodeResultItem.Location mapLocation(Episode episode) {
        PatientProgram patientProgram = firstPatientProgram(episode);
        Location omrsLocation = patientProgram == null ? null : patientProgram.getLocation();
        if (omrsLocation == null) {
            return null;
        }
        EpisodeResultItem.Location location = new EpisodeResultItem.Location();
        location.setUuid(omrsLocation.getUuid());
        location.setName(omrsLocation.getName());
        return location;
    }

    private List<EpisodeResultItem.WorkflowState> mapWorkflowStates(Episode episode) {
        Set<EpisodeStatusHistory> statusHistory = episode.getStatusHistory();
        if (statusHistory == null || statusHistory.isEmpty()) {
            return Collections.emptyList();
        }
        List<EpisodeResultItem.WorkflowState> workflowStates = new ArrayList<>();
        for (EpisodeStatusHistory history : statusHistory) {
            workflowStates.add(mapWorkflowState(history));
        }
        return workflowStates;
    }

    private EpisodeResultItem.WorkflowState mapWorkflowState(EpisodeStatusHistory history) {
        EpisodeResultItem.WorkflowState workflowState = new EpisodeResultItem.WorkflowState();
        workflowState.setState(history.getStatus() == null ? null : history.getStatus().name());
        workflowState.setStartDate(history.getDateStarted());
        workflowState.setEndDate(history.getDateEnded());
        return workflowState;
    }

    private String mapDestinationCountry(Episode episode) {
        EpisodeAttribute attribute = findAttributeByTypeName(episode, DESTINATION_COUNTRY_ATTRIBUTE_TYPE);
        return attribute == null ? null : attribute.getValueReference();
    }

    private EpisodeAttribute findAttributeByTypeName(Episode episode, String attributeTypeName) {
        for (EpisodeAttribute attribute : episode.getActiveAttributes()) {
            if (attribute.getAttributeType() != null && attributeTypeName.equals(attribute.getAttributeType().getName())) {
                return attribute;
            }
        }
        return null;
    }

    private EpisodeResultItem.Patient mapPatient(Patient patient) {
        if (patient == null) {
            return null;
        }
        EpisodeResultItem.Patient result = new EpisodeResultItem.Patient();
        result.setUuid(patient.getUuid());
        result.setPerson(mapPerson(patient));
        result.setIdentifiers(mapIdentifiers(patient));
        return result;
    }

    private EpisodeResultItem.Person mapPerson(Patient patient) {
        EpisodeResultItem.Person person = new EpisodeResultItem.Person();
        person.setGivenName(patient.getGivenName());
        person.setFamilyName(patient.getFamilyName());
        person.setFullName(mapFullName(patient));
        person.setGender(patient.getGender());
        person.setBirthdate(patient.getBirthdate());
        person.setAge(patient.getAge());
        return person;
    }

    private String mapFullName(Patient patient) {
        PersonName personName = patient.getPersonName();
        return personName == null ? null : personName.getFullName();
    }

    private List<EpisodeResultItem.Identifier> mapIdentifiers(Patient patient) {
        Set<PatientIdentifier> identifiers = patient.getIdentifiers();
        if (identifiers == null || identifiers.isEmpty()) {
            return Collections.emptyList();
        }
        List<EpisodeResultItem.Identifier> result = new ArrayList<>();
        for (PatientIdentifier identifier : identifiers) {
            result.add(mapIdentifier(identifier));
        }
        return result;
    }

    private EpisodeResultItem.Identifier mapIdentifier(PatientIdentifier patientIdentifier) {
        EpisodeResultItem.Identifier identifier = new EpisodeResultItem.Identifier();
        identifier.setIdentifier(patientIdentifier.getIdentifier());
        identifier.setPreferred(Boolean.TRUE.equals(patientIdentifier.getPreferred()));
        identifier.setType(mapIdentifierType(patientIdentifier.getIdentifierType()));
        return identifier;
    }

    private EpisodeResultItem.IdentifierType mapIdentifierType(PatientIdentifierType patientIdentifierType) {
        if (patientIdentifierType == null) {
            return null;
        }
        EpisodeResultItem.IdentifierType identifierType = new EpisodeResultItem.IdentifierType();
        identifierType.setUuid(patientIdentifierType.getUuid());
        identifierType.setName(patientIdentifierType.getName());
        return identifierType;
    }
}
