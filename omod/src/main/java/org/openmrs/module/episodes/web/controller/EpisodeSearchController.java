/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.web.controller;

import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
import org.openmrs.module.episodes.search.dto.SearchRequest;
import org.openmrs.module.episodes.search.validation.SearchCriteriaValidator;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/rest/v1/episode")
public class EpisodeSearchController {

    private static final String SUPPORTED_ENTITY = "patientProgram";

    public static final String CURRENT_ENTITY_ATTRIBUTE = "episodeSearch.currentEntity";

    private final EpisodeSearchService episodeSearchService;
    private final SearchCriteriaValidator criteriaValidator;

    @Autowired
    public EpisodeSearchController(EpisodeSearchService episodeSearchService,
                                   SearchCriteriaValidator criteriaValidator) {
        this.episodeSearchService = episodeSearchService;
        this.criteriaValidator = criteriaValidator;
    }

    @PostMapping(
            value = "/search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<EpisodeSearchResponse> search(@RequestBody SearchRequest request, WebRequest webRequest) {
        String entity = request.getEntity();
        criteriaValidator.validateEntity(entity, SUPPORTED_ENTITY);
        webRequest.setAttribute(CURRENT_ENTITY_ATTRIBUTE, entity, WebRequest.SCOPE_REQUEST);
        criteriaValidator.validateRequest(request);
        EpisodeSearchResponse response = episodeSearchService.search(request);
        return ResponseEntity.ok(response);
    }

}
