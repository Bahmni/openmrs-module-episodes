/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.service.impl;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Provider;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeSearchDAO;
import org.openmrs.module.episodes.search.BuiltQuery;
import org.openmrs.module.episodes.search.CriteriaValidator;
import org.openmrs.module.episodes.search.EpisodeSearchQueryBuilder;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResultDTO;
import org.openmrs.module.episodes.search.dto.LocationRefDTO;
import org.openmrs.module.episodes.search.dto.PatientDTO;
import org.openmrs.module.episodes.search.dto.PatientProgramDTO;
import org.openmrs.module.episodes.search.dto.PersonNameDTO;
import org.openmrs.module.episodes.search.dto.ProgramDTO;
import org.openmrs.module.episodes.search.criteria.SearchRequest;
import org.openmrs.module.episodes.service.EpisodeSearchService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EpisodeSearchServiceImpl implements EpisodeSearchService {

    private final EpisodeSearchDAO episodeSearchDAO;
    private final CriteriaValidator validator;
    private final EpisodeSearchQueryBuilder queryBuilder;

    public EpisodeSearchServiceImpl(EpisodeSearchDAO episodeSearchDAO) {
        this.episodeSearchDAO = episodeSearchDAO;
        this.validator = new CriteriaValidator();
        this.queryBuilder = new EpisodeSearchQueryBuilder();
    }

    @Override
    public List<EpisodeSearchResultDTO> search(SearchRequest request) {
        validator.validate(request);
        BuiltQuery builtQuery = queryBuilder.build(request.getCriteria());
        List<Episode> episodes = episodeSearchDAO.search(builtQuery);
        return toResultDTOs(episodes);
    }

    private List<EpisodeSearchResultDTO> toResultDTOs(List<Episode> episodes) {
        List<EpisodeSearchResultDTO> results = new ArrayList<>();
        for (Episode episode : episodes) {
            results.add(toResultDTO(episode));
        }
        return results;
    }

    private EpisodeSearchResultDTO toResultDTO(Episode episode) {
        EpisodeSearchResultDTO dto = new EpisodeSearchResultDTO();
        dto.setUuid(episode.getUuid());
        dto.setStatus(episode.getStatus() != null ? episode.getStatus().name() : null);
        dto.setDateStarted(formatDate(episode.getDateStarted()));
        dto.setDateEnded(formatDate(episode.getDateEnded()));

        dto.setPatient(mapPatient(episode.getPatient()));
        dto.setPatientPrograms(mapPatientPrograms(episode));
        dto.setCareManager(mapCareManager(episode.getCareManager()));

        return dto;
    }

    private PatientDTO mapPatient(Patient patient) {
        if (patient == null) {
            return null;
        }
        PatientDTO dto = new PatientDTO();
        dto.setUuid(patient.getUuid());
        dto.setVoided(patient.getVoided() != null && patient.getVoided());
        dto.setIdentifiers(mapIdentifiers(patient));
        dto.setPerson(mapPerson(patient));

        return dto;
    }

    private List<PatientDTO.IdentifierDTO> mapIdentifiers(Patient patient) {
        List<PatientDTO.IdentifierDTO> result = new ArrayList<>();
        for (PatientIdentifier pi : patient.getActiveIdentifiers()) {
            PatientDTO.IdentifierDTO idDto = new PatientDTO.IdentifierDTO();
            idDto.setUuid(pi.getUuid());
            idDto.setIdentifier(pi.getIdentifier());
            idDto.setPreferred(Boolean.TRUE.equals(pi.getPreferred()));

            PatientIdentifierType pit = pi.getIdentifierType();
            if (pit != null) {
                PatientDTO.IdentifierTypeDTO typeDto = new PatientDTO.IdentifierTypeDTO();
                typeDto.setUuid(pit.getUuid());
                typeDto.setDisplay(pit.getName());
                idDto.setIdentifierType(typeDto);
                idDto.setDisplay(pit.getName() + " = " + pi.getIdentifier());
            }

            Location loc = pi.getLocation();
            if (loc != null) {
                LocationRefDTO locDto = new LocationRefDTO();
                locDto.setUuid(loc.getUuid());
                locDto.setDisplay(loc.getName());
                idDto.setLocation(locDto);
            }

            result.add(idDto);
        }
        return result;
    }

    private PatientDTO.PersonDTO mapPerson(Patient patient) {
        PatientDTO.PersonDTO personDto = new PatientDTO.PersonDTO();
        personDto.setUuid(patient.getUuid());
        personDto.setGender(patient.getGender());
        personDto.setAge(patient.getAge());
        personDto.setBirthdate(formatDate(patient.getBirthdate()));
        personDto.setBirthdateEstimated(Boolean.TRUE.equals(patient.getBirthdateEstimated()));

        PersonName name = patient.getPersonName();
        if (name != null) {
            personDto.setPreferredName(buildPersonNameDTO(name));
        }

        return personDto;
    }

    private List<PatientProgramDTO> mapPatientPrograms(Episode episode) {
        List<PatientProgramDTO> result = new ArrayList<>();
        for (PatientProgram pp : episode.getPatientPrograms()) {
            if (!pp.getVoided()) {
                result.add(mapPatientProgram(pp));
            }
        }
        return result;
    }

    private PatientProgramDTO mapPatientProgram(PatientProgram pp) {
        PatientProgramDTO dto = new PatientProgramDTO();
        dto.setUuid(pp.getUuid());

        Location loc = pp.getLocation();
        if (loc != null) {
            LocationRefDTO locDto = new LocationRefDTO();
            locDto.setUuid(loc.getUuid());
            locDto.setDisplay(loc.getName());
            dto.setLocation(locDto);
        }

        dto.setAttributes(mapProgramAttributes(pp));
        dto.setProgram(mapProgram(pp.getProgram()));
        dto.setCurrentState(findCurrentState(pp));

        return dto;
    }

    private PatientProgramDTO.CurrentStateDTO findCurrentState(PatientProgram pp) {
        PatientState current = selectCurrentState(pp.getStates());
        return current != null ? mapCurrentState(current) : null;
    }

    static PatientState selectCurrentState(Collection<PatientState> states) {
        List<PatientState> active = new ArrayList<>();
        List<PatientState> ended = new ArrayList<>();

        for (PatientState state : states) {
            if (state.getVoided()) continue;
            if (state.getEndDate() == null) {
                active.add(state);
            } else {
                ended.add(state);
            }
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
            if (state.getEndDate().after(latest.getEndDate())) {
                latest = state;
            }
        }
        return latest;
    }

    private PatientProgramDTO.CurrentStateDTO mapCurrentState(PatientState patientState) {
        PatientProgramDTO.CurrentStateDTO dto = new PatientProgramDTO.CurrentStateDTO();
        dto.setUuid(patientState.getUuid());
        dto.setStartDate(formatDate(patientState.getStartDate()));
        dto.setEndDate(formatDate(patientState.getEndDate()));

        ProgramWorkflowState workflowState = patientState.getState();
        if (workflowState != null) {
            PatientProgramDTO.CurrentStateDTO.WorkflowStateDTO stateDto =
                    new PatientProgramDTO.CurrentStateDTO.WorkflowStateDTO();
            stateDto.setUuid(workflowState.getUuid());
            stateDto.setInitial(Boolean.TRUE.equals(workflowState.getInitial()));
            stateDto.setTerminal(Boolean.TRUE.equals(workflowState.getTerminal()));
            stateDto.setConcept(toConceptRef(workflowState.getConcept()));
            dto.setState(stateDto);

            ProgramWorkflow workflow = workflowState.getProgramWorkflow();
            if (workflow != null) {
                PatientProgramDTO.CurrentStateDTO.WorkflowRefDTO workflowDto =
                        new PatientProgramDTO.CurrentStateDTO.WorkflowRefDTO();
                workflowDto.setUuid(workflow.getUuid());
                workflowDto.setConcept(toConceptRef(workflow.getConcept()));
                dto.setWorkflow(workflowDto);
            }
        }

        return dto;
    }

    private PatientProgramDTO.CurrentStateDTO.ConceptRefDTO toConceptRef(Concept concept) {
        if (concept == null) return null;
        PatientProgramDTO.CurrentStateDTO.ConceptRefDTO ref =
                new PatientProgramDTO.CurrentStateDTO.ConceptRefDTO();
        ref.setUuid(concept.getUuid());
        ConceptName name = concept.getName();
        if (name != null) ref.setDisplay(name.getName());
        return ref;
    }

    private ProgramDTO mapProgram(Program program) {
        if (program == null) {
            return null;
        }
        ProgramDTO dto = new ProgramDTO();
        dto.setUuid(program.getUuid());
        dto.setName(program.getName());
        dto.setRetired(Boolean.TRUE.equals(program.getRetired()));
        dto.setDescription(program.getDescription());

        if (program.getConcept() != null) {
            ProgramDTO.ConceptRefDTO conceptDto = new ProgramDTO.ConceptRefDTO();
            conceptDto.setUuid(program.getConcept().getUuid());
            if (program.getConcept().getName() != null) {
                conceptDto.setDisplay(program.getConcept().getName().getName());
            }
            dto.setConcept(conceptDto);
        }

        return dto;
    }

    @SuppressWarnings("unchecked")
    private List<PatientProgramDTO.ProgramAttributeDTO> mapProgramAttributes(PatientProgram pp) {
        List<PatientProgramDTO.ProgramAttributeDTO> result = new ArrayList<>();
        for (org.openmrs.attribute.Attribute<?, ?> attr : pp.getActiveAttributes()) {
            PatientProgramDTO.ProgramAttributeDTO attrDto = new PatientProgramDTO.ProgramAttributeDTO();
            attrDto.setUuid(attr.getUuid());
            attrDto.setValue(attr.getValueReference());

            PatientProgramDTO.ProgramAttributeDTO.AttributeTypeRefDTO typeRef =
                    new PatientProgramDTO.ProgramAttributeDTO.AttributeTypeRefDTO();
            typeRef.setUuid(attr.getAttributeType().getUuid());
            attrDto.setAttributeType(typeRef);

            result.add(attrDto);
        }
        return result;
    }

    private EpisodeSearchResultDTO.CareManagerDTO mapCareManager(Provider careManager) {
        EpisodeSearchResultDTO.CareManagerDTO dto = new EpisodeSearchResultDTO.CareManagerDTO();
        if (careManager != null) {
            dto.setUuid(careManager.getUuid());
            dto.setDisplay(careManager.getName());
            dto.setIdentifier(careManager.getIdentifier());

            if (careManager.getPerson() != null) {
                EpisodeSearchResultDTO.CareManagerDTO.PersonDTO personDto =
                        new EpisodeSearchResultDTO.CareManagerDTO.PersonDTO();
                personDto.setUuid(careManager.getPerson().getUuid());
                PersonName name = careManager.getPerson().getPersonName();
                if (name != null) {
                    personDto.setPreferredName(buildPersonNameDTO(name));
                }
                dto.setPerson(personDto);
            }
        }
        return dto;
    }

    private PersonNameDTO buildPersonNameDTO(PersonName name) {
        PersonNameDTO dto = new PersonNameDTO();
        dto.setUuid(name.getUuid());
        dto.setGivenName(name.getGivenName());
        dto.setMiddleName(name.getMiddleName());
        dto.setFamilyName(name.getFamilyName());
        dto.setFamilyName2(name.getFamilyName2());
        dto.setVoided(Boolean.TRUE.equals(name.getVoided()));
        return dto;
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
