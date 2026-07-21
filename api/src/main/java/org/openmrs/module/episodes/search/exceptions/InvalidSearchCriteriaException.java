/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.exceptions;

public class InvalidSearchCriteriaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final SearchResponseErrorStatus status;

    public InvalidSearchCriteriaException(String message, SearchResponseErrorStatus status) {
        super(message);
        this.status = status;
    }

    public SearchResponseErrorStatus getStatus() {
        return status;
    }
}
