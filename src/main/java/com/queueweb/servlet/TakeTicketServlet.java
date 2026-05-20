package com.queueweb.servlet;

import com.queueweb.model.Ticket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Permet au patient de prendre un ticket.
 */
public class TakeTicketServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String patientName = (String) request.getSession().getAttribute("patientName");
        String cabinetName = request.getParameter("cabinet");

        if (cabinetName == null || cabinetName.isBlank()) {
            cabinetName = getDefaultCabinet();
        }

        if (patientName == null) {
            storeMessage(request, "Veuillez vous connecter avant de prendre un ticket.");
            redirectWithCabinet(request, response, "/patient", cabinetName);
            return;
        }

        try {
            Ticket ticket = queueService.takeTicket(patientName, cabinetName);
            storeMessage(request, "Ticket pris avec succes. Votre numero est " + ticket.getNumber() + ".");
        } catch (RuntimeException exception) {
            storeMessage(request, exception.getMessage());
        }

        redirectWithCabinet(request, response, "/patient", cabinetName);
    }
}
