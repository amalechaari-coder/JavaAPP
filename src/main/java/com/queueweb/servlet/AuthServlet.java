package com.queueweb.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Gere l'inscription et la connexion du patient.
 */
public class AuthServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        if (name == null || name.isBlank() || password == null || password.isBlank()) {
            storeMessage(request, "Veuillez remplir le nom et le mot de passe.");
            redirect(request, response, "/patient");
            return;
        }

        try {
            if ("register".equals(action)) {
                boolean success = queueService.registerPatient(name.trim(), password.trim());
                if (!success) {
                    storeMessage(request, "Ce patient existe deja.");
                    redirect(request, response, "/patient");
                    return;
                }

                request.getSession().setAttribute("patientName", name.trim());
                storeMessage(request, "Inscription reussie.");
                redirect(request, response, "/patient");
                return;
            }

            if ("login".equals(action)) {
                boolean success = queueService.loginPatient(name.trim(), password.trim());
                if (!success) {
                    storeMessage(request, "Nom ou mot de passe incorrect.");
                    redirect(request, response, "/patient");
                    return;
                }

                request.getSession().setAttribute("patientName", name.trim());
                storeMessage(request, "Connexion reussie.");
                redirect(request, response, "/patient");
                return;
            }

            storeMessage(request, "Action de connexion inconnue.");
            redirect(request, response, "/patient");
        } catch (RuntimeException exception) {
            storeMessage(request, exception.getMessage());
            redirect(request, response, "/patient");
        }
    }
}
