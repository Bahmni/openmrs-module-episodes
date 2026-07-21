/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.service.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.episodes.dao.EpisodeSearchDAO;
import org.openmrs.module.episodes.search.BuiltQuery;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.ConditionOperator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.criteria.SearchRequest;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeSearchServiceImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private EpisodeSearchDAO episodeSearchDAO;

    @InjectMocks
    private EpisodeSearchServiceImpl episodeSearchService;

    @Test
    public void shouldDelegateToDAOForValidRequest() {
        when(episodeSearchDAO.search(any(BuiltQuery.class))).thenReturn(Collections.emptyList());

        episodeSearchService.search(validRequest());

        verify(episodeSearchDAO, times(1)).search(any(BuiltQuery.class));
    }

    @Test
    public void shouldPassBuiltHqlToDAO() {
        when(episodeSearchDAO.search(any(BuiltQuery.class))).thenReturn(Collections.emptyList());
        ArgumentCaptor<BuiltQuery> captor = ArgumentCaptor.forClass(BuiltQuery.class);

        episodeSearchService.search(validRequest());

        verify(episodeSearchDAO).search(captor.capture());
        assertThat(captor.getValue().getHql(), notNullValue());
        assertThat(captor.getValue().getHql(), containsString("e.dateStarted > :param0"));
    }

    @Test
    public void shouldThrowBeforeCallingDAOWhenOrOperatorUsed() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("'OR'");

        Condition criteria = new Condition();
        criteria.setOperator(ConditionOperator.OR);
        criteria.setConditions(Collections.singletonList(leaf("episodeOfCare.startDate", "gt", "2024-01-01")));

        SearchRequest request = new SearchRequest();
        request.setEntity("episodeOfCare");
        request.setCriteria(criteria);

        episodeSearchService.search(request);

        verify(episodeSearchDAO, times(0)).search(any());
    }

    @Test
    public void shouldThrowBeforeCallingDAOForUnknownField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown search field");

        SearchRequest request = new SearchRequest();
        request.setEntity("episodeOfCare");
        request.setCriteria(leaf("episode.unknownField", "eq", "val"));

        episodeSearchService.search(request);

        verify(episodeSearchDAO, times(0)).search(any());
    }

    @Test
    public void shouldReturnEmptyListWhenNoResults() {
        when(episodeSearchDAO.search(any(BuiltQuery.class))).thenReturn(Collections.emptyList());

        assertThat(episodeSearchService.search(validRequest()).size(), is(0));
    }

    private SearchRequest validRequest() {
        SearchRequest request = new SearchRequest();
        request.setEntity("episodeOfCare");
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
