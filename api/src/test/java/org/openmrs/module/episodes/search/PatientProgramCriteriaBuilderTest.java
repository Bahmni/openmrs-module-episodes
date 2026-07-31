/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.builder.PatientProgramCriteriaBuilder;
import org.openmrs.module.episodes.search.builder.QueryContext;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.model.SearchCondition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class PatientProgramCriteriaBuilderTest {

    private static final String EQ = "eq";
    private static final String GT = "gt";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final PatientProgramCriteriaBuilder criteriaBuilder = new PatientProgramCriteriaBuilder();

    private final QueryContext queryContext = createMockQueryContext();

    @Test
    public void shouldThrowForUnknownSearchField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown search field: 'patient.unknownField'");

        criteriaBuilder.apply(queryContext, createLeafCriteria("patient.unknownField", EQ, "value"));
    }

    @Test
    public void shouldThrowWhenGtUsedOnTextField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("is not supported for field 'program.uuid'");

        criteriaBuilder.apply(queryContext, createLeafCriteria(SearchFields.PROGRAM_UUID, GT, "uuid"));
    }


    @Test
    public void shouldThrowForInvalidDateFormat() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Invalid date format");

        criteriaBuilder.apply(queryContext, createLeafCriteria(SearchFields.EOC_START_DATE, GT, "01/01/2024"));
    }

    @SuppressWarnings("unchecked")
    private QueryContext createMockQueryContext() {
        CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
        Root<Episode> mockEpisodeRoot = mock(Root.class);
        From<?, ?> mockPatientProgramJoin = mock(From.class);
        List<Predicate> predicates = new ArrayList<>();
        return new QueryContext(mockCriteriaBuilder, mockEpisodeRoot, mockPatientProgramJoin, predicates);
    }

    private SearchCondition createLeafCriteria(String fieldName, String comparator, String value) {
        SearchCondition criteria = new SearchCondition();
        criteria.setField(fieldName);
        criteria.setComparator(comparator);
        criteria.setValue(value);
        return criteria;
    }

}
