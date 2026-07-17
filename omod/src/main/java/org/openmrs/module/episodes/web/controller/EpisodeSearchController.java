package org.openmrs.module.episodes.web.controller;

import org.openmrs.module.episodes.search.EpisodeSearchRequest;
import org.openmrs.module.episodes.search.EpisodeSearchResponse;
import org.openmrs.module.episodes.service.EpisodeSearchService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/episode/search")
public class EpisodeSearchController extends BaseRestController {

    @Autowired
    private EpisodeSearchService episodeSearchService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public EpisodeSearchResponse search(@RequestBody EpisodeSearchRequest request) {
        return episodeSearchService.search(request.getCriteria());
    }
}
