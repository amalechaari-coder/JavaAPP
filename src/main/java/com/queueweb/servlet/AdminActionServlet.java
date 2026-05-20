package com.queueweb.servlet;

import com.queueweb.model.Ticket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gere les actions de l'administrateur.
 */
public class AdminActionServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String cabinetName = request.getParameter("cabinet");

        if (cabinetName == null || cabinetName.isBlank()) {
            cabinetName = getDefaultCabinet();
        }

        try {
            if ("add".equals(action)) {
                String patientName = request.getParameter("patientName");
                if (patientName == null || patientName.isBlank()) {
                    storeMessage(request, "Veuillez saisir le nom du patient.");
                } else {
                    Ticket ticket = queueService.addPatientByAdmin(patientName.trim(), cabinetName);
                    storeMessage(request, "Patient ajoute avec le ticket " + ticket.getNumber() + ".");
                }
            } else if ("next".equals(action)) {
                Ticket next = queueService.callNextPatient(cabinetName);
                if (next == null) {
                    storeMessage(request, "Aucun patient en attente dans ce cabinet.");
                } else {
                    storeMessage(request, "Patient appele : ticket " + next.getNumber() + " - " + next.getPatientName());
                }
            } else if ("reset".equals(action)) {
                queueService.resetCabinet(cabinetName);
                storeMessage(request, "La file du cabinet a ete reinitialisee.");
            } else {
                storeMessage(request, "Action admin inconnue.");
            }
        } catch (RuntimeException exception) {
            storeMessage(request, exception.getMessage());
        }

        redirectWithCabinet(request, response, "/admin", cabinetName);
    }
}
