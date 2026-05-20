<%@ page import="java.util.List" %>
<%@ page import="com.queueweb.model.Ticket" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Espace admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/styles.css">
</head>
<body>
<%
    List<String> cabinets = (List<String>) request.getAttribute("cabinets");
    String selectedCabinet = (String) request.getAttribute("selectedCabinet");
    Ticket currentTicket = (Ticket) request.getAttribute("currentTicket");
    List<Ticket> waitingTickets = (List<Ticket>) request.getAttribute("waitingTickets");
    String message = (String) request.getAttribute("message");
%>
<div class="container">
    <div class="hero">
        <span class="badge">Admin</span>
        <h2>Espace administrateur</h2>
        <p>Ajoute des patients, appelle le suivant, consulte la file d'attente et reinitialise la file du jour.</p>
    </div>

    <%
        if (message != null && !message.isBlank()) {
    %>
    <div class="alert success"><%= message %></div>
    <%
        }
    %>

    <div class="grid">
        <div class="card">
            <h3>Cabinet et actions</h3>

            <form action="<%= request.getContextPath() %>/admin" method="get">
                <label>Cabinet</label>
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
                <button type="submit">Afficher ce cabinet</button>
            </form>

            <div class="stats">
                <div class="stat-box">
                    <span>Numero actuel</span>
                    <strong><%= currentTicket == null ? "-" : currentTicket.getNumber() %></strong>
                </div>
                <div class="stat-box">
                    <span>Patient actuel</span>
                    <strong><%= currentTicket == null ? "-" : currentTicket.getPatientName() %></strong>
                </div>
                <div class="stat-box">
                    <span>Patients en attente</span>
                    <strong><%= waitingTickets == null ? 0 : waitingTickets.size() %></strong>
                </div>
            </div>

            <form action="<%= request.getContextPath() %>/admin/action" method="post">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="cabinet" value="<%= selectedCabinet %>">
                <label>Nom du patient</label>
                <input type="text" name="patientName" required>
                <button class="secondary" type="submit">Ajouter patient</button>
            </form>

            <div class="actions" style="margin-top: 18px;">
                <form action="<%= request.getContextPath() %>/admin/action" method="post">
                    <input type="hidden" name="action" value="next">
                    <input type="hidden" name="cabinet" value="<%= selectedCabinet %>">
                    <button type="submit">Suivant</button>
                </form>

                <form action="<%= request.getContextPath() %>/admin/action" method="post">
                    <input type="hidden" name="action" value="reset">
                    <input type="hidden" name="cabinet" value="<%= selectedCabinet %>">
                    <button class="danger" type="submit">Reinitialiser</button>
                </form>
            </div>
        </div>

        <div class="card">
            <h3>Liste d'attente</h3>

            <%
                if (waitingTickets == null || waitingTickets.isEmpty()) {
            %>
            <p>Aucun patient en attente dans ce cabinet.</p>
            <%
                } else {
            %>
            <table>
                <thead>
                <tr>
                    <th>Ticket</th>
                    <th>Patient</th>
                    <th>Heure</th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (Ticket ticket : waitingTickets) {
                %>
                <tr>
                    <td><%= ticket.getNumber() %></td>
                    <td><%= ticket.getPatientName() %></td>
                    <td><%= ticket.getFormattedCreatedAt() %></td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
            <%
                }
            %>
        </div>
    </div>
</div>
</body>
</html>
