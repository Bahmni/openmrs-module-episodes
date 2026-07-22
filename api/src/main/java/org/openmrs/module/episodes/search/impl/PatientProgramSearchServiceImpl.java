/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.impl;

import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramResponseBuilder;
import org.openmrs.module.episodes.search.validation.CriteriaValidator;
import org.openmrs.module.episodes.service.SearchService;
import org.openmrs.module.episodes.search.model.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatientProgramSearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(PatientProgramSearchServiceImpl.class);

    private static final String ENTITY = "patientProgram";

    private final PatientProgramSearchDAO patientProgramSearchDAO;
    private final CriteriaValidator validator;
    private final PatientProgramResponseBuilder responseBuilder;

    public PatientProgramSearchServiceImpl(PatientProgramSearchDAO patientProgramSearchDAO,
            CriteriaValidator validator,
            PatientProgramResponseBuilder responseBuilder) {
        this.patientProgramSearchDAO = patientProgramSearchDAO;
        this.validator = validator;
        this.responseBuilder = responseBuilder;
    }

    @Override
    public String getEntity() {
        return ENTITY;
    }

    @Override
    public List<Map<String, Object>> search(SearchRequest request) {
        log.debug("Searching patient programs for entity '{}'", request.getEntity());
        validator.validateRequest(request);
        List<PatientProgram> patientPrograms = patientProgramSearchDAO.search(request.getCriteria());
        if (patientPrograms.isEmpty()) {
            log.debug("No patient programs found");
            return new ArrayList<>();
        }

        Set<Integer> patientProgramIds = new HashSet<>();
        for (PatientProgram patientProgram : patientPrograms) {
            patientProgramIds.add(patientProgram.getPatientProgramId());
        }
        Map<Integer, Map<String, Object>> episodesByPatientProgramId = buildEpisodeByPatientProgramId(patientProgramIds);

        List<Map<String, Object>> results = new ArrayList<>();
        for (PatientProgram patientProgram : patientPrograms) {
            results.add(responseBuilder.mapPatientProgram(patientProgram, episodesByPatientProgramId.get(patientProgram.getPatientProgramId())));
        }
        log.debug("Returning {} patient program results", results.size());
        return results;
    }

    private Map<Integer, Map<String, Object>> buildEpisodeByPatientProgramId(Set<Integer> patientProgramIds) {
        Map<Integer, Map<String, Object>> episodesByPatientProgramId = new HashMap<>();
        for (Episode episode : patientProgramSearchDAO.getEpisodesForPatientProgramIds(patientProgramIds)) {
            Map<String, Object> episodeMap = responseBuilder.mapEpisode(episode);
            for (PatientProgram patientProgram : episode.getPatientPrograms()) {
                if (patientProgramIds.contains(patientProgram.getPatientProgramId())) {
                    episodesByPatientProgramId.put(patientProgram.getPatientProgramId(), episodeMap);
                }
            }
        }
        return episodesByPatientProgramId;
    }
}
