/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.web.controller;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ContextSearchResponse;
import org.bahmni.search.model.ErrorSearchResponse;
import org.bahmni.search.model.SearchRequest;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/v1/episode")
public class EpisodeSearchController {

    private static final Logger log = LoggerFactory.getLogger(EpisodeSearchController.class);

    private static final String SUPPORTED_ENTITY = "patientProgram";

    private final EpisodeSearchService episodeSearchService;

    @Autowired
    public EpisodeSearchController(EpisodeSearchService episodeSearchService) {
        this.episodeSearchService = episodeSearchService;
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
            if (entity == null || entity.isEmpty()) {
                throw new InvalidSearchCriteriaException(
                        "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
            }
            if (!SUPPORTED_ENTITY.equalsIgnoreCase(entity)) {
                throw new InvalidSearchCriteriaException(
                        "Entity '" + entity + "' is not supported. Supported entities: ["
                                + SUPPORTED_ENTITY + "]",
                        SearchResponseErrorStatus.BAD_REQUEST);
            }
            ContextSearchResponse response = episodeSearchService.search(request);
            return ResponseEntity.ok(response);
        } catch (InvalidSearchCriteriaException e) {
            return ResponseEntity.status(e.getStatus().getCode())
                    .body(new ErrorSearchResponse(entity, e.getStatus().getCode(), e.getMessages()));
        } catch (ContextAuthenticationException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Authentication required";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorSearchResponse(entity, HttpStatus.UNAUTHORIZED.value(), message));
        } catch (APIAuthenticationException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Access denied";
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorSearchResponse(entity, HttpStatus.FORBIDDEN.value(), message));
        } catch (RuntimeException e) {
            SearchException searchException =
                    new SearchException("Unexpected error during episode search", e);
            log.error(searchException.getMessage(), searchException);
            int statusCode = searchException.getStatus().getCode();
            return ResponseEntity.status(statusCode)
                    .body(new ErrorSearchResponse(entity, statusCode,
                            "An unexpected error occurred while processing the search request"));
        }
    }
}
