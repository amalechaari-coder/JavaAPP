package com.queueweb.beans;

import com.queueweb.MDP.Cabinet;
import com.queueweb.MDP.Ticket;
import com.queueweb.service.QueueManagementService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean JSF cote admin.
 * Il gere l'ajout de patients, l'appel du suivant et la vue de la file.
 */
@Named
@SessionScoped
public class AdminBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private final QueueManagementService service = new QueueManagementService();

    private List<Cabinet> cabinets;
    private List<Ticket> waitingTickets;
    private String selectedCabinet;
    private String newPatientName;
    private Ticket currentTicket;

    /**
     * Charge la liste initiale des cabinets et de la file.
     */
    @PostConstruct
    public void init() {
        cabinets = new ArrayList<>(service.getCabinets());

        if (!cabinets.isEmpty()) {
            selectedCabinet = cabinets.get(0).getName();
        }

        loadQueue();
    }

    /**
     * Recharge les donnees de la file d'attente.
     */
    public void loadQueue() {
        if (selectedCabinet == null) {
            waitingTickets = new ArrayList<>();
            currentTicket = null;
            return;
        }

        waitingTickets = new ArrayList<>(service.getWaitingTickets(selectedCabinet));
        currentTicket = service.getCurrentTicket(selectedCabinet);
    }

    /**
     * Ajoute un patient a la file depuis l'espace admin.
     */
    public String addPatient() {
        Ticket ticket = service.addPatientByAdmin(newPatientName, selectedCabinet);
        loadQueue();

        if (ticket == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Ajout impossible", "Verifiez le nom du patient et le cabinet.");
        } else {
            addMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Patient ajoute",
                    "Ticket " + ticket.getNumber() + " cree pour " + ticket.getPatientName() + "."
            );
            newPatientName = null;
        }

        return null;
    }

    /**
     * Appelle le prochain patient.
     */
    public String callNext() {
        Ticket ticket = service.callNextPatient(selectedCabinet);
        loadQueue();

        if (ticket == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aucun patient", "La file d'attente est vide.");
        } else {
            addMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Patient appele",
                    "Le ticket courant est maintenant " + ticket.getNumber() + "."
            );
        }

        return null;
    }

    /**
     * Reinitialise toute la file du cabinet selectionne.
     */
    public String resetQueue() {
        service.resetCabinet(selectedCabinet);
        loadQueue();
        addMessage(FacesMessage.SEVERITY_INFO, "File reinitialisee", "La file du jour a ete videe.");
        return null;
    }

    /**
     * Recharge la file quand l'admin change de cabinet.
     */
    public void onCabinetChange() {
        loadQueue();
    }

    /**
     * Ajoute un message visible sur l'interface admin.
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    /**
     * Retourne la liste des cabinets.
     */
    public List<Cabinet> getCabinets() {
        return cabinets;
    }

    /**
     * Retourne les tickets en attente.
     */
    public List<Ticket> getWaitingTickets() {
        return waitingTickets;
    }

    /**
     * Retourne le cabinet selectionne.
     */
    public String getSelectedCabinet() {
        return selectedCabinet;
    }

    /**
     * Definit le cabinet selectionne.
     */
    public void setSelectedCabinet(String selectedCabinet) {
        this.selectedCabinet = selectedCabinet;
    }

    /**
     * Retourne le nom du patient saisi par l'admin.
     */
    public String getNewPatientName() {
        return newPatientName;
    }

    /**
     * Definit le nom du patient saisi par l'admin.
     */
    public void setNewPatientName(String newPatientName) {
        this.newPatientName = newPatientName;
    }

    /**
     * Retourne le ticket actuellement appele.
     */
    public Ticket getCurrentTicket() {
        return currentTicket;
    }
}
