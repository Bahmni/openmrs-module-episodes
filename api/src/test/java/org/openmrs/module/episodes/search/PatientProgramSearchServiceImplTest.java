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
    import org.junit.Rule;
    import org.junit.Test;
    import org.junit.rules.ExpectedException;
    import org.junit.runner.RunWith;
    import org.mockito.Mock;
    import org.mockito.runners.MockitoJUnitRunner;
    import org.openmrs.module.episodes.Episode;
    import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
    import org.openmrs.module.episodes.search.builder.PatientProgramResponseBuilder;
    import org.openmrs.module.episodes.search.dto.EpisodeSearchResponse;
    import org.openmrs.module.episodes.search.impl.PatientProgramSearchServiceImpl;
    import org.bahmni.search.model.SearchCondition;
    import org.openmrs.module.episodes.search.dto.SearchRequest;
    import org.openmrs.module.episodes.search.validation.SearchCriteriaValidator;

    import java.util.Arrays;
    import java.util.Collections;

    import static org.hamcrest.MatcherAssert.assertThat;
    import static org.hamcrest.Matchers.is;
    import static org.hamcrest.Matchers.notNullValue;
    import static org.hamcrest.Matchers.nullValue;
    import static org.mockito.Matchers.any;
    import static org.mockito.Matchers.anyInt;
    import static org.mockito.Matchers.anyString;
    import static org.mockito.Matchers.eq;
    import static org.mockito.Mockito.*;

    @RunWith(MockitoJUnitRunner.class)
    public class PatientProgramSearchServiceImplTest {

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Mock
        private PatientProgramSearchDAO patientProgramSearchDAO;

        private PatientProgramSearchServiceImpl searchService;

        @Before
        public void setUp() {
            searchService = new PatientProgramSearchServiceImpl(
                    patientProgramSearchDAO, new SearchCriteriaValidator(), new PatientProgramResponseBuilder());
        }

        @Test
        public void shouldDelegateToDAOForValidRequest() {
            mockDaoReturns(Collections.<Episode>emptyList());

            searchService.search(validRequest());

            verify(patientProgramSearchDAO, times(1)).search(
                    any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(101));
        }

        @Test
        public void shouldDelegateToDAOWhenOrOperatorUsed() {
            mockDaoReturns(Collections.<Episode>emptyList());

            SearchCondition criteria = new SearchCondition();
            criteria.setOperator("or");
            criteria.setConditions(Collections.singletonList(leaf("episodeOfCare.startDate", "gt", "2024-01-01")));

            SearchRequest request = new SearchRequest();
            request.setEntity("patientProgram");
            request.setCriteria(criteria);

            searchService.search(request);

            verify(patientProgramSearchDAO, times(1)).search(
                    any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(101));
        }

        @Test
        public void shouldReturnEmptyListWhenNoResults() {
            mockDaoReturns(Collections.<Episode>emptyList());

            assertThat(searchService.search(validRequest()).getResults().size(), is(0));
        }

        @Test
        public void shouldReturnNullNextCursorWhenNoMoreResults() {
            mockDaoReturns(Collections.<Episode>emptyList());

            EpisodeSearchResponse response = searchService.search(validRequest());

            assertThat(response.getMeta().getPagination().getNextCursor(), is(nullValue()));
        }

        @Test
        public void shouldDecodeCursorAndPassToDao() {
            String cursor = CursorCodec.encode(50);
            SearchRequest request = requestWithPagination(10, cursor, "next");
            mockDaoReturns(Collections.<Episode>emptyList());

            searchService.search(request);

            verify(patientProgramSearchDAO, times(1)).search(
                    any(SearchCondition.class), eq(50L), anyString(), eq("next"), eq(11));
        }

        @Test
        public void shouldCapLimitToMax500() {
            SearchRequest request = requestWithPagination(1000, null, null);
            mockDaoReturns(Collections.<Episode>emptyList());

            searchService.search(request);

            verify(patientProgramSearchDAO, times(1)).search(
                    any(SearchCondition.class), (Long) isNull(), anyString(), (String) isNull(), eq(501));
        }

        @Test
        public void shouldCountWhenIncludeTotalCountIsTrue() {
            SearchRequest request = validRequest();
            SearchRequestMeta meta = new SearchRequestMeta();
            meta.setIncludeTotalCount(true);
            request.setMeta(meta);
            mockDaoReturns(Collections.<Episode>emptyList());
            when(patientProgramSearchDAO.count(any(SearchCondition.class))).thenReturn(42L);

            EpisodeSearchResponse response = searchService.search(request);

            assertThat(response.getMeta().getTotalCount(), is(42L));
            verify(patientProgramSearchDAO, times(1)).count(any(SearchCondition.class));
        }

        @Test
        public void shouldNotCountWhenIncludeTotalCountIsNotSet() {
            mockDaoReturns(Collections.<Episode>emptyList());

            EpisodeSearchResponse response = searchService.search(validRequest());

            assertThat(response.getMeta().getTotalCount(), is(nullValue()));
            verify(patientProgramSearchDAO, never()).count(any(SearchCondition.class));
        }

        private void mockDaoReturns(java.util.List<Episode> episodes) {
            when(patientProgramSearchDAO.search(
                    any(SearchCondition.class), any(), anyString(), any(), anyInt()))
                    .thenReturn(episodes);
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
