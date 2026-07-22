/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.search.query.PatientProgramQueryBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.ConditionOperator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class PatientProgramQueryBuilderTest {

    private static final String EQ = "eq";
    private static final String GT = "gt";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final PatientProgramQueryBuilder builder = new PatientProgramQueryBuilder();

    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    private final CriteriaQuery<PatientProgram> cq = mock(CriteriaQuery.class);
    @SuppressWarnings("unchecked")
    private final Root<PatientProgram> root = mock(Root.class);

    @Test
    public void shouldThrowForUnknownSearchField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown search field: 'patient.unknownField'");

        builder.buildPredicates(cb, root, cq, new ArrayList<>(), leaf("patient.unknownField", EQ, "value"));
    }

    @Test
    public void shouldThrowWhenGtUsedOnTextField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("not supported for field 'program.uuid'");

        builder.buildPredicates(cb, root, cq, new ArrayList<>(), leaf(SearchFields.Program.UUID, GT, "uuid"));
    }

    @Test
    public void shouldThrowWhenComparatorIsNull() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown comparator");

        builder.buildPredicates(cb, root, cq, new ArrayList<>(),
                leaf(SearchFields.EpisodeOfCare.START_DATE, null, "2024-01-01"));
    }

    @Test
    public void shouldThrowForInvalidDateFormat() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Invalid date format");

        builder.buildPredicates(cb, root, cq, new ArrayList<>(),
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, "01/01/2024"));
    }

    private Condition leaf(String field, String comparator, String value) {
        Condition c = new Condition();
        c.setField(field);
        c.setComparator(comparator);
        c.setValue(value);
        return c;
    }

    private Condition group(Condition... children) {
        Condition c = new Condition();
        c.setOperator(ConditionOperator.AND);
        c.setConditions(Arrays.asList(children));
        return c;
    }
}
