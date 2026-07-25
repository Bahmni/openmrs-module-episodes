/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.model;

import java.util.List;
import java.util.Map;

/**
 * Standard response envelope for context-aware search APIs.
 * This interface will be extracted to a common library when we refactor
 * to share search contracts across modules.
 */
public interface ContextSearchResponse {

    String getContext();

    SearchResponseMeta getMetaData();

    List<Map<String, Object>> getResults();

    List<Map<String, String>> getLinks();
}
