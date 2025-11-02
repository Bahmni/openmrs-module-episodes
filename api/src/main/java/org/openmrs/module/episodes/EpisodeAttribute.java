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
