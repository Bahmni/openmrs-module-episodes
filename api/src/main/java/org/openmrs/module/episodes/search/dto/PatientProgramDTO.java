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
public class PatientProgramDTO {

    private String uuid;
    private LocationRefDTO location;
    private List<ProgramAttributeDTO> attributes;
    private ProgramDTO program;

    @Data
    public static class ProgramAttributeDTO {
        private AttributeTypeRefDTO attributeType;
        private String value;
        private String uuid;

        @Data
        public static class AttributeTypeRefDTO {
            private String uuid;
        }
    }
}
