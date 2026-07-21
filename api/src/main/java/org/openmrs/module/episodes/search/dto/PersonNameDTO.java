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
public class PersonNameDTO {

    private String display;
    private String uuid;
    private String givenName;
    private String middleName;
    private String familyName;
    private String familyName2;
    private boolean voided;
}
