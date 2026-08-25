/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.impl;

import org.openmrs.module.episodes.dao.EpisodePatientProgram;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramResponseBuilder;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
import org.openmrs.module.episodes.search.validation.SearchCriteriaValidator;

import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.openmrs.module.episodes.search.dto.SearchRequest;
import org.bahmni.search.model.SearchRequestMeta;
import org.bahmni.search.model.SearchResponseMeta;
import org.bahmni.search.pagination.PageResult;
import org.bahmni.search.pagination.PaginationHelper;
import org.bahmni.search.pagination.ResolvedPagination;
import org.openmrs.api.AdministrationService;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientProgramSearchServiceImpl implements EpisodeSearchService {

    private static final Logger log = LoggerFactory.getLogger(PatientProgramSearchServiceImpl.class);

    private static final String ENTITY = "patientProgram";

    private static final String GP_PAGINATION_DEFAULT_LIMIT = "bahmni.search.pagination.defaultLimit";
    private static final String GP_PAGINATION_MAX_LIMIT = "bahmni.search.pagination.maxLimit";

    private static final int FALLBACK_DEFAULT_LIMIT = 100;
    private static final int FALLBACK_MAX_LIMIT = 500;


    private final PatientProgramSearchDAO patientProgramSearchDAO;
    private final SearchCriteriaValidator validator;
    private final PatientProgramResponseBuilder responseBuilder;
    private final AdministrationService administrationService;

    public PatientProgramSearchServiceImpl(PatientProgramSearchDAO patientProgramSearchDAO,

            SearchCriteriaValidator validator,
            PatientProgramResponseBuilder responseBuilder,
            AdministrationService administrationService) {
        this.patientProgramSearchDAO = patientProgramSearchDAO;
        this.validator = validator;
        this.responseBuilder = responseBuilder;
        this.administrationService = administrationService;
    }

    @Override
    public EpisodeSearchResponse search(SearchRequest request) {
        log.debug("Searching patient programs for entity '{}'", request.getEntity());
        validator.validateRequest(request);

        SearchRequestMeta meta = request.getMeta();
        int defaultLimit = PaginationHelper.resolveGlobalProperty(
                administrationService.getGlobalProperty(GP_PAGINATION_DEFAULT_LIMIT), FALLBACK_DEFAULT_LIMIT, GP_PAGINATION_DEFAULT_LIMIT);
        int maxLimit = PaginationHelper.resolveGlobalProperty(
                administrationService.getGlobalProperty(GP_PAGINATION_MAX_LIMIT), FALLBACK_MAX_LIMIT, GP_PAGINATION_MAX_LIMIT);

        ResolvedPagination paginationData = PaginationHelper.resolvePaginationContext(meta, ENTITY, defaultLimit, maxLimit);

        List<Integer> matchingIds = patientProgramSearchDAO.findMatchingIds(
                request.getCriteria(), paginationData.getCursorId(), paginationData.getSortOrder(),
                paginationData.getDirection(), paginationData.getFetchSize());

        boolean hasMore = PaginationHelper.hasMore(matchingIds.size(), paginationData.getEffectiveLimit());
        List<Integer> idsToFetch = hasMore
                ? matchingIds.subList(0, paginationData.getEffectiveLimit())
                : matchingIds;

        List<EpisodePatientProgram> rawPairs = patientProgramSearchDAO.findByIds(idsToFetch);

        PageResult<EpisodePatientProgram> pageResult = PaginationHelper.paginate(
                ENTITY, rawPairs, pair -> pair.getPatientProgram().getPatientProgramId().longValue(),
                paginationData, hasMore);

        List<EpisodePatientProgram> pairs = pageResult.getItems();
        List<Map<String, Object>> results = buildResults(pairs);

        Long totalCount = PaginationHelper.resolveTotalCount(meta,
                () -> patientProgramSearchDAO.count(request.getCriteria()));

        SearchResponseMeta responseMeta = new SearchResponseMeta(pageResult.getPaginationResponse(), totalCount);
        log.debug("Returning {} patient program results", results.size());
        return EpisodeSearchResponse.success(ENTITY, results, responseMeta);
    }

    private List<Map<String, Object>> buildResults(List<EpisodePatientProgram> pairs) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (EpisodePatientProgram pair : pairs) {
            Map<String, Object> episodeMap = responseBuilder.mapEpisode(pair.getEpisode());
            results.add(responseBuilder.mapPatientProgram(pair.getPatientProgram(), episodeMap));
        }
        return results;
    }

}


