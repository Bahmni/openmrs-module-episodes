/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.dto;

import lombok.Data;

@Data
public class EpisodeSearchResultDTO {

    private String uuid;
    private String status;
    private String dateStarted;
    private String dateEnded;
    private PatientDTO patient;
    private ProgramDTO program;
    private CareManagerDTO careManager;

    @Data
    public static class CareManagerDTO {
        private String uuid;
        private String display;
        private String identifier;
        private PersonDTO person;

        @Data
        public static class PersonDTO {
            private String uuid;
            private PersonNameDTO preferredName;
        }
    }
}
