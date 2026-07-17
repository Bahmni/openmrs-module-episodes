package org.openmrs.module.episodes.service.impl;

import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeSearchDao;
import org.openmrs.module.episodes.search.EpisodeResultItem;
import org.openmrs.module.episodes.search.EpisodeResultItemMapper;
import org.openmrs.module.episodes.search.EpisodeSearchCriteria;
import org.openmrs.module.episodes.search.EpisodeSearchResponse;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
public class EpisodeSearchServiceImpl implements EpisodeSearchService {

    private final EpisodeSearchDao episodeSearchDao;
    private final EpisodeResultItemMapper episodeResultItemMapper;

    public EpisodeSearchServiceImpl(EpisodeSearchDao episodeSearchDao) {
        this(episodeSearchDao, new EpisodeResultItemMapper());
    }

    public EpisodeSearchServiceImpl(EpisodeSearchDao episodeSearchDao, EpisodeResultItemMapper episodeResultItemMapper) {
        this.episodeSearchDao = episodeSearchDao;
        this.episodeResultItemMapper = episodeResultItemMapper;
    }

    @Override
    public EpisodeSearchResponse search(EpisodeSearchCriteria criteria) {
        List<Episode> episodes = episodeSearchDao.search(criteria);

        EpisodeSearchResponse response = new EpisodeSearchResponse();
        response.setResults(mapToResultItems(episodes));
        return response;
    }

    private List<EpisodeResultItem> mapToResultItems(List<Episode> episodes) {
        List<EpisodeResultItem> resultItems = new ArrayList<>();
        for (Episode episode : episodes) {
            resultItems.add(episodeResultItemMapper.map(episode));
        }
        return resultItems;
    }
}

