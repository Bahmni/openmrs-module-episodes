/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.model;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Represents an error in a search response envelope.
 * Contains the HTTP status code and one or more human-readable error messages.
 */
@Getter
public class SearchError {

    private final int status;
    private final List<String> messages;

    public SearchError(int status, List<String> messages) {
        this.status = status;
        this.messages = Collections.unmodifiableList(messages);
    }

    public SearchError(int status, String message) {
        this(status, Collections.singletonList(message));
    }
}
