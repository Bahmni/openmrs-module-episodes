    /*
     * This Source Code Form is subject to the terms of the Mozilla Public License,
     * v. 2.0. If a copy of the MPL was not distributed with this file, You can
     * obtain one at https://www.bahmni.org/license/mplv2hd.
     *
     * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
     * graphic logo is a trademark of OpenMRS Inc.
     */

    package org.openmrs.module.episodes.search;

    import org.junit.Before;
    import org.junit.Rule;
    import org.junit.Test;
    import org.junit.rules.ExpectedException;
    import org.junit.runner.RunWith;
    import org.mockito.Mock;
    import org.mockito.runners.MockitoJUnitRunner;
    import org.openmrs.module.episodes.dao.PatientProgramSearchDAO;
    import org.openmrs.module.episodes.search.builder.PatientProgramResponseBuilder;
    import org.openmrs.module.episodes.search.impl.PatientProgramSearchServiceImpl;
    import org.openmrs.module.episodes.search.model.SearchCondition;
    import org.openmrs.module.episodes.search.model.SearchRequest;
    import org.openmrs.module.episodes.search.validation.CriteriaValidator;

    import java.util.Collections;

    import static org.hamcrest.MatcherAssert.assertThat;
    import static org.hamcrest.Matchers.is;
    import static org.mockito.Matchers.any;
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
            searchService = new PatientProgramSearchServiceImpl(patientProgramSearchDAO, new CriteriaValidator(), new PatientProgramResponseBuilder());
        }

        @Test
        public void shouldDelegateToDAOForValidRequest() {
            when(patientProgramSearchDAO.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

            searchService.search(validRequest());

            verify(patientProgramSearchDAO, times(1)).search(any(SearchCondition.class));
        }

        @Test
        public void shouldDelegateToDAOWhenOrOperatorUsed() {
            when(patientProgramSearchDAO.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

            SearchCondition criteria = new SearchCondition();
            criteria.setOperator("or");
            criteria.setConditions(Collections.singletonList(leaf("episodeOfCare.startDate", "gt", "2024-01-01")));

            SearchRequest request = new SearchRequest();
            request.setEntity("patientProgram");
            request.setCriteria(criteria);

            searchService.search(request);

            verify(patientProgramSearchDAO, times(1)).search(any(SearchCondition.class));
        }

        @Test
        public void shouldReturnEmptyListWhenNoResults() {
            when(patientProgramSearchDAO.search(any(SearchCondition.class))).thenReturn(Collections.emptyList());

            assertThat(searchService.search(validRequest()).getResults().size(), is(0));
        }

        @Test
        public void shouldReturnPatientProgramEntity() {
            assertThat(searchService.getEntity(), is("patientProgram"));
        }

        private SearchRequest validRequest() {
            SearchRequest request = new SearchRequest();
            request.setEntity("patientProgram");
            request.setCriteria(leaf("episodeOfCare.startDate", "gt", "2024-01-01"));
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
