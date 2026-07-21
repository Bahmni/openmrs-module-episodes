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

import java.util.List;

@Data
public class PatientDTO {

    private String uuid;
    private String display;
    private List<IdentifierDTO> identifiers;
    private PersonDTO person;
    private boolean voided;

    @Data
    public static class IdentifierDTO {
        private String display;
        private String uuid;
        private String identifier;
        private IdentifierTypeDTO identifierType;
        private LocationRefDTO location;
        private boolean preferred;
    }

    @Data
    public static class IdentifierTypeDTO {
        private String uuid;
        private String display;
    }

    @Data
    public static class PersonDTO {
        private String uuid;
        private String display;
        private String gender;
        private Integer age;
        private String birthdate;
        private boolean birthdateEstimated;
        private PersonNameDTO preferredName;
    }
}
