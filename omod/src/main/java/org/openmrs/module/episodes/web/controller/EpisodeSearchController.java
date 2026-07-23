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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        try {
            SearchService service = searchServiceRegistry.resolve(request.getEntity());
            List<Map<String, Object>> results = service.search(request);
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (InvalidSearchCriteriaException e) {
            return ResponseEntity.status(e.getStatus().getCode())
                    .body(buildErrorResponse(e.getStatus().getCode(), e.getMessages()));
        } catch (RuntimeException e) {
            EpisodeSearchException searchException =
                    new EpisodeSearchException("Unexpected error during episode search", e);
            log.error(searchException.getMessage(), searchException);
            return ResponseEntity.status(searchException.getStatus().getCode())
                    .body(buildErrorResponse(searchException.getStatus().getCode(),
                            Collections.singletonList("An unexpected error occurred while processing the search request")));
        }
    }

    private Map<String, Object> buildErrorResponse(int status, List<String> messages) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status);
        error.put("messages", messages);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        return response;
    }
}
