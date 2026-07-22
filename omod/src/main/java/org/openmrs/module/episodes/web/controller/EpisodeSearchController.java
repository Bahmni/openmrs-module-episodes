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
import org.openmrs.module.episodes.search.SearchService;
import org.openmrs.module.episodes.search.model.SearchResponse;
import org.openmrs.module.episodes.search.model.SearchRequest;
import org.openmrs.module.episodes.search.exceptions.EpisodeSearchException;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    private Map<String, SearchService> serviceRegistry;

    @PostConstruct
    public void init() {
        serviceRegistry = new HashMap<>();
        List<SearchService> services = Context.getRegisteredComponents(SearchService.class);
        for (SearchService service : services) {
            serviceRegistry.put(service.getEntity(), service);
        }
    }

    @PostMapping(
            value = "/search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        try {
            SearchService service = resolveService(request);
            List<Map<String, Object>> results = service.search(request);
            return ResponseEntity.ok(new SearchResponse(results));
        } catch (InvalidSearchCriteriaException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus().getCode()).body(error);
        } catch (RuntimeException e) {
            EpisodeSearchException searchException =
                    new EpisodeSearchException("Unexpected error during episode search", e);
            log.error(searchException.getMessage(), searchException);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "An unexpected error occurred while processing the search request");
            return ResponseEntity.status(searchException.getStatus().getCode()).body(error);
        }
    }

    private SearchService resolveService(SearchRequest request) {
        String entity = request.getEntity();
        if (entity == null || entity.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        SearchService service = serviceRegistry.get(entity);
        if (service == null) {
            throw new InvalidSearchCriteriaException(
                    "Entity '" + entity + "' is not supported. Supported entities: " + serviceRegistry.keySet(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        return service;
    }
}
