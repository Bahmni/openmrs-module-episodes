package org.openmrs.module.episodes;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.User;

import java.util.Date;

public class EpisodeReason extends BaseOpenmrsObject {

    private Integer episodeReasonId;

    private User creator;
    private Date dateCreated;
    private Concept reasonUse;
    private Concept valueConcept;
    private String valueReference;

    private Episode episode;
    private Boolean voided = Boolean.FALSE;
    private Date dateVoided;
    private User voidedBy;
    private String voidReason;


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

    public Concept getReasonUse() {
        return reasonUse;
    }

    public void setReasonUse(Concept reasonUse) {
        this.reasonUse = reasonUse;
    }

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public Date getDateVoided() {
        return dateVoided;
    }

    public void setDateVoided(Date dateVoided) {
        this.dateVoided = dateVoided;
    }

    public User getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(User voidedBy) {
        this.voidedBy = voidedBy;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public String getValueReference() {
        return valueReference;
    }

    public void setValueReference(String valueReference) {
        this.valueReference = valueReference;
    }

    public Concept getValueConcept() {
        return valueConcept;
    }

    public void setValueConcept(Concept valueConcept) {
        this.valueConcept = valueConcept;
    }
}
