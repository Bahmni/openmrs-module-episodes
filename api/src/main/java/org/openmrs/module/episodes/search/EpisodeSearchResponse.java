package org.openmrs.module.episodes.search;

import java.util.List;

public class EpisodeSearchResponse {

    private List<EpisodeResultItem> results;

    public List<EpisodeResultItem> getResults() {
        return results;
    }

    public void setResults(List<EpisodeResultItem> results) {
        this.results = results;
    }
}
