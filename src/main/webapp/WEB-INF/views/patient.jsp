<%@ page import="java.util.List" %>
<%@ page import="com.queueweb.model.Ticket" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Espace patient</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/styles.css">
</head>
<body>
<%
    String patientName = (String) request.getAttribute("patientName");
    List<String> cabinets = (List<String>) request.getAttribute("cabinets");
    String selectedCabinet = (String) request.getAttribute("selectedCabinet");
    Ticket ticket = (Ticket) request.getAttribute("ticket");
    Integer patientsAhead = (Integer) request.getAttribute("patientsAhead");
    String statusMessage = (String) request.getAttribute("statusMessage");
    String message = (String) request.getAttribute("message");
    Boolean turnClose = (Boolean) request.getAttribute("turnClose");
%>
<div class="container">
    <div class="hero">
        <span class="badge">Patient</span>
        <h2>Espace patient</h2>
        <p>Prends un ticket a distance, suis ta position et reduis ton temps d'attente.</p>
    </div>

    <%
        if (message != null && !message.isBlank()) {
    %>
    <div class="alert success"><%= message %></div>
    <%
        }
    %>

    <%
        if (Boolean.TRUE.equals(turnClose)) {
    %>
    <div class="alert">Notification : votre tour approche dans le cabinet selectionne.</div>
    <%
        }
    %>

    <div class="grid">
        <div class="card">
            <h3><%= patientName == null ? "Connexion / Inscription" : "Session patient" %></h3>

            <%
                if (patientName == null) {
            %>
            <form action="<%= request.getContextPath() %>/auth" method="post">
                <input type="hidden" name="action" value="login">
                <label>Nom du patient</label>
                <input type="text" name="name" required>

                <label>Mot de passe</label>
                <input type="password" name="password" required>

                <button type="submit">Se connecter</button>
            </form>

            <hr style="margin: 22px 0; border: none; border-top: 1px solid #e6edf5;">

            <form action="<%= request.getContextPath() %>/auth" method="post">
                <input type="hidden" name="action" value="register">
                <label>Nom du patient</label>
                <input type="text" name="name" required>

                <label>Mot de passe</label>
                <input type="password" name="password" required>

                <button class="secondary" type="submit">S'inscrire</button>
            </form>
            <%
                } else {
            %>
            <p><strong>Connecte :</strong> <%= patientName %></p>

            <form action="<%= request.getContextPath() %>/logout" method="post">
                <button class="danger" type="submit">Se deconnecter</button>
            </form>
            <%
                }
            %>
        </div>

        <div class="card">
            <h3>Prendre un ticket</h3>

            <form action="<%= request.getContextPath() %>/patient" method="get">
                <label>Choisir un cabinet</label>
                <select name="cabinet">
                    <%
                        for (String cabinet : cabinets) {
                            boolean selected = cabinet.equals(selectedCabinet);
                    %>
                    <option value="<%= cabinet %>" <%= selected ? "selected" : "" %>><%= cabinet %></option>
                    <%
                        }
                    %>
                </select>
                <button type="submit">Actualiser</button>
            </form>

            <form action="<%= request.getContextPath() %>/patient/take-ticket" method="post">
                <input type="hidden" name="cabinet" value="<%= selectedCabinet %>">
                <button class="secondary" type="submit">Prendre un ticket</button>
            </form>

            <div class="stats">
                <div class="stat-box">
                    <span>Ton numero</span>
                    <strong><%= ticket == null ? "-" : ticket.getNumber() %></strong>
                </div>
                <div class="stat-box">
                    <span>Patients avant toi</span>
                    <strong><%= patientsAhead == null ? "-" : Math.max(patientsAhead, 0) %></strong>
                </div>
                <div class="stat-box">
                    <span>Heure du ticket</span>
                    <strong><%= ticket == null ? "-" : ticket.getFormattedCreatedAt() %></strong>
                </div>
            </div>

            <p style="margin-top: 18px;">
                <strong>Notification :</strong>
                <%= statusMessage == null ? "Connecte-toi puis prends un ticket." : statusMessage %>
            </p>
        </div>
    </div>
</div>
</body>
</html>
