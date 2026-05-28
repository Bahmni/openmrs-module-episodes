/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes.dao;

import org.openmrs.module.episodes.EpisodeAttributeType;

import java.util.List;

public interface EpisodeAttributeTypeDao {
    List<EpisodeAttributeType> getAllAttributeTypes(boolean includeRetired);

    EpisodeAttributeType getAttributeTypeByUuid(String uuid);

    EpisodeAttributeType getAttributeTypeById(Integer id);

    EpisodeAttributeType save(EpisodeAttributeType attributeType);

    List<EpisodeAttributeType> findByName(String name);
}
