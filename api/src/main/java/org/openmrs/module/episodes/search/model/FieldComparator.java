/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.model;

import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;

public enum FieldComparator {

    EQ, GT, LT;

    public static FieldComparator resolve(String value) {
        if (value == null) return null;
        try {
            return FieldComparator.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidSearchCriteriaException(
                    "Unknown comparator: '" + value + "'. Supported: eq, gt, lt",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
