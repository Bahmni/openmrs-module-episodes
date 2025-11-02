package org.openmrs.module.episodes.service;

import org.openmrs.ConceptAttributeType;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface EpisodeAttributeTypeService {
    @Transactional(readOnly = true)
    @Authorized({"Get Episode Attribute Types"})
    List<EpisodeAttributeType> getAllAttributeTypes(boolean includeRetired);

    @Transactional(readOnly = true)
    @Authorized({"Get Episode Attribute Types"})
    EpisodeAttributeType getAttributeTypeByUuid(@NotBlank String uuid);

    @Transactional
    @Authorized({"Manage Episode Attribute Types"})
    EpisodeAttributeType save(EpisodeAttributeType attributeType);

    @Transactional
    @Authorized({"Manage Episode Attribute Types"})
    EpisodeAttributeType retire(EpisodeAttributeType attributeType, String retireReason);

    @Transactional
    @Authorized({"Manage Episode Attribute Types"})
    List<EpisodeAttributeType> getAttributesByName(String name);
}
