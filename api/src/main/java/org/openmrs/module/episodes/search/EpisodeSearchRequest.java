package org.openmrs.module.episodes.search;


public class EpisodeSearchRequest {

    private EpisodeSearchCriteria criteria;

    public EpisodeSearchCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EpisodeSearchCriteria criteria) {
        this.criteria = criteria;
    }
}
