package org.bahmni.module.episodes.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.module.episodes.dao.impl.EpisodeDAO;
import org.openmrs.module.episodes.service.impl.EpisodeServiceImpl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeServiceImplTest {

    @InjectMocks
    private EpisodeServiceImpl episodeService;

    @Mock
    EpisodeDAO episodeDAO;

    @Test
    public void shouldGetEncounterForAnEpisode() {
        Encounter encounter = new Encounter();

        episodeService.getEpisodeForEncounter(encounter);

        verify(episodeDAO, times(1)).getEpisodeForEncounter(encounter);
    }
}