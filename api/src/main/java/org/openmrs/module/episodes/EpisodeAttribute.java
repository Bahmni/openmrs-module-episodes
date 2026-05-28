/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.episodes;

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

public class EpisodeAttribute extends BaseAttribute<EpisodeAttributeType, Episode> implements Attribute<EpisodeAttributeType, Episode> {
    private Integer episodeAttributeId;

    @Override
    public Integer getId() {
        return getEpisodeAttributeId();
    }

    @Override
    public void setId(Integer id) {
        setEpisodeAttributeId(id);
    }

    public Integer getEpisodeAttributeId() {
        return episodeAttributeId;
    }

    public void setEpisodeAttributeId(Integer episodeAttributeId) {
        this.episodeAttributeId = episodeAttributeId;
    }

    public Episode getEpisode() {
        return getOwner();
    }

    public void setEpisode(Episode episode) {
        setOwner(episode);
    }


}
