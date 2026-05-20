package com.queueweb.service;

import com.queueweb.DAO.AbstractDAO;
import com.queueweb.DAO.CabinetDAO;
import com.queueweb.DAO.PatientDAO;
import com.queueweb.DAO.TicketDAO;
import com.queueweb.MDP.Cabinet;
import com.queueweb.MDP.Patient;
import com.queueweb.MDP.Ticket;

import java.util.Arrays;
import java.util.List;

/**
 * Service metier principal du projet JSF.
 * Il reste simple et s'appuie sur les classes DAO.
 */
public class QueueManagementService {
    private static final List<String> DEFAULT_CABINETS = Arrays.asList(
            "Cabinet General",
            "Cabinet Dentaire",
            "Cabinet Pediatrique"
    );

    private final PatientDAO patientDAO;
    private final CabinetDAO cabinetDAO;
    private final TicketDAO ticketDAO;

    /**
     * Initialise la base au premier appel du service.
     */
    public QueueManagementService() {
        AbstractDAO.initializeDatabase(DEFAULT_CABINETS);
        patientDAO = new PatientDAO();
        cabinetDAO = new CabinetDAO();
        ticketDAO = new TicketDAO();
    }

    /**
     * Retourne la liste des cabinets.
     */
    public List<Cabinet> getCabinets() {
        return cabinetDAO.findAll();
    }

    /**
     * Inscrit un patient.
     * Retourne false si le nom existe deja.
     */
    public boolean registerPatient(String name, String password) {
        if (isBlank(name) || isBlank(password)) {
            return false;
        }

        Patient patient = new Patient();
        patient.setName(name.trim());
        patient.setPassword(password);
        return patientDAO.insert(patient);
    }

    /**
     * Verifie la connexion du patient.
     */
    public boolean loginPatient(String name, String password) {
        if (isBlank(name) || isBlank(password)) {
            return false;
        }

        Patient patient = patientDAO.findByName(name.trim());
        return patient != null && password.equals(patient.getPassword());
    }

    /**
     * Cree un ticket depuis l'espace patient.
     */
    public Ticket takeTicket(String patientName, String cabinetName) {
        return createTicket(patientName, cabinetName, true);
    }

    /**
     * Cree un ticket depuis l'espace admin.
     */
    public Ticket addPatientByAdmin(String patientName, String cabinetName) {
        return createTicket(patientName, cabinetName, false);
    }

    /**
     * Retourne le ticket actif d'un patient et complete sa position.
     */
    public Ticket getActiveTicket(String patientName, String cabinetName) {
        Cabinet cabinet = cabinetDAO.findByName(cabinetName);
        if (cabinet == null || isBlank(patientName)) {
            return null;
        }

        Ticket ticket = ticketDAO.findActiveTicket(patientName.trim(), cabinet.getId());
        return enrichTicket(ticket, cabinet);
    }

    /**
     * Retourne le nombre de personnes avant le patient.
     */
    public int getPatientsAhead(String patientName, String cabinetName) {
        Ticket ticket = getActiveTicket(patientName, cabinetName);
        if (ticket == null) {
            return -1;
        }
        return ticket.getPatientsAhead();
    }

    /**
     * Construit un message simple a afficher au patient.
     */
    public String getPatientStatusMessage(String patientName, String cabinetName) {
        Ticket ticket = getActiveTicket(patientName, cabinetName);
        if (ticket == null) {
            return "Aucun ticket actif";
        }

        if (ticket.isCurrent()) {
            return "C'est votre tour, presentez-vous au cabinet.";
        }

        if (ticket.getPatientsAhead() == 0) {
            return "Vous etes le prochain patient a appeler.";
        }

        if (ticket.getPatientsAhead() == 1) {
            return "Votre tour approche.";
        }

        return ticket.getPatientsAhead() + " patients avant vous.";
    }

    /**
     * Indique si le tour est proche.
     */
    public boolean isTurnClose(String patientName, String cabinetName) {
        int patientsAhead = getPatientsAhead(patientName, cabinetName);
        return patientsAhead >= 0 && patientsAhead <= 1;
    }

    /**
     * Retourne la liste des tickets en attente.
     */
    public List<Ticket> getWaitingTickets(String cabinetName) {
        Cabinet cabinet = cabinetDAO.findByName(cabinetName);
        if (cabinet == null) {
            return List.of();
        }

        List<Ticket> tickets = ticketDAO.findWaitingTickets(cabinet.getId());
        for (Ticket ticket : tickets) {
            ticket.setPatientsAhead(ticketDAO.countPatientsAhead(cabinet.getId(), ticket.getNumber()));
        }
        return tickets;
    }

    /**
     * Retourne le ticket actuellement appele.
     */
    public Ticket getCurrentTicket(String cabinetName) {
        Cabinet cabinet = cabinetDAO.findByName(cabinetName);
        if (cabinet == null) {
            return null;
        }
        return enrichTicket(ticketDAO.findCurrentTicket(cabinet.getId()), cabinet);
    }

    /**
     * Appelle le prochain patient.
     */
    public Ticket callNextPatient(String cabinetName) {
        Cabinet cabinet = cabinetDAO.findByName(cabinetName);
        if (cabinet == null) {
            return null;
        }
        return enrichTicket(ticketDAO.callNextPatient(cabinet.getId()), cabinet);
    }

    /**
     * Reinitialise la file d'un cabinet pour la journee.
     */
    public void resetCabinet(String cabinetName) {
        Cabinet cabinet = cabinetDAO.findByName(cabinetName);
        if (cabinet != null) {
            ticketDAO.resetCabinet(cabinet.getId());
        }
    }

    /**
     * Cree un ticket en tenant compte du contexte patient ou admin.
     */
    private Ticket createTicket(String patientName, String cabinetName, boolean requireRegisteredPatient) {
        if (isBlank(patientName) || isBlank(cabinetName)) {
            return null;
        }

        Cabinet cabinet = cabinetDAO.findByName(cabinetName);
        if (cabinet == null) {
            return null;
        }

        Integer patientId = null;
        Patient patient = patientDAO.findByName(patientName.trim());

        if (requireRegisteredPatient && patient == null) {
            return null;
        }

        if (patient != null) {
            patientId = patient.getId();
        }

        return enrichTicket(ticketDAO.createTicket(patientName.trim(), patientId, cabinet), cabinet);
    }

    /**
     * Ajoute au ticket sa position dans la file.
     */
    private Ticket enrichTicket(Ticket ticket, Cabinet cabinet) {
        if (ticket == null) {
            return null;
        }

        if (ticket.isCurrent()) {
            ticket.setPatientsAhead(0);
        } else {
            ticket.setPatientsAhead(ticketDAO.countPatientsAhead(cabinet.getId(), ticket.getNumber()));
        }

        return ticket;
    }

    /**
     * Evite de dupliquer les tests de chaines vides.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
