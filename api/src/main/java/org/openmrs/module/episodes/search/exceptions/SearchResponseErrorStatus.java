/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.exceptions;

import lombok.Getter;

@Getter
public enum SearchResponseErrorStatus {

    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    SearchResponseErrorStatus(int code) {
        this.code = code;
    }

}
