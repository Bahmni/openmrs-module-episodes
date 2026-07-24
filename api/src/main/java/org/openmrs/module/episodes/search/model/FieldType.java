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

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum FieldType {

    STRING(EnumSet.of(FieldComparator.EQ)),
    DATE(EnumSet.of(FieldComparator.GT, FieldComparator.LT));

    private final Set<FieldComparator> supportedComparators;

    FieldType(Set<FieldComparator> supportedComparators) {
        this.supportedComparators = supportedComparators;
    }

    public boolean supports(FieldComparator comparator) {
        return supportedComparators.contains(comparator);
    }
}
