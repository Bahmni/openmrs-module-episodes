/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.validation;

import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.exceptions.SearchResponseErrorStatus;
import org.openmrs.module.episodes.search.model.Condition;
import org.openmrs.module.episodes.search.model.SearchRequest;

public class CriteriaValidator {

    public void validate(SearchRequest request) {
        if (request.getCriteria() == null) {
            throw new InvalidSearchCriteriaException("Request must include 'criteria'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        validateCondition(request.getCriteria());
    }

    private void validateCondition(Condition condition) {
        if (condition.isLeaf()) {
            validateLeaf(condition);
        } else if (condition.isGroup()) {
            validateGroup(condition);
        } else {
            throw new InvalidSearchCriteriaException(
                    "Each condition must be either a leaf {field, comparator, value} or a group {operator, conditions}", SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateLeaf(Condition leaf) {
        if (leaf.getComparator() == null) {
            throw new InvalidSearchCriteriaException(
                    "Leaf condition for field '" + leaf.getField() + "' is missing or has an unknown 'comparator'. Supported: eq, gt, lt", SearchResponseErrorStatus.BAD_REQUEST);
        }
        if (leaf.getValue() == null || leaf.getValue().isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Leaf condition for field '" + leaf.getField() + "' is missing 'value'", SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateGroup(Condition group) {
        if (group.getConditions() == null || group.getConditions().isEmpty()) {
            throw new InvalidSearchCriteriaException("A group condition must have at least one condition in 'conditions'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        for (Condition child : group.getConditions()) {
            validateCondition(child);
        }
    }
}
