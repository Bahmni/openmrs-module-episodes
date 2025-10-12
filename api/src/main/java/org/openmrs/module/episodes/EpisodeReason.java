package org.openmrs.module.episodes;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.User;

import java.util.Date;

public class EpisodeReason extends BaseOpenmrsObject {

    private Integer episodeReasonId;

    private User creator;
    private Date dateCreated;
    private Concept reason;
    private Episode episode;

    public Integer getEpisodeReasonId() {
        return episodeReasonId;
    }

    public void setEpisodeReasonId(Integer episodeReasonId) {
        this.episodeReasonId = episodeReasonId;
    }

    public EpisodeReason() {
    }

    @Override
    public Integer getId() {
        return getEpisodeReasonId();
    }

    @Override
    public void setId(Integer id) {
        setEpisodeReasonId(id);
    }

    public User getCreator() {
        return creator;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Concept getReason() {
        return reason;
    }

    public void setReason(Concept reason) {
        this.reason = reason;
    }

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }
}
