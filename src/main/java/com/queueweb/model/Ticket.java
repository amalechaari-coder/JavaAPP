package com.queueweb.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modele simple d'un ticket pour l'application web.
 */
public class Ticket {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final int number;
    private final String patientName;
    private final String cabinetName;
    private final LocalDateTime createdAt;
    private final String status;

    /**
     * Cree un ticket complet.
     */
    public Ticket(int number, String patientName, String cabinetName, LocalDateTime createdAt, String status) {
        this.number = number;
        this.patientName = patientName;
        this.cabinetName = cabinetName;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getCabinetName() {
        return cabinetName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(FORMATTER);
    }
}
