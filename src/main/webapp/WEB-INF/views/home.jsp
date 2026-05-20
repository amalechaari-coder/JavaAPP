<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>JavaAppJEE</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/styles.css">
</head>
<body>
<div class="container">
    <div class="hero">
        <span class="badge">JEE Web App</span>
        <h1>Application de gestion des files d'attente</h1>
        <p>Version web simple en Servlet, JSP et JDBC. Le patient prend un ticket en ligne et l'admin gere la file.</p>
    </div>

    <%
        String message = (String) request.getAttribute("message");
        if (message != null && !message.isBlank()) {
    %>
    <div class="alert success"><%= message %></div>
    <%
        }
    %>

    <div class="link-row">
        <a class="link-card" href="<%= request.getContextPath() %>/patient">
            <div class="card">
                <span class="badge">Patient</span>
                <h3>Espace patient</h3>
                <p>Inscription, connexion, prise de ticket, suivi du numero et notification simple.</p>
            </div>
        </a>

        <a class="link-card" href="<%= request.getContextPath() %>/admin">
            <div class="card">
                <span class="badge">Admin</span>
                <h3>Espace admin</h3>
                <p>Ajout de patient, affichage de la file, appel du suivant et reinitialisation.</p>
            </div>
        </a>
    </div>
</div>
</body>
</html>
