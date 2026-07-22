/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.search.SearchHandler;
import org.openmrs.module.episodes.search.criteria.SearchResponse;
import org.openmrs.module.episodes.search.criteria.SearchRequest;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rest/v1/episode")
public class EpisodeSearchController {

    private static final Logger log = LoggerFactory.getLogger(EpisodeSearchController.class);

    private Map<String, SearchHandler> handlerRegistry;

    @PostConstruct
    public void init() {
        handlerRegistry = new HashMap<>();
        List<SearchHandler> handlers = Context.getRegisteredComponents(SearchHandler.class);
        for (SearchHandler handler : handlers) {
            handlerRegistry.put(handler.getEntity(), handler);
        }
    }

    @RequestMapping(
            value = "/search",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        try {
            SearchHandler handler = resolveHandler(request);
            List<Map<String, Object>> results = handler.search(request);
            return ResponseEntity.ok(new SearchResponse(results));
        } catch (InvalidSearchCriteriaException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus().getCode()).body(error);
        } catch (Exception e) {
            log.error("Unexpected error during episode search", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "An unexpected error occurred while processing the search request");
            return ResponseEntity.status(500).body(error);
        }
    }

    private SearchHandler resolveHandler(SearchRequest request) {
        String entity = request.getEntity();
        if (entity == null || entity.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        SearchHandler handler = handlerRegistry.get(entity);
        if (handler == null) {
            throw new InvalidSearchCriteriaException(
                    "Entity '" + entity + "' is not supported. Supported entities: " + handlerRegistry.keySet(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        return handler;
    }
}
