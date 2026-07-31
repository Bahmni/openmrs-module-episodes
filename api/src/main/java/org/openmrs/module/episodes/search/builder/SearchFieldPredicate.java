/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.builder;

import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;

import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface SearchFieldPredicate {
    Predicate build(QueryContext queryContext, String fieldName,
                    FieldComparator comparator, String value,
                    ConditionOperator operator);
}
