/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Standard response envelope for context-aware search APIs.
 * Accommodates both success and error scenarios through a unified contract.
 */
public interface ContextSearchResponse {

    String getContext();

    SearchResponseMeta getMetaData();

    List<Map<String, Object>> getResults();

    List<Map<String, String>> getLinks();

    /**
     * Returns error details when the response represents a failure, or null on success.
     * Excluded from JSON output when null.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    default SearchError getError() {
        return null;
    }

    default boolean isSuccess() {
        return getError() == null;
    }
}
