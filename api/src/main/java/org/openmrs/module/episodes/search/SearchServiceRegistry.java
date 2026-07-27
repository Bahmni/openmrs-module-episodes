/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.service.SearchService;

import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchServiceRegistry {

    private final Map<String, SearchService> registry;

    public SearchServiceRegistry(List<SearchService> services) {
        this.registry = new HashMap<>();
        for (SearchService service : services) {
            registry.put(service.getEntity(), service);
        }
    }

    public SearchService resolve(String entity) {
        if (entity == null || entity.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        SearchService service = registry.get(entity);
        if (service == null) {
            throw new InvalidSearchCriteriaException(
                    "Entity '" + entity + "' is not supported. Supported entities: " + registry.keySet(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        return service;
    }
}
