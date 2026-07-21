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
import org.openmrs.module.episodes.search.dto.EpisodeSearchResultDTO;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.criteria.SearchRequest;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rest/v1/episode")
public class EpisodeSearchController {

    private EpisodeSearchService episodeSearchService;

    @PostConstruct
    public void init() {
        List<EpisodeSearchService> services = Context.getRegisteredComponents(EpisodeSearchService.class);
        if (services.isEmpty()) {
            throw new IllegalStateException("EpisodeSearchService not found in application context");
        }
        episodeSearchService = services.get(0);
    }

    @RequestMapping(
            value = "/search",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Map<String, Object>> search(@RequestBody SearchRequest request) {
        try {
            List<EpisodeSearchResultDTO> results = episodeSearchService.search(request);
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (InvalidSearchCriteriaException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus().getCode()).body(error);
        }
    }
}
