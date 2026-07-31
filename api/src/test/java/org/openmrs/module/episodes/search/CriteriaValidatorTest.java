/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.episodes.search.validation.CriteriaValidator;
import org.bahmni.search.model.SearchRequest;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CriteriaValidatorTest {

    private static final String GT = "gt";
    private static final String LT = "lt";
    private static final String EQ = "eq";
    private static final String DATE_FROM = "2024-01-01";
    private static final String DATE_TO = "2024-12-31";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final CriteriaValidator validator = new CriteriaValidator();

    @Test
    public void shouldThrowWhenCriteriaIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'criteria'");

        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");

        validator.validateRequest(request);
    }

    @Test
    public void shouldPassValidationForOrOperatorAtRoot() {
        validator.validateRequest(requestWith(group("or",
                leaf(SearchFields.EOC_START_DATE, GT, DATE_FROM))));
    }

    @Test
    public void shouldPassValidationForOrOperatorInNestedGroup() {
        SearchCondition inner = group("or",
                leaf(SearchFields.EOC_START_DATE, GT, DATE_FROM));
        SearchCondition outer = group("and", inner);

        validator.validateRequest(requestWith(outer));
    }

    @Test
    public void shouldThrowWhenLeafComparatorIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("comparator");

        SearchCondition leaf = new SearchCondition();
        leaf.setField(SearchFields.EOC_START_DATE);
        leaf.setValue(DATE_FROM);

        validator.validateRequest(requestWith(leaf));
    }

    @Test
    public void shouldThrowWhenLeafValueIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("value");

        SearchCondition leaf = new SearchCondition();
        leaf.setField(SearchFields.EOC_START_DATE);
        leaf.setComparator(GT);

        validator.validateRequest(requestWith(leaf));
    }

    @Test
    public void shouldThrowWhenGroupHasNoConditions() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("must be either a leaf");

        validator.validateRequest(requestWith(group("and")));
    }

    @Test
    public void shouldPassValidationForValidLeaf() {
        validator.validateRequest(requestWith(leaf(SearchFields.EOC_START_DATE, GT, DATE_FROM)));
    }

    @Test
    public void shouldPassValidationForAndGroup() {
        validator.validateRequest(requestWith(group("and",
                leaf(SearchFields.EOC_START_DATE, GT, DATE_FROM),
                leaf(SearchFields.EOC_START_DATE, LT, DATE_TO))));
    }

    @Test
    public void shouldCollectMultipleErrorsWhenBothComparatorAndValueAreMissing() {
        SearchCondition leaf = new SearchCondition();
        leaf.setField(SearchFields.EOC_START_DATE);
        // both comparator and value are missing

        try {
            validator.validateRequest(requestWith(leaf));
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(2, e.getMessages().size());
        }
    }

    @Test
    public void shouldCollectErrorsFromMultipleChildConditions() {
        SearchCondition leaf1 = new SearchCondition();
        leaf1.setField(SearchFields.EOC_START_DATE);
        // missing comparator and value

        SearchCondition leaf2 = new SearchCondition();
        leaf2.setField(SearchFields.PROGRAM_LOCATION);
        leaf2.setComparator(EQ);
        // missing value

        SearchCondition root = group("and", leaf1, leaf2);

        try {
            validator.validateRequest(requestWith(root));
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(3, e.getMessages().size());
        }
    }

    @Test
    public void shouldPassValidationForNestedAndGroups() {
        SearchCondition inner = group("and",
                leaf(SearchFields.PATIENT_IDENTIFIER_KIND, EQ, "NATIONAL_ID"),
                leaf(SearchFields.PATIENT_IDENTIFIER_VALUE, EQ, "N456"));

        SearchCondition outer = group("and",
                leaf(SearchFields.EOC_START_DATE, GT, DATE_FROM),
                inner);

        validator.validateRequest(requestWith(outer));
    }

    private SearchRequest requestWith(SearchCondition criteria) {
        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
        request.setCriteria(criteria);
        return request;
    }

    private SearchCondition leaf(String field, String comparator, String value) {
        SearchCondition criteria = new SearchCondition();
        criteria.setField(field);
        criteria.setComparator(comparator);
        criteria.setValue(value);
        return criteria;
    }

    private SearchCondition group(String operator, SearchCondition... children) {
        SearchCondition criteria = new SearchCondition();
        criteria.setOperator(operator);
        criteria.setConditions(children.length > 0 ? Arrays.asList(children) : Collections.emptyList());
        return criteria;
    }
}
