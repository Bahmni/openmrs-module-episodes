package org.openmrs.module.episodes.dao;

import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.search.EpisodeSearchCriteria;

import java.util.List;

public interface EpisodeSearchDao {

    List<Episode> search(EpisodeSearchCriteria criteria);
}
