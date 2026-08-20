/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.bahmni.search.model.SearchResponseMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EpisodeSearchResponse {

    private final String context;
    private final SearchResponseMeta meta;
    private final List<Map<String, Object>> results;
    private final SearchError error;


    private EpisodeSearchResponse(String context, List<Map<String, Object>> results,
                                   SearchResponseMeta meta, SearchError error) {
        this.context = context;
        this.meta = meta;
        this.results = results;
        this.error = error;
    }

    public static EpisodeSearchResponse success(String context, List<Map<String, Object>> results) {
        return new EpisodeSearchResponse(context, results, new SearchResponseMeta(), null);
    }

    public static EpisodeSearchResponse success(String context, List<Map<String, Object>> results,
                                                  SearchResponseMeta meta) {
        return new EpisodeSearchResponse(context, results, meta, null);
    }

    public static EpisodeSearchResponse error(String context, int status, List<String> messages) {
        return new EpisodeSearchResponse(context, Collections.emptyList(),
                new SearchResponseMeta(), new SearchError(status, messages));
    }

    public static EpisodeSearchResponse error(String context, int status, String message) {
        return error(context, status, Collections.singletonList(message));
    }
}
