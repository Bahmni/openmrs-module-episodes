/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

import org.openmrs.module.episodes.search.constants.SearchFields;
import org.openmrs.module.episodes.search.criteria.Condition;
import org.openmrs.module.episodes.search.criteria.ConditionOperator;
import org.openmrs.module.episodes.search.exceptions.InvalidSearchCriteriaException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class EpisodeSearchQueryBuilderTest {

    private static final String EQ = "eq";
    private static final String GT = "gt";
    private static final String LT = "lt";
    private static final String DATE_FROM = "2024-01-01";
    private static final String DATE_TO = "2024-12-31";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final EpisodeSearchQueryBuilder builder = new EpisodeSearchQueryBuilder();

    @Test
    public void shouldStartWithSelectDistinctFromEpisode() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));

        assertThat(result.getHql(), containsString("SELECT DISTINCT e FROM Episode e"));
    }

    @Test
    public void shouldAlwaysExcludeVoidedEpisodes() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));

        assertThat(result.getHql(), containsString("WHERE e.voided = false"));
    }

    @Test
    public void shouldProduceStartDateGtFragmentWithDateParameter() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));

        assertThat(result.getHql(), containsString("e.dateStarted > :param0"));
        assertThat(result.getHql(), not(containsString("INNER JOIN")));
        assertThat(result.getParameters().get("param0"), instanceOf(Date.class));
    }

    @Test
    public void shouldProduceStartDateLtFragmentOnly() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, LT, DATE_TO));

        assertThat(result.getHql(), containsString("e.dateStarted < :param0"));
        assertThat(result.getHql(), not(containsString(">")));
        assertThat(result.getParameters().size(), is(1));
    }

    @Test
    public void shouldProduceEndDateGtFragment() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.END_DATE, GT, DATE_FROM));

        assertThat(result.getHql(), containsString("e.dateEnded > :param0"));
        assertThat(result.getParameters().get("param0"), instanceOf(Date.class));
    }

    @Test
    public void shouldProduceEndDateLtFragment() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.END_DATE, LT, DATE_TO));

        assertThat(result.getHql(), containsString("e.dateEnded < :param0"));
        assertThat(result.getParameters().get("param0"), instanceOf(Date.class));
    }

    @Test
    public void shouldBindCareManagerAsStringWithNoJoins() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.CARE_MANAGER, EQ, "provider-uuid"));

        assertThat(result.getHql(), containsString("e.careManager.uuid = :param0"));
        assertThat(result.getHql(), not(containsString("INNER JOIN")));
        assertThat(result.getParameters().get("param0"), instanceOf(String.class));
    }

    @Test
    public void shouldAddPpJoinWhenSearchingByProgramUuid() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.UUID, EQ, "program-uuid"));

        assertThat(result.getHql(), containsString("INNER JOIN e.patientPrograms pp"));
        assertThat(result.getHql(), containsString("pp.program.uuid = :param0"));
        assertThat(result.getHql(), containsString("pp.voided = false"));
        assertThat(result.getParameters().get("param0"), instanceOf(String.class));
    }

    @Test
    public void shouldAddPpJoinWhenSearchingByProgramType() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.TYPE, EQ, "concept-uuid"));

        assertThat(result.getHql(), containsString("INNER JOIN e.patientPrograms pp"));
        assertThat(result.getHql(), containsString("pp.program.concept.uuid = :param0"));
    }

    @Test
    public void shouldAddPpJoinWhenSearchingByProgramLocation() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.LOCATION, EQ, "location-uuid"));

        assertThat(result.getHql(), containsString("INNER JOIN e.patientPrograms pp"));
        assertThat(result.getHql(), containsString("pp.location.uuid = :param0"));
    }

    @Test
    public void shouldAddPpAndPsJoinsWhenSearchingByProgramStatus() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.STATUS, EQ, "concept-uuid"));

        assertThat(result.getHql(), containsString("INNER JOIN e.patientPrograms pp"));
        assertThat(result.getHql(), containsString("INNER JOIN pp.states ps"));
        assertThat(result.getHql(), containsString("ps.state.concept.uuid = :param0"));
        assertThat(result.getHql(), containsString("pp.voided = false"));
        assertThat(result.getHql(), containsString("ps.voided = false"));
    }

    @Test
    public void shouldAddPsJoinWithDateParameterForStatusDateGt() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.STATUS_DATE, GT, DATE_FROM));

        assertThat(result.getHql(), containsString("INNER JOIN pp.states ps"));
        assertThat(result.getHql(), containsString("ps.startDate > :param0"));
        assertThat(result.getParameters().get("param0"), instanceOf(Date.class));
    }

    @Test
    public void shouldProduceStatusDateLtFragment() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.STATUS_DATE, LT, DATE_TO));

        assertThat(result.getHql(), containsString("ps.startDate < :param0"));
        assertThat(result.getParameters().get("param0"), instanceOf(Date.class));
    }

    @Test
    public void shouldAddPiJoinWithOrClauseWhenSearchingByIdentifier() {
        BuiltQuery result = builder.build(group(
                leaf(SearchFields.Patient.IDENTIFIER_KIND, EQ, "NATIONAL_ID"),
                leaf(SearchFields.Patient.IDENTIFIER_VALUE, EQ, "N456")
        ));

        assertThat(result.getHql(), containsString("INNER JOIN e.patient ep INNER JOIN ep.identifiers pi"));
        assertThat(result.getHql(), containsString("(pi.identifierType.uuid = :param0 OR pi.identifierType.name = :param0)"));
        assertThat(result.getHql(), containsString("pi.identifier = :param1"));
        assertThat(result.getHql(), containsString("pi.voided = false"));
    }

    @Test
    public void shouldAddPpaJoinAndTrackAttributeTypeUuidWhenSearchingByAttribute() {
        String attrTypeUuid = "attr-type-uuid";
        BuiltQuery result = builder.build(group(
                leaf(SearchFields.Program.ATTRIBUTE_KIND, EQ, attrTypeUuid),
                leaf(SearchFields.Program.ATTRIBUTE_VALUE, EQ, "US")
        ));

        assertThat(result.getHql(), containsString("INNER JOIN pp.attributes ppa"));
        assertThat(result.getHql(), containsString("ppa.attributeType.uuid = :param0"));
        assertThat(result.getHql(), containsString("ppa.valueReference = :param1"));
        assertThat(result.getHql(), containsString("ppa.voided = false"));
        assertThat(result.getSearchedAttributeTypeUuid(), is(attrTypeUuid));
    }

    @Test
    public void shouldNotSetAttributeTypeUuidWhenOnlyValueFieldIsPresent() {
        BuiltQuery result = builder.build(leaf(SearchFields.Program.ATTRIBUTE_VALUE, EQ, "US"));

        assertThat(result.getSearchedAttributeTypeUuid(), is(nullValue()));
    }

    @Test
    public void shouldNotDuplicatePpJoinForMultipleProgramFields() {
        BuiltQuery result = builder.build(group(
                leaf(SearchFields.Program.UUID, EQ, "prog-uuid"),
                leaf(SearchFields.Program.TYPE, EQ, "concept-uuid"),
                leaf(SearchFields.Program.LOCATION, EQ, "loc-uuid")
        ));

        int ppJoinCount = result.getHql().split("INNER JOIN e\\.patientPrograms pp", -1).length - 1;
        assertThat(ppJoinCount, is(1));
    }

    @Test
    public void shouldAddBothPpAndPiJoinsWhenCombiningProgramAndIdentifierSearch() {
        BuiltQuery result = builder.build(group(
                leaf(SearchFields.Program.UUID, EQ, "prog-uuid"),
                leaf(SearchFields.Patient.IDENTIFIER_KIND, EQ, "NATIONAL_ID"),
                leaf(SearchFields.Patient.IDENTIFIER_VALUE, EQ, "N456")
        ));

        assertThat(result.getHql(), containsString("INNER JOIN e.patientPrograms pp"));
        assertThat(result.getHql(), containsString("INNER JOIN e.patient ep INNER JOIN ep.identifiers pi"));
    }

    @Test
    public void shouldNameParametersSequentiallyAcrossMultipleCriteria() {
        BuiltQuery result = builder.build(group(
                leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM),
                leaf(SearchFields.EpisodeOfCare.END_DATE, LT, DATE_TO),
                leaf(SearchFields.Program.UUID, EQ, "prog-uuid")
        ));

        assertThat(result.getParameters().containsKey("param0"), is(true));
        assertThat(result.getParameters().containsKey("param1"), is(true));
        assertThat(result.getParameters().containsKey("param2"), is(true));
        assertThat(result.getParameters().size(), is(3));
    }

    @Test
    public void shouldReturnNullAttributeTypeUuidWhenNoAttributeSearched() {
        BuiltQuery result = builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, DATE_FROM));

        assertThat(result.getSearchedAttributeTypeUuid(), is(nullValue()));
    }

    @Test
    public void shouldThrowForUnknownSearchField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown search field: 'patient.unknownField'");

        builder.build(leaf("patient.unknownField", EQ, "value"));
    }

    @Test
    public void shouldThrowWhenGtUsedOnTextField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("not supported for field 'program.uuid'");

        builder.build(leaf(SearchFields.Program.UUID, GT, "uuid"));
    }

    @Test
    public void shouldThrowWhenEqUsedOnDateField() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("not supported for field 'episodeOfCare.startDate'");

        builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, EQ, DATE_FROM));
    }

    @Test
    public void shouldThrowWhenComparatorIsNull() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Unknown comparator");

        builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, null, DATE_FROM));
    }

    @Test
    public void shouldThrowForInvalidDateFormat() {
        thrown.expect(InvalidSearchCriteriaException.class);
        thrown.expectMessage("Invalid date format");

        builder.build(leaf(SearchFields.EpisodeOfCare.START_DATE, GT, "01/01/2024"));
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
