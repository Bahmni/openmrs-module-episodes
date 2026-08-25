/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao;

import lombok.Getter;
import org.openmrs.PatientProgram;
import org.openmrs.module.episodes.Episode;

@Getter
public class EpisodePatientProgram {

    private final Episode episode;
    private final PatientProgram patientProgram;

    public EpisodePatientProgram(Episode episode, PatientProgram patientProgram) {
        this.episode = episode;
        this.patientProgram = patientProgram;
    }

}
