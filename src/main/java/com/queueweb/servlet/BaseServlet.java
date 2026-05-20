package com.queueweb.servlet;

import com.queueweb.service.QueueService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servlet de base pour factoriser quelques operations communes.
 */
public abstract class BaseServlet extends HttpServlet {
    protected static final QueueService queueService = new QueueService();

    /**
     * Place un message temporaire dans la session.
     */
    protected void storeMessage(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashMessage", message);
    }

    /**
     * Recupere puis supprime le message temporaire.
     */
    protected String consumeMessage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String message = (String) session.getAttribute("flashMessage");
        session.removeAttribute("flashMessage");
        return message;
    }

    /**
     * Fait un forward vers une JSP.
     */
    protected void forward(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
    }

    /**
     * Redirige vers un chemin de l'application.
     */
    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * Redirige vers un chemin avec un parametre cabinet.
     */
    protected void redirectWithCabinet(HttpServletRequest request, HttpServletResponse response,
                                       String path, String cabinetName) throws IOException {
        String encodedCabinet = URLEncoder.encode(cabinetName, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + path + "?cabinet=" + encodedCabinet);
    }

    /**
     * Retourne le premier cabinet par defaut.
     */
    protected String getDefaultCabinet() {
        List<String> cabinets = queueService.getCabinetNames();
        return cabinets.isEmpty() ? "" : cabinets.get(0);
    }
}
