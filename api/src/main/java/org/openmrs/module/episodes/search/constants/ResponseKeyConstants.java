/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.search.constants;

public final class ResponseKeyConstants {

    public static final String COMMON_UUID = "uuid";
    public static final String COMMON_NAME = "name";
    public static final String COMMON_VOIDED = "voided";
    public static final String COMMON_CONCEPT = "concept";
    public static final String COMMON_DISPLAY = "display";

    public static final String ENROLLMENT_DATE_ENROLLED = "dateEnrolled";
    public static final String ENROLLMENT_DATE_COMPLETED = "dateCompleted";
    public static final String ENROLLMENT_PATIENT = "patient";
    public static final String ENROLLMENT_PROGRAM = "program";
    public static final String ENROLLMENT_LOCATION = "location";
    public static final String ENROLLMENT_ATTRIBUTES = "attributes";
    public static final String ENROLLMENT_CURRENT_STATE = "currentState";
    public static final String ENROLLMENT_EPISODE = "episode";

    public static final String EPISODE_STATUS = "status";
    public static final String EPISODE_DATE_STARTED = "dateStarted";
    public static final String EPISODE_DATE_ENDED = "dateEnded";
    public static final String EPISODE_CARE_MANAGER = "careManager";

    public static final String PATIENT_GENDER = "gender";
    public static final String PATIENT_BIRTHDATE = "birthdate";
    public static final String PATIENT_IDENTIFIERS = "identifiers";

    public static final String PERSON_GIVEN_NAME = "givenName";
    public static final String PERSON_MIDDLE_NAME = "middleName";
    public static final String PERSON_FAMILY_NAME = "familyName";
    public static final String PERSON_FAMILY_NAME2 = "familyName2";

    public static final String IDENTIFIER_VALUE = "identifier";
    public static final String IDENTIFIER_PREFERRED = "preferred";
    public static final String IDENTIFIER_TYPE = "identifierType";

    public static final String PROGRAM_RETIRED = "retired";
    public static final String PROGRAM_DESCRIPTION = "description";

    public static final String ATTRIBUTE_VALUE = "value";
    public static final String ATTRIBUTE_TYPE = "attributeType";

    public static final String STATE_START_DATE = "startDate";
    public static final String STATE_END_DATE = "endDate";
    public static final String STATE_STATE = "state";
    public static final String STATE_WORKFLOW = "workflow";
    public static final String STATE_INITIAL = "initial";
    public static final String STATE_TERMINAL = "terminal";

    public static final String PROVIDER_PERSON = "person";
}
