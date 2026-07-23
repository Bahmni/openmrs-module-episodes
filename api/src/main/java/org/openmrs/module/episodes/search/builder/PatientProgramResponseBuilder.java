/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Provider;
import org.openmrs.attribute.Attribute;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.constants.ResponseKeyConstants;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatientProgramResponseBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

    public Map<String, Object> mapPatientProgram(PatientProgram patientProgram, Map<String, Object> episode) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, patientProgram.getUuid());
        map.put(ResponseKeyConstants.ENROLLMENT_DATE_ENROLLED, formatDate(patientProgram.getDateEnrolled()));
        map.put(ResponseKeyConstants.ENROLLMENT_DATE_COMPLETED, formatDate(patientProgram.getDateCompleted()));
        map.put(ResponseKeyConstants.ENROLLMENT_PATIENT, buildPatientMap(patientProgram.getPatient()));
        map.put(ResponseKeyConstants.ENROLLMENT_PROGRAM, buildProgramMap(patientProgram.getProgram()));
        map.put(ResponseKeyConstants.ENROLLMENT_LOCATION, buildLocationMap(patientProgram.getLocation()));
        map.put(ResponseKeyConstants.ENROLLMENT_ATTRIBUTES, buildAttributesList(patientProgram));
        map.put(ResponseKeyConstants.ENROLLMENT_CURRENT_STATE, buildCurrentStateMap(patientProgram));
        map.put(ResponseKeyConstants.ENROLLMENT_EPISODE, episode);
        return map;
    }

    public Map<String, Object> mapEpisode(Episode episode) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, episode.getUuid());
        map.put(ResponseKeyConstants.EPISODE_STATUS, episode.getStatus() != null ? episode.getStatus().name() : null);
        map.put(ResponseKeyConstants.EPISODE_DATE_STARTED, formatDate(episode.getDateStarted()));
        map.put(ResponseKeyConstants.EPISODE_DATE_ENDED, formatDate(episode.getDateEnded()));
        map.put(ResponseKeyConstants.EPISODE_CARE_MANAGER, buildCareManagerMap(episode.getCareManager()));
        return map;
    }

    private Map<String, Object> buildPatientMap(Patient patient) {
        if (patient == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, patient.getUuid());
        map.put(ResponseKeyConstants.PATIENT_GENDER, patient.getGender());
        map.put(ResponseKeyConstants.PATIENT_BIRTHDATE, formatDate(patient.getBirthdate()));
        map.put(ResponseKeyConstants.COMMON_VOIDED, patient.getVoided());
        map.put(ResponseKeyConstants.COMMON_NAME, buildPersonNameMap(patient.getPersonName()));
        map.put(ResponseKeyConstants.PATIENT_IDENTIFIERS, buildIdentifiersList(patient));
        return map;
    }

    private Map<String, Object> buildPersonNameMap(PersonName name) {
        if (name == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.PERSON_GIVEN_NAME, name.getGivenName());
        map.put(ResponseKeyConstants.PERSON_MIDDLE_NAME, name.getMiddleName());
        map.put(ResponseKeyConstants.PERSON_FAMILY_NAME, name.getFamilyName());
        map.put(ResponseKeyConstants.PERSON_FAMILY_NAME2, name.getFamilyName2());
        map.put(ResponseKeyConstants.COMMON_VOIDED, name.getVoided());
        return map;
    }

    private List<Map<String, Object>> buildIdentifiersList(Patient patient) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PatientIdentifier pi : patient.getActiveIdentifiers()) {
            Map<String, Object> idMap = new LinkedHashMap<>();
            idMap.put(ResponseKeyConstants.COMMON_UUID, pi.getUuid());
            idMap.put(ResponseKeyConstants.IDENTIFIER_VALUE, pi.getIdentifier());
            idMap.put(ResponseKeyConstants.IDENTIFIER_PREFERRED, Boolean.TRUE.equals(pi.getPreferred()));
            if (pi.getIdentifierType() != null) {
                idMap.put(ResponseKeyConstants.COMMON_DISPLAY, pi.getIdentifierType().getName() + " = " + pi.getIdentifier());
                idMap.put(ResponseKeyConstants.IDENTIFIER_TYPE, buildReferenceMap(pi.getIdentifierType().getUuid(), pi.getIdentifierType().getName()));
            }
            list.add(idMap);
        }
        return list;
    }

    private Map<String, Object> buildProgramMap(Program program) {
        if (program == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, program.getUuid());
        map.put(ResponseKeyConstants.COMMON_NAME, program.getName());
        map.put(ResponseKeyConstants.PROGRAM_RETIRED, program.getRetired());
        map.put(ResponseKeyConstants.PROGRAM_DESCRIPTION, program.getDescription());
        map.put(ResponseKeyConstants.COMMON_CONCEPT, buildConceptRef(program.getConcept()));
        return map;
    }

    private Map<String, Object> buildLocationMap(Location location) {
        if (location == null) return null;
        return buildReferenceMap(location.getUuid(), location.getName());
    }

    private List<Map<String, Object>> buildAttributesList(PatientProgram patientProgram) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Attribute<?, ?> attr : patientProgram.getActiveAttributes()) {
            Map<String, Object> attrMap = new LinkedHashMap<>();
            attrMap.put(ResponseKeyConstants.COMMON_UUID, attr.getUuid());
            attrMap.put(ResponseKeyConstants.ATTRIBUTE_VALUE, attr.getValueReference());
            attrMap.put(ResponseKeyConstants.ATTRIBUTE_TYPE, buildReferenceMap(attr.getAttributeType().getUuid(), attr.getAttributeType().getName()));
            list.add(attrMap);
        }
        return list;
    }

    private Map<String, Object> buildCurrentStateMap(PatientProgram patientProgram) {
        PatientState current = selectCurrentState(patientProgram.getStates());
        if (current == null) return null;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, current.getUuid());
        map.put(ResponseKeyConstants.STATE_START_DATE, formatDate(current.getStartDate()));
        map.put(ResponseKeyConstants.STATE_END_DATE, formatDate(current.getEndDate()));

        ProgramWorkflowState wfState = current.getState();
        if (wfState != null) {
            map.put(ResponseKeyConstants.STATE_STATE, buildWorkflowStateMap(wfState));
            ProgramWorkflow wf = wfState.getProgramWorkflow();
            if (wf != null) {
                map.put(ResponseKeyConstants.STATE_WORKFLOW, buildWorkflowMap(wf));
            }
        }
        return map;
    }

    private Map<String, Object> buildWorkflowStateMap(ProgramWorkflowState wfState) {
        Map<String, Object> stateMap = new LinkedHashMap<>();
        stateMap.put(ResponseKeyConstants.COMMON_UUID, wfState.getUuid());
        stateMap.put(ResponseKeyConstants.COMMON_CONCEPT, buildConceptRef(wfState.getConcept()));
        stateMap.put(ResponseKeyConstants.STATE_INITIAL, Boolean.TRUE.equals(wfState.getInitial()));
        stateMap.put(ResponseKeyConstants.STATE_TERMINAL, Boolean.TRUE.equals(wfState.getTerminal()));
        return stateMap;
    }

    private Map<String, Object> buildWorkflowMap(ProgramWorkflow wf) {
        Map<String, Object> wfMap = new LinkedHashMap<>();
        wfMap.put(ResponseKeyConstants.COMMON_UUID, wf.getUuid());
        wfMap.put(ResponseKeyConstants.COMMON_CONCEPT, buildConceptRef(wf.getConcept()));
        return wfMap;
    }

    private Map<String, Object> buildCareManagerMap(Provider provider) {
        if (provider == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, provider.getUuid());
        map.put(ResponseKeyConstants.COMMON_DISPLAY, provider.getName());
        map.put(ResponseKeyConstants.IDENTIFIER_VALUE, provider.getIdentifier());
        if (provider.getPerson() != null) {
            map.put(ResponseKeyConstants.PROVIDER_PERSON, buildPersonNameMap(provider.getPerson().getPersonName()));
        }
        return map;
    }

    private Map<String, Object> buildConceptRef(Concept concept) {
        if (concept == null) return null;
        String conceptName = concept.getName() != null ? concept.getName().getName() : null;
        return buildReferenceMap(concept.getUuid(), conceptName);
    }

    private Map<String, Object> buildReferenceMap(String uuid, String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ResponseKeyConstants.COMMON_UUID, uuid);
        if (name != null) map.put(ResponseKeyConstants.COMMON_NAME, name);
        return map;
    }

    /**
     * Selects the "current" state from a collection of patient states.
     * Prefers active (no end date) states; falls back to the latest ended state.
     */
    public static PatientState selectCurrentState(Collection<PatientState> states) {
        List<PatientState> active = new ArrayList<>();
        List<PatientState> ended = new ArrayList<>();
        for (PatientState state : states) {
            if (state.getVoided()) continue;
            if (state.getEndDate() == null) active.add(state);
            else ended.add(state);
        }
        return active.isEmpty() ? findLatestByEndDate(ended) : findLatestByStartDate(active);
    }

    private static PatientState findLatestByStartDate(List<PatientState> states) {
        PatientState latest = null;
        for (PatientState state : states) {
            if (state.getStartDate() == null) continue;
            if (latest == null || state.getStartDate().after(latest.getStartDate())) {
                latest = state;
            }
        }
        return latest != null ? latest : states.get(0);
    }

    private static PatientState findLatestByEndDate(List<PatientState> states) {
        if (states.isEmpty()) return null;
        PatientState latest = states.get(0);
        for (PatientState state : states) {
            if (state.getEndDate().after(latest.getEndDate())) latest = state;
        }
        return latest;
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return DATE_FORMATTER.format(date.toInstant());
    }
}
