package com.queueweb.MDP;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modele simple d'un ticket de file d'attente.
 */
public class Ticket {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private int id;
    private int number;
    private String patientName;
    private String cabinetName;
    private String status;
    private int patientsAhead;
    private LocalDateTime createdAt;

    /**
     * Retourne l'identifiant technique du ticket.
     */
    public int getId() {
        return id;
    }

    /**
     * Definit l'identifiant technique du ticket.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne le numero visible du ticket.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Definit le numero visible du ticket.
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Retourne le nom du patient.
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * Definit le nom du patient.
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * Retourne le nom du cabinet.
     */
    public String getCabinetName() {
        return cabinetName;
    }

    /**
     * Definit le nom du cabinet.
     */
    public void setCabinetName(String cabinetName) {
        this.cabinetName = cabinetName;
    }

    /**
     * Retourne l'etat du ticket: waiting, current ou done.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Definit l'etat du ticket.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retourne le nombre de personnes avant ce ticket.
     */
    public int getPatientsAhead() {
        return patientsAhead;
    }

    /**
     * Definit le nombre de personnes avant ce ticket.
     */
    public void setPatientsAhead(int patientsAhead) {
        this.patientsAhead = patientsAhead;
    }

    /**
     * Retourne l'heure de creation du ticket.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Definit l'heure de creation du ticket.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retourne l'heure de creation sous forme simple.
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "";
        }
        return createdAt.format(FORMATTER);
    }

    /**
     * Indique si ce ticket est le ticket actuellement appele.
     */
    public boolean isCurrent() {
        return "current".equals(status);
    }

    /**
     * Indique si le ticket est proche du passage.
     */
    public boolean isTurnClose() {
        return !isCurrent() && patientsAhead <= 1;
    }
}
