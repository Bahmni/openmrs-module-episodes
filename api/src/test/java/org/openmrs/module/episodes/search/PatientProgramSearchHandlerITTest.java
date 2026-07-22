/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.model.Condition;
import org.openmrs.module.episodes.search.model.ConditionOperator;
import org.openmrs.module.episodes.search.model.SearchRequest;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PatientProgramSearchHandlerITTest extends BaseModuleContextSensitiveTest {

    private static final String EQ = "eq";
    private static final String GT = "gt";
    private static final String LT = "lt";
    private static final String DATE_FROM = "2024-01-01";
    private static final String DATE_TO = "2024-12-31";

    @Autowired
    private SearchService searchService;

    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private PersonService personService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private LocationService locationService;

    @Test
    public void shouldReturnPatientProgramsLinkedToProgram() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        saveEpisodeWith(pp);

        List<Map<String, Object>> results = searchService.search(
                requestWith(leaf(SearchFields.Program.UUID, EQ, pp.getProgram().getUuid()))
        );

        assertThat(results.size(), is(1));
        assertThat(results.get(0).get("uuid"), is(pp.getUuid()));
    }

    @Test
    public void shouldReturnPatientProgramsWithEpisodesInStartDateRange() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.addPatientProgram(pp);
        episode.setDateStarted(date(2024, 6, 15));
        episodeService.save(episode);

        Condition range = group(
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM),
                leaf(SearchFields.EpisodeOfCare.START_DATE, LT, DATE_TO)
        );

        List<Map<String, Object>> results = searchService.search(requestWith(range));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).get("uuid"), is(pp.getUuid()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnPatientProgramsWithResponseStructure() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        saveEpisodeWith(pp);

        List<Map<String, Object>> results = searchService.search(
                requestWith(leaf(SearchFields.Program.UUID, EQ, pp.getProgram().getUuid()))
        );

        Map<String, Object> result = results.get(0);
        assertThat(result.get("uuid"), notNullValue());
        assertThat(result.containsKey("dateEnrolled"), is(true));
        assertThat(result.containsKey("dateCompleted"), is(true));
        assertThat(result.get("patient"), notNullValue());
        assertThat(result.get("program"), notNullValue());
        assertThat(result.get("episode"), notNullValue());

        Map<String, Object> patient = (Map<String, Object>) result.get("patient");
        assertThat(patient.get("uuid"), notNullValue());
        assertThat(patient.get("name"), notNullValue());
        assertThat(patient.containsKey("preferredName"), is(false));
    }

    @Test
    public void shouldReturnPatientProgramsForCareManager() {
        Provider provider = createProvider("Dr Smith");
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.addPatientProgram(pp);
        episode.setCareManager(provider);
        episodeService.save(episode);

        List<Map<String, Object>> results = searchService.search(
                requestWith(leaf(SearchFields.EpisodeOfCare.CARE_MANAGER, EQ, provider.getUuid()))
        );

        assertThat(results.size(), is(1));
        assertThat(results.get(0).get("uuid"), is(pp.getUuid()));
    }

    @Test
    public void shouldReturnPatientProgramsForProgramLocation() {
        Location location = locationService.getLocation(1);
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        pp.setLocation(location);
        programWorkflowService.savePatientProgram(pp);
        saveEpisodeWith(pp);

        List<Map<String, Object>> results = searchService.search(
                requestWith(leaf(SearchFields.Program.LOCATION, EQ, location.getUuid()))
        );

        assertThat(results.size(), is(1));
        assertThat(results.get(0).get("uuid"), is(pp.getUuid()));
    }

    @Test
    public void shouldReturnEmptyListWhenNoCriteriaMatch() {
        List<Map<String, Object>> results = searchService.search(
                requestWith(leaf(SearchFields.Program.UUID, EQ, "non-existent-uuid"))
        );

        assertThat(results.size(), is(0));
    }

    @Test
    public void shouldSupportOrOperator() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        saveEpisodeWith(pp);

        Condition criteria = new Condition();
        criteria.setOperator(ConditionOperator.OR);
        criteria.setConditions(Arrays.asList(
                leaf(SearchFields.Program.UUID, EQ, pp.getProgram().getUuid()),
                leaf(SearchFields.Program.UUID, EQ, "non-existent-uuid")
        ));

        List<Map<String, Object>> results = searchService.search(requestWith(criteria));
        assertThat(results.size(), is(1));
        assertThat(results.get(0).get("uuid"), is(pp.getUuid()));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionForUnknownSearchField() {
        searchService.search(requestWith(leaf("episode.unknownField", EQ, "value")));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionForUnsupportedComparator() {
        searchService.search(requestWith(leaf(SearchFields.Program.UUID, GT, "uuid")));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionForInvalidDateFormat() {
        searchService.search(requestWith(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, "01/01/2024")));
    }

    private Episode saveEpisodeWith(PatientProgram pp) {
        Episode episode = new Episode();
        episode.addPatientProgram(pp);
        episode.setPatient(pp.getPatient());
        episodeService.save(episode);
        return episode;
    }

    private Provider createProvider(String name) {
        Person person = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName(name);
        personName.setFamilyName("Provider");
        person.addName(personName);
        personService.savePerson(person);
        Provider provider = new Provider();
        provider.setPerson(person);
        providerService.saveProvider(provider);
        return provider;
    }

    private Date date(int year, int month, int day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private SearchRequest requestWith(Condition criteria) {
        SearchRequest request = new SearchRequest();
        request.setEntity("patientProgram");
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

    private Condition group(Condition... children) {
        Condition c = new Condition();
        c.setOperator(ConditionOperator.AND);
        c.setConditions(Arrays.asList(children));
        return c;
    }
}
