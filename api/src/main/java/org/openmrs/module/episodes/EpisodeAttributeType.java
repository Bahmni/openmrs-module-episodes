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
