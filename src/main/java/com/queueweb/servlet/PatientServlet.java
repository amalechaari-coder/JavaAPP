package com.queueweb.servlet;

import com.queueweb.model.Ticket;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Affiche l'espace patient.
 */
public class PatientServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String selectedCabinet = request.getParameter("cabinet");
        if (selectedCabinet == null || selectedCabinet.isBlank()) {
            selectedCabinet = getDefaultCabinet();
        }

        String patientName = (String) request.getSession().getAttribute("patientName");

        request.setAttribute("cabinets", queueService.getCabinetNames());
        request.setAttribute("selectedCabinet", selectedCabinet);
        request.setAttribute("patientName", patientName);
        request.setAttribute("message", consumeMessage(request));

        if (patientName != null) {
            Ticket ticket = queueService.getActiveTicket(patientName, selectedCabinet);
            request.setAttribute("ticket", ticket);

            if (ticket != null) {
                request.setAttribute("patientsAhead", queueService.getPatientsAhead(patientName, selectedCabinet));
                request.setAttribute("statusMessage", queueService.getPatientStatusMessage(patientName, selectedCabinet));
                request.setAttribute("turnClose", queueService.isTurnClose(patientName, selectedCabinet));
            }
        }

        forward(request, response, "/WEB-INF/views/patient.jsp");
    }
}
