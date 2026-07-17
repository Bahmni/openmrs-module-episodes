package org.openmrs.module.episodes.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.dao.EpisodeSearchDao;
import org.openmrs.module.episodes.search.EpisodeResultItem;
import org.openmrs.module.episodes.search.EpisodeResultItemMapper;
import org.openmrs.module.episodes.search.EpisodeSearchCriteria;
import org.openmrs.module.episodes.search.EpisodeSearchResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeSearchServiceImplTest {

    @Mock
    private EpisodeSearchDao episodeSearchDao;

    @Mock
    private EpisodeResultItemMapper episodeResultItemMapper;

    @Test
    public void shouldDelegateSearchToDaoWithTheGivenCriteria() {
        EpisodeSearchServiceImpl episodeSearchService = new EpisodeSearchServiceImpl(episodeSearchDao, episodeResultItemMapper);
        EpisodeSearchCriteria criteria = new EpisodeSearchCriteria();
        when(episodeSearchDao.search(criteria)).thenReturn(Collections.emptyList());

        episodeSearchService.search(criteria);

        verify(episodeSearchDao, times(1)).search(criteria);
    }

    @Test
    public void shouldMapEachEpisodeReturnedByTheDaoToAResultItem() {
        EpisodeSearchServiceImpl episodeSearchService = new EpisodeSearchServiceImpl(episodeSearchDao, episodeResultItemMapper);
        Episode firstEpisode = new Episode();
        Episode secondEpisode = new Episode();
        EpisodeResultItem firstResultItem = new EpisodeResultItem();
        EpisodeResultItem secondResultItem = new EpisodeResultItem();
        when(episodeSearchDao.search(null)).thenReturn(Arrays.asList(firstEpisode, secondEpisode));
        when(episodeResultItemMapper.map(firstEpisode)).thenReturn(firstResultItem);
        when(episodeResultItemMapper.map(secondEpisode)).thenReturn(secondResultItem);

        EpisodeSearchResponse response = episodeSearchService.search(null);

        List<EpisodeResultItem> results = response.getResults();
        assertEquals(2, results.size());
        assertSame(firstResultItem, results.get(0));
        assertSame(secondResultItem, results.get(1));
    }

    @Test
    public void shouldReturnAnEmptyResultListWhenTheDaoFindsNoEpisodes() {
        EpisodeSearchServiceImpl episodeSearchService = new EpisodeSearchServiceImpl(episodeSearchDao, episodeResultItemMapper);
        when(episodeSearchDao.search(null)).thenReturn(Collections.emptyList());

        EpisodeSearchResponse response = episodeSearchService.search(null);

        assertEquals(0, response.getResults().size());
    }

    @Test
    public void shouldUseADefaultMapperWhenConstructedWithoutOne() {
        EpisodeSearchDao dao = mock(EpisodeSearchDao.class);
        Episode episode = new Episode();
        when(dao.search(null)).thenReturn(Collections.singletonList(episode));
        EpisodeSearchServiceImpl episodeSearchService = new EpisodeSearchServiceImpl(dao);

        EpisodeSearchResponse response = episodeSearchService.search(null);

        assertEquals(1, response.getResults().size());
    }
}
