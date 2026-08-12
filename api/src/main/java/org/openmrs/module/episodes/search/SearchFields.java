/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search;

public final class SearchFields {

    public static final String EOC_START_DATE = "episodeOfCare.startDate";
    public static final String EOC_END_DATE = "episodeOfCare.endDate";
    public static final String EOC_CARE_MANAGER = "episodeOfCare.careManager";

    public static final String PROGRAM_ENROLLMENT_DATE = "program.dateEnrolled";
    public static final String PROGRAM_COMPLETION_DATE = "program.dateCompleted";

    public static final String PROGRAM_UUID = "program.uuid";
    public static final String PROGRAM_TYPE = "program.type";
    public static final String PROGRAM_LOCATION = "location.uuid";
    public static final String PROGRAM_STATUS = "program.status";
    public static final String PROGRAM_STATUS_DATE = "program.statusDate";
    public static final String PROGRAM_ATTRIBUTE_KIND = "program.attributeType.kind";
    public static final String PROGRAM_ATTRIBUTE_VALUE = "program.attributeType.value";

    public static final String PATIENT_IDENTIFIER_KIND = "patient.identifiers.kind";
    public static final String PATIENT_IDENTIFIER_VALUE = "patient.identifiers.value";
}
