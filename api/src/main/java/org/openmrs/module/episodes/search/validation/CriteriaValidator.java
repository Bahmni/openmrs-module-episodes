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
import org.openmrs.module.episodes.search.model.SearchCriteria;
import org.openmrs.module.episodes.search.model.SearchRequest;

import java.util.ArrayList;
import java.util.List;

public class CriteriaValidator {

    public void validateRequest(SearchRequest request) {
        if (request.getCriteria() == null) {
            throw new InvalidSearchCriteriaException("Request must include 'criteria'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        List<String> errors = new ArrayList<>();
        validateCondition(request.getCriteria(), errors);
        if (!errors.isEmpty()) {
            throw new InvalidSearchCriteriaException(errors, SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateCondition(SearchCriteria condition, List<String> errors) {
        if (condition.isLeaf()) {
            validateLeaf(condition, errors);
        } else if (condition.isGroup()) {
            validateGroup(condition, errors);
        } else {
            errors.add("Each condition must be either a leaf {field, comparator, value} or a group {operator, conditions}");
        }
    }

    private void validateLeaf(SearchCriteria leaf, List<String> errors) {
        if (leaf.getComparator() == null) {
            errors.add("Leaf condition for field '" + leaf.getField() + "' is missing 'comparator'. Supported: eq, gt, lt");
        }
        if (leaf.getValue() == null || leaf.getValue().isEmpty()) {
            errors.add("Leaf condition for field '" + leaf.getField() + "' is missing 'value'");
        }
    }

    private void validateGroup(SearchCriteria group, List<String> errors) {
        if (group.getConditions() == null || group.getConditions().isEmpty()) {
            errors.add("A group condition must have at least one condition in 'conditions'");
        } else {
            for (SearchCriteria child : group.getConditions()) {
                validateCondition(child, errors);
            }
        }
    }
}
