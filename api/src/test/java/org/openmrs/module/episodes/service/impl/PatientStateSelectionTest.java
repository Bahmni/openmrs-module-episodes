/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.service.impl;

import org.junit.Test;
import org.openmrs.PatientState;
import org.openmrs.module.episodes.search.impl.PatientProgramResponseMapper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PatientStateSelectionTest {

    @Test
    public void shouldReturnNullWhenNoStates() {
        assertThat(PatientProgramResponseMapper.selectCurrentState(Collections.emptySet()), is(nullValue()));
    }

    @Test
    public void shouldReturnNullWhenAllStatesAreVoided() {
        PatientState voided = state(date(2024, 1, 1), null);
        voided.setVoided(true);

        assertThat(PatientProgramResponseMapper.selectCurrentState(setOf(voided)), is(nullValue()));
    }

    @Test
    public void shouldReturnSingleActiveState() {
        PatientState active = state(date(2024, 1, 1), null);

        assertThat(PatientProgramResponseMapper.selectCurrentState(setOf(active)), is(active));
    }

    @Test
    public void shouldReturnActiveStateOverEndedState() {
        PatientState ended = state(date(2023, 1, 1), date(2023, 6, 1));
        PatientState active = state(date(2024, 1, 1), null);

        assertThat(PatientProgramResponseMapper.selectCurrentState(setOf(ended, active)), is(active));
    }

    @Test
    public void shouldReturnLatestStartDateWhenMultipleActiveStates() {
        PatientState earlier = state(date(2023, 1, 1), null);
        PatientState later = state(date(2024, 6, 1), null);

        assertThat(PatientProgramResponseMapper.selectCurrentState(setOf(earlier, later)), is(later));
    }

    @Test
    public void shouldReturnLatestEndDateWhenAllStatesAreEnded() {
        PatientState older = state(date(2022, 1, 1), date(2022, 12, 31));
        PatientState newer = state(date(2023, 1, 1), date(2023, 12, 31));

        assertThat(PatientProgramResponseMapper.selectCurrentState(setOf(older, newer)), is(newer));
    }

    @Test
    public void shouldIgnoreVoidedStatesWhenSelectingCurrent() {
        PatientState active = state(date(2023, 1, 1), null);
        PatientState voidedActive = state(date(2024, 1, 1), null);
        voidedActive.setVoided(true);

        assertThat(PatientProgramResponseMapper.selectCurrentState(setOf(active, voidedActive)), is(active));
    }

    private PatientState state(Date startDate, Date endDate) {
        PatientState s = new PatientState();
        s.setStartDate(startDate);
        s.setEndDate(endDate);
        s.setVoided(false);
        return s;
    }

    private Date date(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Set<PatientState> setOf(PatientState... states) {
        return new LinkedHashSet<>(Arrays.asList(states));
    }
}
