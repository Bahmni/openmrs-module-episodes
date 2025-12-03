package org.openmrs.module.episodes.service.impl;

import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.EpisodeAttributeType;
import org.openmrs.module.episodes.dao.EpisodeAttributeTypeDao;
import org.openmrs.module.episodes.service.EpisodeAttributeTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Transactional
public class EpisodeAttributeTypeServiceImpl implements EpisodeAttributeTypeService {

    private EpisodeAttributeTypeDao episodeAttributeTypeDao;

    public void setEpisodeAttributeTypeDao(EpisodeAttributeTypeDao episodeAttributeTypeDao) {
        this.episodeAttributeTypeDao = episodeAttributeTypeDao;
    }

    public EpisodeAttributeTypeServiceImpl(EpisodeAttributeTypeDao episodeAttributeTypeDao) {
        this.episodeAttributeTypeDao = episodeAttributeTypeDao;
    }

    @Override
    public List<EpisodeAttributeType> getAllAttributeTypes(boolean includeRetired) {
        return episodeAttributeTypeDao.getAllAttributeTypes(includeRetired);
    }

    @Override
    public EpisodeAttributeType getAttributeTypeByUuid(@NotBlank String uuid) {
        return episodeAttributeTypeDao.getAttributeTypeByUuid(uuid);
    }

    @Override
    public EpisodeAttributeType save(EpisodeAttributeType attributeType) {
        return episodeAttributeTypeDao.save(attributeType);
    }

    @Override
    public EpisodeAttributeType retire(EpisodeAttributeType attributeType, String retireReason) {
        attributeType.setRetired(true);
        attributeType.setRetireReason(retireReason);
        attributeType.setRetiredBy(Context.getAuthenticatedUser());
        attributeType.setDateRetired(new Date());
        return episodeAttributeTypeDao.save(attributeType);
    }

    @Override
    public List<EpisodeAttributeType> getAttributesByName(String name) {
        return episodeAttributeTypeDao.findByName(name);
    }
}
