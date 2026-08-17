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
import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
import org.openmrs.module.episodes.search.dto.SearchResponseMeta;
import org.openmrs.module.episodes.search.validation.SearchCriteriaValidator;

import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.openmrs.module.episodes.search.dto.SearchRequest;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.PaginationResponse;
import org.bahmni.search.model.SearchRequestMeta;
import org.bahmni.search.pagination.PaginationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientProgramSearchServiceImpl implements EpisodeSearchService {

    private static final Logger log = LoggerFactory.getLogger(PatientProgramSearchServiceImpl.class);

    private static final String ENTITY = "patientProgram";

    private final PatientProgramSearchDAO patientProgramSearchDAO;
    private final SearchCriteriaValidator validator;
    private final PatientProgramResponseBuilder responseBuilder;

    public PatientProgramSearchServiceImpl(PatientProgramSearchDAO patientProgramSearchDAO,
            SearchCriteriaValidator validator,
            PatientProgramResponseBuilder responseBuilder) {
        this.patientProgramSearchDAO = patientProgramSearchDAO;
        this.validator = validator;
        this.responseBuilder = responseBuilder;
    }

    @Override
    public EpisodeSearchResponse search(SearchRequest request) {
        log.debug("Searching patient programs for entity '{}'", request.getEntity());
        validator.validateRequest(request);

        SearchRequestMeta meta = request.getMeta();
        PaginationRequest pagination = PaginationHelper.resolvePagination(meta);
        int effectiveLimit = PaginationHelper.resolveEffectiveLimit(pagination.getLimit());
        String sortOrder = PaginationHelper.resolveSortOrder(pagination.getSortOrder());
        String direction = pagination.getDirection();
        Long cursorId = PaginationHelper.decodeCursor(pagination.getCursor());
        boolean isPrev = PaginationHelper.isPrevDirection(direction);

        int fetchSize = effectiveLimit + 1;
        List<Episode> rawEpisodes = patientProgramSearchDAO.search(
                request.getCriteria(), cursorId, sortOrder, direction, fetchSize);

        boolean hasMore = PaginationHelper.hasMore(rawEpisodes.size(), effectiveLimit);
        List<Episode> episodes = PaginationHelper.trimAndOrient(rawEpisodes, effectiveLimit, isPrev);

        List<Map<String, Object>> results = buildResults(episodes);
        PaginationResponse paginationResponse = episodes.isEmpty()
                ? PaginationHelper.emptyPaginationResponse()
                : PaginationHelper.buildPaginationResponse(
                        episodes.get(0).getEpisodeId(),
                        episodes.get(episodes.size() - 1).getEpisodeId(),
                        hasMore, cursorId, isPrev);

        Long totalCount = PaginationHelper.resolveTotalCount(meta,
                () -> patientProgramSearchDAO.count(request.getCriteria()));

        SearchResponseMeta responseMeta = new SearchResponseMeta(paginationResponse, totalCount);
        log.debug("Returning {} patient program results", results.size());
        return EpisodeSearchResponse.success(ENTITY, results, responseMeta);
    }

    private List<Map<String, Object>> buildResults(List<Episode> episodes) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Episode episode : episodes) {
            Map<String, Object> episodeMap = responseBuilder.mapEpisode(episode);
            for (PatientProgram patientProgram : episode.getPatientPrograms()) {
                results.add(responseBuilder.mapPatientProgram(patientProgram, episodeMap));
            }
        }
        return results;
    }
}
