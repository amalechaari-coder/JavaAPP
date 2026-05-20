package com.queueweb.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Deconnecte le patient.
 */
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().removeAttribute("patientName");
        storeMessage(request, "Deconnexion reussie.");
        redirect(request, response, "/patient");
    }
}
