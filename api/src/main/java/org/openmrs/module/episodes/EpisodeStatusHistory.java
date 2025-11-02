package org.openmrs.module.episodes;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

import java.util.Date;

public class EpisodeStatusHistory extends BaseOpenmrsObject {
    private Integer episodeStatusHistoryId;

    private User creator;
    private Date dateCreated;
    private Episode episode;
    private Episode.Status status;
    private Date dateStarted;
    private Date dateEnded;

    @Override
    public Integer getId() {
        return getEpisodeStatusHistoryId();
    }

    @Override
    public void setId(Integer id) {
        setEpisodeStatusHistoryId(id);
    }

    public Integer getEpisodeStatusHistoryId() {
        return episodeStatusHistoryId;
    }

    public void setEpisodeStatusHistoryId(Integer episodeStatusHistoryId) {
        this.episodeStatusHistoryId = episodeStatusHistoryId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public Episode.Status getStatus() {
        return status;
    }

    public void setStatus(Episode.Status status) {
        this.status = status;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Date getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(Date dateEnded) {
        this.dateEnded = dateEnded;
    }
}
