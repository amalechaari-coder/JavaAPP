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
 * Bean JSF cote patient.
 * Il gere l'inscription, la connexion et le suivi du ticket.
 */
@Named
@SessionScoped
public class PatientBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private final QueueManagementService service = new QueueManagementService();

    private List<Cabinet> cabinets;
    private String patientName;
    private String password;
    private String selectedCabinet;
    private Ticket activeTicket;
    private String statusMessage;
    private boolean loggedIn;
    private boolean turnNotificationShown;

    /**
     * Charge les cabinets des l'ouverture de session.
     */
    @PostConstruct
    public void init() {
        cabinets = new ArrayList<>(service.getCabinets());

        if (!cabinets.isEmpty()) {
            selectedCabinet = cabinets.get(0).getName();
        }
    }

    /**
     * Inscrit le patient courant.
     */
    public String register() {
        if (service.registerPatient(patientName, password)) {
            addMessage(FacesMessage.SEVERITY_INFO, "Inscription reussie", "Vous pouvez maintenant vous connecter.");
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Inscription impossible", "Verifiez le nom et le mot de passe.");
        }
        return null;
    }

    /**
     * Connecte le patient puis ouvre sa page.
     */
    public String login() {
        if (!service.loginPatient(patientName, password)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Connexion refusee", "Nom ou mot de passe incorrect.");
            return null;
        }

        loggedIn = true;
        turnNotificationShown = false;
        refreshTicket();
        addMessage(FacesMessage.SEVERITY_INFO, "Connexion reussie", "Bienvenue " + patientName + ".");
        return "patient.xhtml?faces-redirect=true";
    }

    /**
     * Prend un ticket pour le cabinet selectionne.
     */
    public String takeTicket() {
        if (!loggedIn) {
            addMessage(FacesMessage.SEVERITY_WARN, "Connexion requise", "Connectez-vous avant de prendre un ticket.");
            return null;
        }

        activeTicket = service.takeTicket(patientName, selectedCabinet);
        refreshTicket();

        if (activeTicket == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Ticket impossible", "Verifiez le cabinet selectionne.");
        } else {
            addMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Ticket cree",
                    "Votre numero est " + activeTicket.getNumber() + "."
            );
        }

        return null;
    }

    /**
     * Recharge le ticket et le message de statut.
     * Cette methode est utilisee par le poll JSF.
     */
    public void refreshTicket() {
        if (!loggedIn || selectedCabinet == null) {
            activeTicket = null;
            statusMessage = "Connectez-vous pour suivre votre ticket.";
            return;
        }

        activeTicket = service.getActiveTicket(patientName, selectedCabinet);
        statusMessage = service.getPatientStatusMessage(patientName, selectedCabinet);

        if (activeTicket == null) {
            turnNotificationShown = false;
            return;
        }

        if ((activeTicket.isCurrent() || activeTicket.isTurnClose()) && !turnNotificationShown) {
            addMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Notification",
                    "Votre tour approche pour le " + selectedCabinet + "."
            );
            turnNotificationShown = true;
        }
    }

    /**
     * Change le cabinet courant puis recharge les informations.
     */
    public void onCabinetChange() {
        turnNotificationShown = false;
        refreshTicket();
    }

    /**
     * Deconnecte le patient et vide les donnees de session.
     */
    public String logout() {
        activeTicket = null;
        statusMessage = null;
        loggedIn = false;
        turnNotificationShown = false;
        password = null;
        return "index.xhtml?faces-redirect=true";
    }

    /**
     * Ajoute un message PrimeFaces/JSF visible a l'ecran.
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
     * Retourne le nom du patient courant.
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * Definit le nom du patient courant.
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * Retourne le mot de passe saisi.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Definit le mot de passe saisi.
     */
    public void setPassword(String password) {
        this.password = password;
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
     * Retourne le ticket actif.
     */
    public Ticket getActiveTicket() {
        return activeTicket;
    }

    /**
     * Retourne le message de statut du patient.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Indique si le patient est connecte.
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
}
