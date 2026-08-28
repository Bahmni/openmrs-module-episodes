/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.SearchRequestMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.episodes.dao.EpisodePatientProgram;
import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
import org.openmrs.module.episodes.search.builder.PatientProgramResponseBuilder;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
import org.openmrs.module.episodes.search.impl.PatientProgramSearchServiceImpl;
import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.episodes.search.dto.SearchRequest;
import org.openmrs.module.episodes.search.validation.SearchCriteriaValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class PatientProgramSearchServiceImplTest {

    @Mock
    private PatientProgramSearchDAO patientProgramSearchDAO;

    @Mock
    private AdministrationService administrationService;

    private PatientProgramSearchServiceImpl searchService;

    @Before
    public void setUp() {
        searchService = new PatientProgramSearchServiceImpl(
                patientProgramSearchDAO, new SearchCriteriaValidator(), new PatientProgramResponseBuilder(),
                administrationService);

    }

    @Test
    public void shouldDelegateToDAOForValidRequest() {
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(validRequest());

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(101));
    }

    @Test
    public void shouldDelegateToDAOWhenOrOperatorUsed() {
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        SearchCondition criteria = new SearchCondition();
        criteria.setOperator("or");
        criteria.setConditions(Collections.singletonList(leaf("episodeOfCare.startDate", "gt", "2024-01-01")));

        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
        request.setCriteria(criteria);

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(101));
    }

    @Test
    public void shouldReturnEmptyListWhenNoResults() {
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        assertThat(searchService.search(validRequest()).getResults().size(), is(0));
    }

    @Test
    public void shouldReturnNullNextCursorWhenNoMoreResults() {
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        EpisodeSearchResponse response = searchService.search(validRequest());

        assertThat(response.getMeta().getPagination().getNextCursor(), is(nullValue()));
    }

    @Test
    public void shouldDecodeCursorAndPassToDao() {
        String cursor = CursorCodec.encode("patientProgram", 50);
        SearchRequest request = requestWithPagination(10, cursor, "next");
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), eq(50L), anyString(), eq("next"), eq(11));
    }

    @Test
    public void shouldCapLimitToMax500() {
        SearchRequest request = requestWithPagination(1000, null, null);
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(501));
    }

    @Test
    public void shouldUseConfiguredDefaultLimitFromGlobalProperty() {
        when(administrationService.getGlobalProperty("bahmni.search.pagination.defaultLimit")).thenReturn("20");

        SearchRequest request = validRequest();
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(21));
    }

    @Test
    public void shouldUseConfiguredMaxLimitFromGlobalProperty() {
        when(administrationService.getGlobalProperty("bahmni.search.pagination.maxLimit")).thenReturn("50");

        SearchRequest request = requestWithPagination(1000, null, null);
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(51));
    }

    @Test
    public void shouldFallbackToDefaultWhenGlobalPropertyIsInvalid() {
        when(administrationService.getGlobalProperty("bahmni.search.pagination.defaultLimit")).thenReturn("not-a-number");

        SearchRequest request = validRequest();
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(101));
    }

    @Test
    public void shouldFallbackToDefaultMaxLimitWhenConfiguredMaxLimitGlobalPropertyIsNonPositive() {
        when(administrationService.getGlobalProperty("bahmni.search.pagination.maxLimit")).thenReturn("0");

        SearchRequest request = requestWithPagination(1000, null, null);
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(request);

        verify(patientProgramSearchDAO, times(1)).findMatchingIds(
                any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(501));
    }

    @Test
    public void shouldCountWhenIncludeTotalCountIsTrue() {
        SearchRequest request = validRequest();
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setIncludeTotalCount(true);
        request.setMeta(meta);
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());
        when(patientProgramSearchDAO.count(any(SearchCondition.class))).thenReturn(42L);

        EpisodeSearchResponse response = searchService.search(request);

        assertThat(response.getMeta().getTotalCount(), is(42L));
        verify(patientProgramSearchDAO, times(1)).count(any(SearchCondition.class));
    }

    @Test
    public void shouldNotCountWhenIncludeTotalCountIsNotSet() {
        mockDaoReturns(Collections.<EpisodePatientProgram>emptyList());

        EpisodeSearchResponse response = searchService.search(validRequest());

        assertThat(response.getMeta().getTotalCount(), is(nullValue()));
        verify(patientProgramSearchDAO, never()).count(any(SearchCondition.class));
    }

    @Test
    public void shouldFetchByIdsReturnedFromFindMatchingIds() {
        when(patientProgramSearchDAO.findMatchingIds(
                any(SearchCondition.class), any(), anyString(), any(), anyInt()))
                .thenReturn(Arrays.asList(1, 2, 3));
        when(patientProgramSearchDAO.findByIds(Arrays.asList(1, 2, 3)))
                .thenReturn(Collections.<EpisodePatientProgram>emptyList());

        searchService.search(validRequest());

        verify(patientProgramSearchDAO, times(1)).findByIds(Arrays.asList(1, 2, 3));
    }

    private void mockDaoReturns(List<EpisodePatientProgram> pairs) {
        when(patientProgramSearchDAO.findMatchingIds(
                any(SearchCondition.class), any(), anyString(), any(), anyInt()))
                .thenReturn(Collections.<Integer>emptyList());
        when(patientProgramSearchDAO.findByIds(anyListOf(Integer.class)))
                .thenReturn(pairs);
    }


    private SearchRequest validRequest() {
        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
        request.setCriteria(leaf("episodeOfCare.startDate", "gt", "2024-01-01"));
        return request;
    }

    private SearchRequest requestWithPagination(int limit, String cursor, String direction) {
        SearchRequest request = validRequest();
        SearchRequestMeta meta = new SearchRequestMeta();
        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(limit);
        pagination.setCursor(cursor);
        pagination.setDirection(direction);
        meta.setPagination(pagination);
        request.setMeta(meta);
        return request;
    }

    private SearchCondition leaf(String field, String comparator, String value) {
        SearchCondition criteria = new SearchCondition();
        criteria.setField(field);
        criteria.setComparator(comparator);
        criteria.setValue(value);
        return criteria;
    }
}
