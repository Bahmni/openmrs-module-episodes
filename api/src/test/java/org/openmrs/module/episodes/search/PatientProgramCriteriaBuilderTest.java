/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.hibernate.Criteria;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.model.Condition;

import static org.mockito.Mockito.mock;

public class PatientProgramCriteriaBuilderTest {

    private static final String EQ = "eq";
    private static final String GT = "gt";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final PatientProgramCriteriaBuilder builder = new PatientProgramCriteriaBuilder();

    private final Criteria criteria = mock(Criteria.class);

    @Test
    public void shouldThrowForUnknownSearchField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown search field: 'patient.unknownField'");

        builder.applyCondition(criteria, leaf("patient.unknownField", EQ, "value"));
    }

    @Test
    public void shouldThrowWhenGtUsedOnTextField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Only 'eq' comparator is supported for field 'program.uuid'");

        builder.applyCondition(criteria, leaf(SearchFields.Program.UUID, GT, "uuid"));
    }

    @Test
    public void shouldThrowWhenComparatorIsNull() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Missing comparator for field 'episodeOfCare.startDate'");

        builder.applyCondition(criteria, leaf(SearchFields.EpisodeOfCare.START_DATE, null, "2024-01-01"));
    }

    @Test
    public void shouldThrowForInvalidDateFormat() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Invalid date format");

        builder.applyCondition(criteria, leaf(SearchFields.EpisodeOfCare.START_DATE, GT, "01/01/2024"));
    }

    private Condition leaf(String field, String comparator, String value) {
        Condition c = new Condition();
        c.setField(field);
        c.setComparator(comparator);
        c.setValue(value);
        return c;
    }

}
