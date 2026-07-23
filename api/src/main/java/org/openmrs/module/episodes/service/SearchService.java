/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.episodes.constants.Privileges;
import org.openmrs.module.episodes.search.model.SearchRequest;

import java.util.List;
import java.util.Map;


public interface SearchService {

    String getEntity();

    @Authorized(value = {
            Privileges.GET_EPISODES,
            Privileges.GET_PROGRAMS,
            Privileges.GET_PROVIDER_ATTRIBUTE_TYPES,
            Privileges.GET_PATIENT_PROGRAM_ATTRIBUTE_TYPES,
            Privileges.GET_PATIENT_PROGRAMS,
            Privileges.GET_PATIENTS,
            Privileges.GET_PERSON_ATTRIBUTE_TYPES
    }, requireAll = true)
    List<Map<String, Object>> search(SearchRequest request);
}
