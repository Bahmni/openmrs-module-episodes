/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.ConditionOperator;
import org.openmrs.module.episodes.search.criteria.SearchRequest;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

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
    public void shouldThrowWhenEntityIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'entity'");

        SearchRequest request = new SearchRequest();
        request.setCriteria(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));

        validator.validate(request);
    }

    @Test
    public void shouldThrowWhenEntityIsNotSupported() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'patient'");

        SearchRequest request = new SearchRequest();
        request.setEntity("patient");
        request.setCriteria(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));

        validator.validate(request);
    }

    @Test
    public void shouldThrowWhenCriteriaIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'criteria'");

        SearchRequest request = new SearchRequest();
        request.setEntity("episodeOfCare");

        validator.validate(request);
    }

    @Test
    public void shouldThrowWhenOrOperatorUsedAtRoot() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'OR'");

        validator.validate(requestWith(group(ConditionOperator.OR,
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM))));
    }

    @Test
    public void shouldThrowWhenOrOperatorUsedInNestedGroup() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'OR'");

        Condition inner = group(ConditionOperator.OR,
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));
        Condition outer = group(ConditionOperator.AND, inner);

        validator.validate(requestWith(outer));
    }

    @Test
    public void shouldThrowWhenLeafComparatorIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("comparator");

        Condition leaf = new Condition();
        leaf.setField(SearchFields.EpisodeOfCare.START_DATE);
        leaf.setValue(DATE_FROM);

        validator.validate(requestWith(leaf));
    }

    @Test
    public void shouldThrowWhenLeafValueIsMissing() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("value");

        Condition leaf = new Condition();
        leaf.setField(SearchFields.EpisodeOfCare.START_DATE);
        leaf.setComparator(GT);

        validator.validate(requestWith(leaf));
    }

    @Test
    public void shouldThrowWhenGroupHasNoConditions() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("at least one condition");

        validator.validate(requestWith(group(ConditionOperator.AND)));
    }

    @Test
    public void shouldPassValidationForValidLeaf() {
        validator.validate(requestWith(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM)));
    }

    @Test
    public void shouldPassValidationForAndGroup() {
        validator.validate(requestWith(group(ConditionOperator.AND,
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM),
                leaf(SearchFields.EpisodeOfCare.START_DATE, LT, DATE_TO))));
    }

    @Test
    public void shouldPassValidationForNestedAndGroups() {
        Condition inner = group(ConditionOperator.AND,
                leaf(SearchFields.Patient.IDENTIFIER_KIND, EQ, "NATIONAL_ID"),
                leaf(SearchFields.Patient.IDENTIFIER_VALUE, EQ, "N456"));

        Condition outer = group(ConditionOperator.AND,
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM),
                inner);

        validator.validate(requestWith(outer));
    }

    private SearchRequest requestWith(Condition criteria) {
        SearchRequest request = new SearchRequest();
        request.setEntity("episodeOfCare");
        request.setCriteria(criteria);
        return request;
    }

    private Condition leaf(String field, String comparator, String value) {
        Condition c = new Condition();
        c.setField(field);
        c.setComparator(comparator);
        c.setValue(value);
        return c;
    }

    private Condition group(ConditionOperator operator, Condition... children) {
        Condition c = new Condition();
        c.setOperator(operator);
        c.setConditions(children.length > 0 ? Arrays.asList(children) : Collections.emptyList());
        return c;
    }
}
