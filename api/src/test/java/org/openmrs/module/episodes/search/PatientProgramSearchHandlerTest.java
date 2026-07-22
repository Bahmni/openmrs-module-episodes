/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.search.impl.PatientProgramSearchHandler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.model.Condition;
import org.openmrs.module.episodes.search.validation.CriteriaValidator;
import org.openmrs.module.episodes.search.model.ConditionOperator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.model.SearchRequest;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PatientProgramSearchHandlerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PatientProgramSearchDAO patientProgramSearchDAO;

    private PatientProgramSearchHandler handler;

    @Before
    public void setUp() {
        handler = new PatientProgramSearchHandler(patientProgramSearchDAO, new CriteriaValidator());
    }

    @Test
    public void shouldDelegateToDAOForValidRequest() {
        when(patientProgramSearchDAO.search(any(Condition.class))).thenReturn(Collections.emptyList());

        handler.search(validRequest());

        verify(patientProgramSearchDAO, times(1)).search(any(Condition.class));
    }

    @Test
    public void shouldDelegateToDAOWhenOrOperatorUsed() {
        when(patientProgramSearchDAO.search(any(Condition.class))).thenReturn(Collections.emptyList());

        Condition criteria = new Condition();
        criteria.setOperator(ConditionOperator.OR);
        criteria.setConditions(Collections.singletonList(leaf("episodeOfCare.startDate", "gt", "2024-01-01")));

        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
        request.setCriteria(criteria);

        handler.search(request);

        verify(patientProgramSearchDAO, times(1)).search(any(Condition.class));
    }

    @Test
    public void shouldReturnEmptyListWhenNoResults() {
        when(patientProgramSearchDAO.search(any(Condition.class))).thenReturn(Collections.emptyList());

        assertThat(handler.search(validRequest()).size(), is(0));
    }

    @Test
    public void shouldReturnPatientProgramEntity() {
        assertThat(handler.getEntity(), is("patientProgram"));
    }

    private SearchRequest validRequest() {
        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
        request.setCriteria(leaf("episodeOfCare.startDate", "gt", "2024-01-01"));
        return request;
    }

    private Condition leaf(String field, String comparator, String value) {
        Condition c = new Condition();
        c.setField(field);
        c.setComparator(comparator);
        c.setValue(value);
        return c;
    }
}
