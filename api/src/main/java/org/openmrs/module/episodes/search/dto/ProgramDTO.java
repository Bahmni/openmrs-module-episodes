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
public class ProgramDTO {

    private String name;
    private String uuid;
    private boolean retired;
    private String description;
    private ConceptRefDTO concept;
    private LocationRefDTO location;
    private List<ProgramAttributeDTO> attributes;

    @Data
    public static class ConceptRefDTO {
        private String uuid;
        private String display;
    }

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
