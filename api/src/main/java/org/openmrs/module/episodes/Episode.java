package org.openmrs.module.episodes;

import org.openmrs.BaseCustomizableData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.User;
import org.openmrs.Visit;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Episode extends BaseCustomizableData<EpisodeAttribute> {

    private Integer episodeId;
    private Set<Encounter> encounters = new HashSet<>();
    private Set<PatientProgram> patientPrograms = new HashSet<>();
    private Patient patient;
    private Set<EpisodeReason> episodeReason = new HashSet<>();
    private Episode.Status status = Episode.Status.ACTIVE;
    private Date dateStarted;
    private Date dateEnded;
    private Concept concept;
    private Set<EpisodeStatusHistory> statusHistory = new HashSet<>();
    private User careManager;
    private Set<Visit> visits = new HashSet<>();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(Date dateEnded) {
        this.dateEnded = dateEnded;
    }


    public Set<EpisodeReason> getEpisodeReason() {
        return episodeReason;
    }

    public void setEpisodeReason(Set<EpisodeReason> episodeReason) {
        this.episodeReason = episodeReason;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public User getCareManager() {
        return careManager;
    }

    public void setCareManager(User careManager) {
        this.careManager = careManager;
    }

    public Set<EpisodeStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(Set<EpisodeStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public Set<Visit> getVisits() {
        return visits;
    }

    public void setVisits(Set<Visit> visits) {
        this.visits = visits;
    }

    public enum Status {
        UNKNOWN,
        WAITLIST,
        PLANNED,
        ACTIVE,
        ONHOLD,
        FINISHED,
        CANCELLED,
        ENTERED_IN_ERROR
    }

    public Episode(Integer episodeId, Set<Encounter> encounters, Set<PatientProgram> patientPrograms) {
        this.episodeId = episodeId;
        this.encounters = encounters;
        this.patientPrograms = patientPrograms;
    }

    public Episode(Integer episodeId, Patient patient, Set<Encounter> encounters, Set<PatientProgram> patientPrograms) {
        this.episodeId = episodeId;
        this.patient = patient;
        this.encounters = encounters;
        this.patientPrograms = patientPrograms;
    }

    public Episode() {
    }

    public Set<Encounter> getEncounters() {
        return encounters;
    }

    public Integer getEpisodeId() {
        return episodeId;
    }

    @Override
    public Integer getId() {
        return episodeId;
    }

    @Override
    public void setId(Integer id) {
        this.episodeId = id;
    }

    public Set<PatientProgram> getPatientPrograms() {
        return patientPrograms;
    }

    public void addEncounter(Encounter encounter) {
        getEncounters().add(encounter);
    }

    public void addPatientProgram(PatientProgram patientProgram) {
        getPatientPrograms().add(patientProgram);
    }

    public void setEpisodeId(Integer episodeId) {
        this.episodeId = episodeId;
    }

    public void setEncounters(Set<Encounter> encounters) {
        this.encounters = encounters;
    }

    public void setPatientPrograms(Set<PatientProgram> patientPrograms) {
        this.patientPrograms = patientPrograms;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
