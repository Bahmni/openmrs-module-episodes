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
import org.openmrs.module.episodes.search.constants.EpisodeSearchPrivileges;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResultDTO;
import org.openmrs.module.episodes.search.criteria.SearchRequest;

import java.util.List;

public interface EpisodeSearchService {

    @Authorized(value = {
            EpisodeSearchPrivileges.GET_EPISODES,
            EpisodeSearchPrivileges.GET_PROGRAMS,
            EpisodeSearchPrivileges.GET_PROVIDER_ATTRIBUTE_TYPES,
            EpisodeSearchPrivileges.GET_PATIENT_PROGRAM_ATTRIBUTE_TYPES,
            EpisodeSearchPrivileges.GET_PATIENT_PROGRAMS,
            EpisodeSearchPrivileges.GET_PATIENTS,
            EpisodeSearchPrivileges.GET_PERSON_ATTRIBUTE_TYPES
    }, requireAll = true)
    List<EpisodeSearchResultDTO> search(SearchRequest request);
}
