/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes;

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

public class EpisodeAttributeType extends BaseAttributeType<Episode> implements AttributeType<Episode> {

    private Integer episodeAttributeTypeId;
    @Override
    public Integer getId() {
        return getEpisodeAttributeTypeId();
    }

    @Override
    public void setId(Integer id) {
        setEpisodeAttributeTypeId(id);
    }

    public Integer getEpisodeAttributeTypeId() {
        return episodeAttributeTypeId;
    }

    public void setEpisodeAttributeTypeId(Integer episodeAttributeTypeId) {
        this.episodeAttributeTypeId = episodeAttributeTypeId;
    }
}
