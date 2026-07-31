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
import org.bahmni.search.model.ContextSearchResponse;
import org.bahmni.search.model.DefaultSearchResponse;
import org.openmrs.module.episodes.search.validation.CriteriaValidator;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.bahmni.search.model.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientProgramSearchServiceImpl implements EpisodeSearchService {

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
    public ContextSearchResponse search(SearchRequest request) {
        log.debug("Searching patient programs for entity '{}'", request.getEntity());
        validator.validateRequest(request);

        List<Episode> episodes = patientProgramSearchDAO.search(request.getCriteria());
        if (episodes.isEmpty()) {
            log.debug("No episodes found for the given criteria");
            return new DefaultSearchResponse(ENTITY, new ArrayList<>());
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Episode episode : episodes) {
            Map<String, Object> episodeMap = responseBuilder.mapEpisode(episode);
            for (PatientProgram patientProgram : episode.getPatientPrograms()) {
                results.add(responseBuilder.mapPatientProgram(patientProgram, episodeMap));
            }
        }

        log.debug("Returning {} patient program results", results.size());
        return new DefaultSearchResponse(ENTITY, results);
    }
}
