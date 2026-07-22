/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.criteria;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchResponse {

    private final List<Map<String, Object>> results;

    public SearchResponse(List<Map<String, Object>> results) {
        this.results = results;
    }

    public List<Map<String, Object>> getResults() {
        return Collections.unmodifiableList(results);
    }
}
