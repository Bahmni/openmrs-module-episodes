package org.openmrs.module.episodes.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.episodes.search.EpisodeSearchCriteria;
import org.openmrs.module.episodes.search.EpisodeSearchResponse;

public interface EpisodeSearchService {

    @Authorized({"Get Episodes"})
    EpisodeSearchResponse search(EpisodeSearchCriteria criteria);
}
