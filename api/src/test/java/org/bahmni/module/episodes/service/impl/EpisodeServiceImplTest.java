package org.bahmni.module.episodes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.module.episodes.dao.impl.EpisodeDAO;
import org.openmrs.module.episodes.service.impl.EpisodeServiceImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
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