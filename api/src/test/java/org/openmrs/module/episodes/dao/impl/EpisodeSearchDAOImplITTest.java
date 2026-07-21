/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao.impl;

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
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.ConditionOperator;
import org.openmrs.module.episodes.search.dto.EpisodeSearchResultDTO;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;
import org.openmrs.module.episodes.search.criteria.SearchRequest;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class EpisodeSearchDAOImplITTest extends BaseModuleContextSensitiveTest {

    private static final String EQ = "eq";
    private static final String GT = "gt";
    private static final String LT = "lt";
    private static final String DATE_FROM = "2024-01-01";
    private static final String DATE_TO = "2024-12-31";

    @Autowired
    private EpisodeSearchService episodeSearchService;

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
    public void shouldReturnEpisodesLinkedToProgram() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = saveEpisodeWith(pp);

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.Program.UUID, EQ, pp.getProgram().getUuid()))
        );

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
    }

    @Test
    public void shouldReturnOnlyEpisodesWithinStartDateRange() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.addPatientProgram(pp);
        episode.setDateStarted(date(2024, 6, 15));
        episodeService.save(episode);

        Episode outsideRange = new Episode();
        outsideRange.addPatientProgram(pp);
        outsideRange.setDateStarted(date(2023, 1, 1));
        episodeService.save(outsideRange);

        Condition range = group(
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM),
                leaf(SearchFields.EpisodeOfCare.START_DATE, LT, DATE_TO)
        );

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(requestWith(range));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
    }

    @Test
    public void shouldReturnEpisodesAfterStartDate() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.addPatientProgram(pp);
        episode.setDateStarted(date(2024, 6, 15));
        episodeService.save(episode);

        Episode before = new Episode();
        before.addPatientProgram(pp);
        before.setDateStarted(date(2023, 1, 1));
        episodeService.save(before);

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM)));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
    }

    @Test
    public void shouldReturnEpisodesBeforeStartDate() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode before = new Episode();
        before.addPatientProgram(pp);
        before.setDateStarted(date(2023, 1, 1));
        episodeService.save(before);

        Episode after = new Episode();
        after.addPatientProgram(pp);
        after.setDateStarted(date(2025, 6, 15));
        episodeService.save(after);

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.EpisodeOfCare.START_DATE, LT, DATE_FROM)));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(before.getUuid()));
    }

    @Test
    public void shouldReturnEpisodesForCareManager() {
        Provider provider = createProvider("Dr Smith");
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.addPatientProgram(pp);
        episode.setCareManager(provider);
        episodeService.save(episode);

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.EpisodeOfCare.CARE_MANAGER, EQ, provider.getUuid()))
        );

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
        assertThat(results.get(0).getCareManager().getUuid(), is(provider.getUuid()));
    }

    @Test
    public void shouldReturnEpisodesByPatientIdentifierName() {
        PatientIdentifierType identifierType = createIdentifierType("PASSPORT");
        Patient patient = createPatientWithIdentifier(identifierType, "P12345");

        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.setPatient(patient);
        episode.addPatientProgram(pp);
        episodeService.save(episode);

        Condition identifierSearch = group(
                leaf(SearchFields.Patient.IDENTIFIER_KIND, EQ, "PASSPORT"),
                leaf(SearchFields.Patient.IDENTIFIER_VALUE, EQ, "P12345")
        );

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(requestWith(identifierSearch));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
    }

    @Test
    public void shouldReturnEpisodesByPatientIdentifierUuid() {
        PatientIdentifierType identifierType = createIdentifierType("TEST_ID_TYPE");
        Patient patient = createPatientWithIdentifier(identifierType, "T99");

        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = new Episode();
        episode.setPatient(patient);
        episode.addPatientProgram(pp);
        episodeService.save(episode);

        Condition identifierSearch = group(
                leaf(SearchFields.Patient.IDENTIFIER_KIND, EQ, identifierType.getUuid()),
                leaf(SearchFields.Patient.IDENTIFIER_VALUE, EQ, "T99")
        );

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(requestWith(identifierSearch));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
    }

    @Test
    public void shouldReturnEpisodesInProgramLocation() {
        Location location = locationService.getLocation(1);
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        pp.setLocation(location);
        programWorkflowService.savePatientProgram(pp);

        Episode episode = saveEpisodeWith(pp);

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.Program.LOCATION, EQ, location.getUuid()))
        );

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getUuid(), is(episode.getUuid()));
    }

    @Test
    public void shouldPopulatePatientAndProgramDataInResponse() {
        PatientProgram pp = programWorkflowService.getPatientProgram(1);
        Episode episode = saveEpisodeWith(pp);

        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.Program.UUID, EQ, pp.getProgram().getUuid()))
        );

        EpisodeSearchResultDTO dto = results.get(0);
        assertThat(dto.getPatient(), is(notNullValue()));
        assertThat(dto.getPatient().getUuid(), is(notNullValue()));
        assertThat(dto.getPatientPrograms(), is(notNullValue()));
        assertThat(dto.getPatientPrograms().isEmpty(), is(false));
        assertThat(dto.getPatientPrograms().get(0).getProgram(), is(notNullValue()));
        assertThat(dto.getStatus(), is(notNullValue()));
    }

    @Test
    public void shouldReturnEmptyListWhenNoCriteriaMatch() {
        List<EpisodeSearchResultDTO> results = episodeSearchService.search(
                requestWith(leaf(SearchFields.Program.UUID, EQ, "non-existent-uuid"))
        );

        assertThat(results.size(), is(0));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionWhenOrOperatorIsUsed() {
        Condition criteria = new Condition();
        criteria.setOperator(ConditionOperator.OR);
        criteria.setConditions(Arrays.asList(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM)));

        episodeSearchService.search(requestWith(criteria));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionForUnknownSearchField() {
        episodeSearchService.search(requestWith(leaf("episode.unknownField", EQ, "value")));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionForUnsupportedComparator() {
        episodeSearchService.search(requestWith(leaf(SearchFields.Program.UUID, GT, "uuid")));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowExceptionForInvalidDateFormat() {
        episodeSearchService.search(requestWith(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, "01/01/2024")));
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

    private PatientIdentifierType createIdentifierType(String name) {
        PatientIdentifierType type = new PatientIdentifierType();
        type.setName(name);
        type.setDescription("Test identifier type: " + name);
        return patientService.savePatientIdentifierType(type);
    }

    private Patient createPatientWithIdentifier(PatientIdentifierType identifierType, String identifier) {
        Patient patient = new Patient();
        PersonName name = new PersonName();
        name.setGivenName("Test");
        name.setFamilyName("Patient");
        patient.addName(name);
        patient.setGender("M");

        PatientIdentifier pi = new PatientIdentifier();
        pi.setIdentifierType(identifierType);
        pi.setIdentifier(identifier);
        pi.setPreferred(true);
        pi.setLocation(locationService.getLocation(1));
        patient.addIdentifier(pi);

        return patientService.savePatient(patient);
    }

    private Date date(int year, int month, int day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
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

    private Condition group(Condition... children) {
        Condition c = new Condition();
        c.setOperator(ConditionOperator.AND);
        c.setConditions(Arrays.asList(children));
        return c;
    }
}
