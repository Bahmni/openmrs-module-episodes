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

    // --- Common keys ---
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String VOIDED = "voided";
    public static final String CONCEPT = "concept";
    public static final String DISPLAY = "display";
    public static final String IDENTIFIER = "identifier";

    // --- Patient program level ---
    public static final String DATE_ENROLLED = "dateEnrolled";
    public static final String DATE_COMPLETED = "dateCompleted";
    public static final String PATIENT = "patient";
    public static final String PROGRAM = "program";
    public static final String LOCATION = "location";
    public static final String ATTRIBUTES = "attributes";
    public static final String CURRENT_STATE = "currentState";
    public static final String EPISODE = "episode";

    // --- Episode level ---
    public static final String STATUS = "status";
    public static final String DATE_STARTED = "dateStarted";
    public static final String DATE_ENDED = "dateEnded";
    public static final String CARE_MANAGER = "careManager";

    // --- Patient level ---
    public static final String GENDER = "gender";
    public static final String BIRTHDATE = "birthdate";
    public static final String IDENTIFIERS = "identifiers";

    // --- Person name level ---
    public static final String GIVEN_NAME = "givenName";
    public static final String MIDDLE_NAME = "middleName";
    public static final String FAMILY_NAME = "familyName";
    public static final String FAMILY_NAME2 = "familyName2";

    // --- Identifier level ---
    public static final String PREFERRED = "preferred";
    public static final String IDENTIFIER_TYPE = "identifierType";

    // --- Program level ---
    public static final String RETIRED = "retired";
    public static final String DESCRIPTION = "description";

    // --- Attribute level ---
    public static final String VALUE = "value";
    public static final String ATTRIBUTE_TYPE = "attributeType";

    // --- State level ---
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String STATE = "state";
    public static final String WORKFLOW = "workflow";
    public static final String INITIAL = "initial";
    public static final String TERMINAL = "terminal";

    // --- Care manager level ---
    public static final String PERSON = "person";
}
