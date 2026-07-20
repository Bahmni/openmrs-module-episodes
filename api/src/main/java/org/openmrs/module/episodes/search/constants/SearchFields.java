/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.constants;

public final class SearchFields {

    public static final class EpisodeOfCare {
        public static final String START_DATE = "episodeOfCare.startDate";
        public static final String END_DATE = "episodeOfCare.endDate";
        public static final String CARE_MANAGER = "episodeOfCare.careManager";

        private EpisodeOfCare() {
        }
    }

    public static final class Program {
        public static final String UUID = "program.uuid";
        public static final String TYPE = "program.type";
        public static final String LOCATION = "program.location";
        public static final String STATUS = "program.status";
        public static final String STATUS_DATE = "program.statusDate";
        public static final String ATTRIBUTE_KIND = "program.attributeType.kind";
        public static final String ATTRIBUTE_VALUE = "program.attributeType.value";

        private Program() {
        }
    }

    public static final class Patient {
        public static final String IDENTIFIER_KIND = "patient.identifiers.kind";
        public static final String IDENTIFIER_VALUE = "patient.identifiers.value";

        private Patient() {
        }
    }

    private SearchFields() {
    }
}
