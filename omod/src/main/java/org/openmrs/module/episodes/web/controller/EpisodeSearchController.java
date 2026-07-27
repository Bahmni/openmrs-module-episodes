/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.web.controller;

import org.openmrs.module.episodes.service.SearchService;
import org.openmrs.module.episodes.search.SearchServiceRegistry;
import org.openmrs.module.episodes.search.model.ContextSearchResponse;
import org.openmrs.module.episodes.search.model.ErrorSearchResponse;
import org.openmrs.module.episodes.search.model.SearchRequest;
import org.openmrs.module.episodes.search.exceptions.EpisodeSearchException;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/rest/v1/episode")
public class EpisodeSearchController {

    private static final Logger log = LoggerFactory.getLogger(EpisodeSearchController.class);

    private final SearchServiceRegistry searchServiceRegistry;

    public EpisodeSearchController(SearchServiceRegistry searchServiceRegistry) {
        this.searchServiceRegistry = searchServiceRegistry;
    }

    @PostMapping(
            value = "/search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<ContextSearchResponse> search(@RequestBody SearchRequest request) {
        String entity = request.getEntity();
        try {
            SearchService service = searchServiceRegistry.resolve(entity);
            ContextSearchResponse response = service.search(request);
            return ResponseEntity.ok(response);
        } catch (InvalidSearchCriteriaException e) {
            return ResponseEntity.status(e.getStatus().getCode())
                    .body(new ErrorSearchResponse(entity, e.getStatus().getCode(), e.getMessages()));
        } catch (RuntimeException e) {
            EpisodeSearchException searchException =
                    new EpisodeSearchException("Unexpected error during episode search", e);
            log.error(searchException.getMessage(), searchException);
            int statusCode = searchException.getStatus().getCode();
            return ResponseEntity.status(statusCode)
                    .body(new ErrorSearchResponse(entity, statusCode,
                            "An unexpected error occurred while processing the search request"));
        }
    }
}
