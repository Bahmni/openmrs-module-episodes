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
import org.openmrs.module.episodes.search.validation.CriteriaValidator;
import org.openmrs.module.episodes.search.SearchService;
import org.openmrs.module.episodes.search.model.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link SearchService} implementation for the "patientProgram" entity.
 *
 * <p>Searches {@link PatientProgram} records using Hibernate Criteria built from
 * the incoming condition tree, then enriches each result with its associated
 * {@link Episode} data (status, dates, care manager).</p>
 *
 * <p>Response mapping is delegated to {@link PatientProgramResponseMapper}.</p>
 *
 * @see SearchService
 */
public class PatientProgramSearchHandler implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(PatientProgramSearchHandler.class);

    private static final String ENTITY = "patientProgram";

    private final PatientProgramSearchDAO patientProgramSearchDAO;
    private final CriteriaValidator validator;
    private final PatientProgramResponseMapper mapper = new PatientProgramResponseMapper();

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
        log.debug("Searching patient programs for entity '{}'", request.getEntity());
        validator.validate(request);
        List<PatientProgram> patientPrograms = patientProgramSearchDAO.search(request.getCriteria());
        if (patientPrograms.isEmpty()) {
            log.debug("No patient programs found");
            return new ArrayList<>();
        }

        Set<Integer> patientProgramIds = new HashSet<>();
        for (PatientProgram pp : patientPrograms) {
            patientProgramIds.add(pp.getPatientProgramId());
        }
        Map<Integer, Map<String, Object>> episodeByPpId = buildEpisodeByPatientProgramId(patientProgramIds);

        List<Map<String, Object>> results = new ArrayList<>();
        for (PatientProgram pp : patientPrograms) {
            results.add(mapper.toMap(pp, episodeByPpId.get(pp.getPatientProgramId())));
        }
        log.debug("Returning {} patient program results", results.size());
        return results;
    }

    private Map<Integer, Map<String, Object>> buildEpisodeByPatientProgramId(Set<Integer> patientProgramIds) {
        Map<Integer, Map<String, Object>> index = new HashMap<>();
        for (Episode episode : patientProgramSearchDAO.getEpisodesForPatientProgramIds(patientProgramIds)) {
            Map<String, Object> episodeMap = mapper.toEpisodeMap(episode);
            for (PatientProgram pp : episode.getPatientPrograms()) {
                if (patientProgramIds.contains(pp.getPatientProgramId())) {
                    index.put(pp.getPatientProgramId(), episodeMap);
                }
            }
        }
        return index;
    }
}
