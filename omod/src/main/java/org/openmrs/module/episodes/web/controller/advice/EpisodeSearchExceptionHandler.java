/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.web.controller.advice;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
import org.openmrs.module.episodes.web.controller.EpisodeSearchController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;

@ControllerAdvice(assignableTypes = EpisodeSearchController.class)
public class EpisodeSearchExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(EpisodeSearchExceptionHandler.class);

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    @ResponseBody
    public ResponseEntity<EpisodeSearchResponse> handleInvalidSearchCriteria(
            InvalidSearchCriteriaException e, WebRequest webRequest) {
        return errorResponse(currentEntity(webRequest), e.getStatus().getCode(), e.getMessages());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<EpisodeSearchResponse> handleMalformedRequest(
            HttpMessageNotReadableException e, WebRequest webRequest) {
        InvalidSearchCriteriaException invalidSearchCriteriaException = findInvalidSearchCriteriaCause(e);
        if (invalidSearchCriteriaException != null) {
            return errorResponse(currentEntity(webRequest),
                    invalidSearchCriteriaException.getStatus().getCode(),
                    invalidSearchCriteriaException.getMessages());
        }
        return errorResponse(currentEntity(webRequest), SearchResponseErrorStatus.BAD_REQUEST.getCode(),
                "Malformed request body");
    }

    private InvalidSearchCriteriaException findInvalidSearchCriteriaCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof InvalidSearchCriteriaException) {
                return (InvalidSearchCriteriaException) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    @ExceptionHandler(ContextAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<EpisodeSearchResponse> handleAuthenticationRequired(
            ContextAuthenticationException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Authentication required";
        return errorResponse(currentEntity(webRequest), HttpStatus.UNAUTHORIZED.value(), message);
    }

    @ExceptionHandler(APIAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<EpisodeSearchResponse> handleAccessDenied(
            APIAuthenticationException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Access denied";
        return errorResponse(currentEntity(webRequest), HttpStatus.FORBIDDEN.value(), message);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<EpisodeSearchResponse> handleUnexpectedError(
            RuntimeException e, WebRequest webRequest) {
        SearchException searchException =
                new SearchException("Unexpected error during episode search", e);
        log.error(searchException.getMessage(), searchException);
        int statusCode = searchException.getStatus().getCode();
        return errorResponse(currentEntity(webRequest), statusCode,
                "An unexpected error occurred while processing the search request");
    }

    private ResponseEntity<EpisodeSearchResponse> errorResponse(String entity, int status, List<String> messages) {
        return ResponseEntity.status(status).body(EpisodeSearchResponse.error(entity, status, messages));
    }

    private ResponseEntity<EpisodeSearchResponse> errorResponse(String entity, int status, String message) {
        return errorResponse(entity, status, Collections.singletonList(message));
    }

    private String currentEntity(WebRequest webRequest) {
        Object entity = webRequest.getAttribute(
                EpisodeSearchController.CURRENT_ENTITY_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
        return entity != null ? entity.toString() : null;
    }
}
