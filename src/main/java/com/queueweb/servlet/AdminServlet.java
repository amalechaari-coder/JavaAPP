package com.queueweb.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Affiche l'espace administrateur.
 */
public class AdminServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String selectedCabinet = request.getParameter("cabinet");
        if (selectedCabinet == null || selectedCabinet.isBlank()) {
            selectedCabinet = getDefaultCabinet();
        }

        request.setAttribute("cabinets", queueService.getCabinetNames());
        request.setAttribute("selectedCabinet", selectedCabinet);
        request.setAttribute("currentTicket", queueService.getCurrentTicket(selectedCabinet));
        request.setAttribute("waitingTickets", queueService.getWaitingTickets(selectedCabinet));
        request.setAttribute("message", consumeMessage(request));

        forward(request, response, "/WEB-INF/views/admin.jsp");
    }
}
