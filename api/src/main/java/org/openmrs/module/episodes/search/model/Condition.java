/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Condition {

    private String field;

    @Setter(AccessLevel.NONE)
    private FieldComparator comparator;

    private String value;

    @Setter(AccessLevel.NONE)
    private ConditionOperator operator;

    private List<Condition> conditions;

    public void setOperator(String operator) {
        this.operator = ConditionOperator.fromString(operator);
    }

    public void setOperator(ConditionOperator operator) {
        this.operator = operator;
    }

    public void setComparator(String comparator) {
        this.comparator = FieldComparator.fromString(comparator);
    }

    public boolean isLeaf() {
        return field != null && operator == null;
    }

    public boolean isGroup() {
        return operator != null && field == null;
    }
}
