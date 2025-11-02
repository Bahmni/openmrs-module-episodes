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
