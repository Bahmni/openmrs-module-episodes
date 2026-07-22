/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.handlers;

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
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.CriteriaValidator;
import org.openmrs.module.episodes.search.SearchHandler;
import org.openmrs.module.episodes.search.criteria.SearchRequest;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatientProgramSearchHandler implements SearchHandler {

    private static final String ENTITY = "patientProgram";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

    private final PatientProgramSearchDAO patientProgramSearchDAO;
    private final CriteriaValidator validator;

    public PatientProgramSearchHandler(PatientProgramSearchDAO patientProgramSearchDAO,
            CriteriaValidator validator) {
        this.patientProgramSearchDAO = patientProgramSearchDAO;
        this.validator = validator;
    }

    @Override
    public String getEntity() {
        return ENTITY;
    }

    @Override
    public List<Map<String, Object>> search(SearchRequest request) {
        validator.validate(request);
        List<PatientProgram> patientPrograms = patientProgramSearchDAO.search(request.getCriteria());
        if (patientPrograms.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> patientProgramIds = new HashSet<>();
        for (PatientProgram pp : patientPrograms) {
            patientProgramIds.add(pp.getPatientProgramId());
        }
        Map<Integer, Map<String, Object>> episodeByPpId = buildEpisodeByPatientProgramId(patientProgramIds);

        List<Map<String, Object>> results = new ArrayList<>();
        for (PatientProgram pp : patientPrograms) {
            results.add(toMap(pp, episodeByPpId.get(pp.getPatientProgramId())));
        }
        return results;
    }

    private Map<Integer, Map<String, Object>> buildEpisodeByPatientProgramId(Set<Integer> patientProgramIds) {
        Map<Integer, Map<String, Object>> index = new HashMap<>();
        for (Episode episode : patientProgramSearchDAO.getEpisodesForPatientProgramIds(patientProgramIds)) {
            Map<String, Object> episodeMap = toEpisodeMap(episode);
            for (PatientProgram pp : episode.getPatientPrograms()) {
                if (patientProgramIds.contains(pp.getPatientProgramId())) {
                    index.put(pp.getPatientProgramId(), episodeMap);
                }
            }
        }
        return index;
    }

    private Map<String, Object> toMap(PatientProgram pp, Map<String, Object> episode) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", pp.getUuid());
        map.put("dateEnrolled", formatDate(pp.getDateEnrolled()));
        map.put("dateCompleted", formatDate(pp.getDateCompleted()));
        map.put("patient", toPatientMap(pp.getPatient()));
        map.put("program", toProgramMap(pp.getProgram()));
        map.put("location", toLocationMap(pp.getLocation()));
        map.put("attributes", toAttributesList(pp));
        map.put("currentState", toCurrentStateMap(pp));
        map.put("episode", episode);
        return map;
    }

    private Map<String, Object> toPatientMap(Patient patient) {
        if (patient == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", patient.getUuid());
        map.put("gender", patient.getGender());
        map.put("birthdate", formatDate(patient.getBirthdate()));
        map.put("voided", patient.getVoided());
        map.put("name", toPersonNameMap(patient.getPersonName()));
        map.put("identifiers", toIdentifiersList(patient));
        return map;
    }

    private Map<String, Object> toPersonNameMap(PersonName name) {
        if (name == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("givenName", name.getGivenName());
        map.put("middleName", name.getMiddleName());
        map.put("familyName", name.getFamilyName());
        map.put("familyName2", name.getFamilyName2());
        map.put("voided", name.getVoided());
        return map;
    }

    private List<Map<String, Object>> toIdentifiersList(Patient patient) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PatientIdentifier pi : patient.getActiveIdentifiers()) {
            Map<String, Object> idMap = new LinkedHashMap<>();
            idMap.put("uuid", pi.getUuid());
            idMap.put("identifier", pi.getIdentifier());
            idMap.put("preferred", Boolean.TRUE.equals(pi.getPreferred()));
            if (pi.getIdentifierType() != null) {
                idMap.put("display", pi.getIdentifierType().getName() + " = " + pi.getIdentifier());
                idMap.put("identifierType", ref(pi.getIdentifierType().getUuid(), pi.getIdentifierType().getName()));
            }
            list.add(idMap);
        }
        return list;
    }

    private Map<String, Object> toProgramMap(Program program) {
        if (program == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", program.getUuid());
        map.put("name", program.getName());
        map.put("retired", program.getRetired());
        map.put("description", program.getDescription());
        map.put("concept", toConceptRef(program.getConcept()));
        return map;
    }

    private Map<String, Object> toLocationMap(Location location) {
        if (location == null) return null;
        return ref(location.getUuid(), location.getName());
    }

    private List<Map<String, Object>> toAttributesList(PatientProgram pp) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (org.openmrs.attribute.Attribute<?, ?> attr : pp.getActiveAttributes()) {
            Map<String, Object> attrMap = new LinkedHashMap<>();
            attrMap.put("uuid", attr.getUuid());
            attrMap.put("value", attr.getValueReference());
            attrMap.put("attributeType", ref(attr.getAttributeType().getUuid(), attr.getAttributeType().getName()));
            list.add(attrMap);
        }
        return list;
    }

    private Map<String, Object> toCurrentStateMap(PatientProgram pp) {
        PatientState current = selectCurrentState(pp.getStates());
        if (current == null) return null;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", current.getUuid());
        map.put("startDate", formatDate(current.getStartDate()));
        map.put("endDate", formatDate(current.getEndDate()));

        ProgramWorkflowState wfState = current.getState();
        if (wfState != null) {
            Map<String, Object> stateMap = new LinkedHashMap<>();
            stateMap.put("uuid", wfState.getUuid());
            stateMap.put("concept", toConceptRef(wfState.getConcept()));
            stateMap.put("initial", Boolean.TRUE.equals(wfState.getInitial()));
            stateMap.put("terminal", Boolean.TRUE.equals(wfState.getTerminal()));
            map.put("state", stateMap);

            ProgramWorkflow wf = wfState.getProgramWorkflow();
            if (wf != null) {
                Map<String, Object> wfMap = new LinkedHashMap<>();
                wfMap.put("uuid", wf.getUuid());
                wfMap.put("concept", toConceptRef(wf.getConcept()));
                map.put("workflow", wfMap);
            }
        }
        return map;
    }

    private Map<String, Object> toEpisodeMap(Episode episode) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", episode.getUuid());
        map.put("status", episode.getStatus() != null ? episode.getStatus().name() : null);
        map.put("dateStarted", formatDate(episode.getDateStarted()));
        map.put("dateEnded", formatDate(episode.getDateEnded()));
        map.put("careManager", toCareManagerMap(episode.getCareManager()));
        return map;
    }

    private Map<String, Object> toCareManagerMap(Provider provider) {
        if (provider == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", provider.getUuid());
        map.put("display", provider.getName());
        map.put("identifier", provider.getIdentifier());
        if (provider.getPerson() != null) {
            map.put("person", toPersonNameMap(provider.getPerson().getPersonName()));
        }
        return map;
    }

    private Map<String, Object> toConceptRef(Concept concept) {
        if (concept == null) return null;
        String display = concept.getName() != null ? concept.getName().getName() : null;
        return ref(concept.getUuid(), display);
    }

    private Map<String, Object> ref(String uuid, String display) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", uuid);
        if (display != null) map.put("display", display);
        return map;
    }

    public static PatientState selectCurrentState(Collection<PatientState> states) {
        List<PatientState> active = new ArrayList<>();
        List<PatientState> ended = new ArrayList<>();
        for (PatientState state : states) {
            if (state.getVoided()) continue;
            if (state.getEndDate() == null) active.add(state);
            else ended.add(state);
        }
        return active.isEmpty() ? latestByEndDate(ended) : latestByStartDate(active);
    }

    private static PatientState latestByStartDate(List<PatientState> states) {
        PatientState latest = states.get(0);
        for (PatientState state : states) {
            if (state.getStartDate() != null && latest.getStartDate() != null
                    && state.getStartDate().after(latest.getStartDate())) {
                latest = state;
            }
        }
        return latest;
    }

    private static PatientState latestByEndDate(List<PatientState> states) {
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
