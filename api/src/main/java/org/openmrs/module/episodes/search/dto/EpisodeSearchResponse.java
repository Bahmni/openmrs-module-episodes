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

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EpisodeSearchResponse {

    private final String context;
    private final SearchResponseMeta metaData;
    private final List<Map<String, Object>> results;
    private final List<Map<String, String>> links;
    private final SearchError error;


    private EpisodeSearchResponse(String context, List<Map<String, Object>> results,
                                   List<Map<String, String>> links, SearchError error) {
        this.context = context;
        this.metaData = new SearchResponseMeta();
        this.results = results;
        this.links = links;
        this.error = error;
    }

    public static EpisodeSearchResponse success(String context, List<Map<String, Object>> results) {
        return new EpisodeSearchResponse(context, results, Collections.emptyList(), null);
    }

    public static EpisodeSearchResponse error(String context, int status, List<String> messages) {
        return new EpisodeSearchResponse(context, Collections.emptyList(), Collections.emptyList(),
                new SearchError(status, messages));
    }

    public static EpisodeSearchResponse error(String context, int status, String message) {
        return error(context, status, Collections.singletonList(message));
    }
}
