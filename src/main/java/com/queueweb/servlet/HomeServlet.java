package com.queueweb.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Affiche la page d'accueil.
 */
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", consumeMessage(request));
        forward(request, response, "/WEB-INF/views/home.jsp");
    }
}
