package com.queueweb.service;

import com.queueweb.config.DatabaseManager;
import com.queueweb.model.Ticket;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service metier principal de l'application web.
 * Il contient une logique simple, claire et proche du projet desktop.
 */
public class QueueService {
    private final List<String> cabinetNames = Arrays.asList(
            "Cabinet General",
            "Cabinet Dentaire",
            "Cabinet Pediatrique"
    );

    /**
     * Initialise la base de donnees au demarrage.
     */
    public QueueService() {
        DatabaseManager.initializeDatabase(cabinetNames);
    }

    /**
     * Retourne la liste fixe des cabinets.
     */
    public List<String> getCabinetNames() {
        return new ArrayList<>(cabinetNames);
    }

    /**
     * Inscrit un patient si le nom n'existe pas deja.
     */
    public boolean registerPatient(String name, String password) {
        String sql = "INSERT INTO patients (name, password) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, password);
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            if (isDuplicateKey(exception)) {
                return false;
            }
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Verifie la connexion d'un patient.
     */
    public boolean loginPatient(String name, String password) {
        String sql = "SELECT password FROM patients WHERE name = ?";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return password.equals(resultSet.getString("password"));
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Cree un ticket depuis l'espace patient.
     */
    public Ticket takeTicket(String patientName, String cabinetName) {
        return createTicket(patientName, cabinetName);
    }

    /**
     * Cree un ticket depuis l'espace admin.
     */
    public Ticket addPatientByAdmin(String patientName, String cabinetName) {
        return createTicket(patientName, cabinetName);
    }

    /**
     * Appelle le prochain patient du cabinet selectionne.
     */
    public Ticket callNextPatient(String cabinetName) {
        try (Connection connection = DatabaseManager.openConnection()) {
            connection.setAutoCommit(false);

            try {
                int cabinetId = getCabinetId(connection, cabinetName);

                try (PreparedStatement closeCurrent = connection.prepareStatement(
                        "UPDATE tickets SET status = 'done' " +
                                "WHERE cabinet_id = ? AND ticket_date = ? AND status = 'current'")) {
                    closeCurrent.setInt(1, cabinetId);
                    closeCurrent.setDate(2, Date.valueOf(LocalDate.now()));
                    closeCurrent.executeUpdate();
                }

                Integer nextTicketId = getFirstWaitingTicketId(connection, cabinetId);
                if (nextTicketId == null) {
                    connection.commit();
                    return null;
                }

                try (PreparedStatement promoteTicket = connection.prepareStatement(
                        "UPDATE tickets SET status = 'current' WHERE id = ?")) {
                    promoteTicket.setInt(1, nextTicketId);
                    promoteTicket.executeUpdate();
                }

                Ticket nextTicket = getTicketById(connection, nextTicketId);
                connection.commit();
                return nextTicket;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Reinitialise la file du jour pour un cabinet.
     */
    public void resetCabinet(String cabinetName) {
        String sql = "DELETE FROM tickets WHERE cabinet_id = ? AND ticket_date = ?";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, getCabinetId(connection, cabinetName));
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Retourne le ticket actif d'un patient.
     */
    public Ticket getActiveTicket(String patientName, String cabinetName) {
        String sql = "SELECT t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE t.patient_name = ? AND c.name = ? AND t.ticket_date = ? " +
                "AND t.status IN ('waiting', 'current') " +
                "ORDER BY t.ticket_number LIMIT 1";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientName);
            statement.setString(2, cabinetName);
            statement.setDate(3, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTicket(resultSet);
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Retourne le nombre de patients avant un patient donne.
     */
    public int getPatientsAhead(String patientName, String cabinetName) {
        Ticket ticket = getActiveTicket(patientName, cabinetName);
        if (ticket == null) {
            return -1;
        }

        if ("current".equals(ticket.getStatus())) {
            return 0;
        }

        String sql = "SELECT COUNT(*) AS total " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE c.name = ? AND t.ticket_date = ? " +
                "AND t.status = 'waiting' AND t.ticket_number < ?";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cabinetName);
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.setInt(3, ticket.getNumber());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("total");
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Retourne le message de statut du patient.
     */
    public String getPatientStatusMessage(String patientName, String cabinetName) {
        Ticket ticket = getActiveTicket(patientName, cabinetName);
        if (ticket == null) {
            return "Aucun ticket actif";
        }

        if ("current".equals(ticket.getStatus())) {
            return "C'est votre tour, presentez-vous au cabinet";
        }

        int patientsAhead = getPatientsAhead(patientName, cabinetName);
        if (patientsAhead == 0) {
            return "Plus aucun patient en attente avant vous";
        }
        if (patientsAhead == 1) {
            return "Votre tour approche";
        }
        if (patientsAhead > 1) {
            return patientsAhead + " patients avant vous";
        }

        return "En attente";
    }

    /**
     * Indique si le tour est proche.
     */
    public boolean isTurnClose(String patientName, String cabinetName) {
        int patientsAhead = getPatientsAhead(patientName, cabinetName);
        return patientsAhead >= 0 && patientsAhead <= 1;
    }

    /**
     * Retourne la liste d'attente du cabinet.
     */
    public List<Ticket> getWaitingTickets(String cabinetName) {
        String sql = "SELECT t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE c.name = ? AND t.ticket_date = ? AND t.status = 'waiting' " +
                "ORDER BY t.ticket_number";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cabinetName);
            statement.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
                return tickets;
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Retourne le ticket actuellement appele.
     */
    public Ticket getCurrentTicket(String cabinetName) {
        String sql = "SELECT t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE c.name = ? AND t.ticket_date = ? AND t.status = 'current' " +
                "ORDER BY t.ticket_number LIMIT 1";

        try (Connection connection = DatabaseManager.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cabinetName);
            statement.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTicket(resultSet);
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Cree un ticket si le patient n'en a pas deja un actif.
     */
    private Ticket createTicket(String patientName, String cabinetName) {
        try (Connection connection = DatabaseManager.openConnection()) {
            connection.setAutoCommit(false);

            try {
                int cabinetId = getCabinetId(connection, cabinetName);
                Ticket existingTicket = findActiveTicket(connection, patientName, cabinetName);
                if (existingTicket != null) {
                    connection.commit();
                    return existingTicket;
                }

                int nextNumber = getNextTicketNumber(connection, cabinetId);
                Integer patientId = getPatientId(connection, patientName);

                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO tickets " +
                                "(ticket_number, patient_name, patient_id, cabinet_id, status, ticket_date) " +
                                "VALUES (?, ?, ?, ?, 'waiting', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, nextNumber);
                    statement.setString(2, patientName);

                    if (patientId == null) {
                        statement.setNull(3, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(3, patientId);
                    }

                    statement.setInt(4, cabinetId);
                    statement.setDate(5, Date.valueOf(LocalDate.now()));
                    statement.executeUpdate();

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        generatedKeys.next();
                        Ticket ticket = getTicketById(connection, generatedKeys.getInt(1));
                        connection.commit();
                        return ticket;
                    }
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw buildDatabaseException(exception);
        }
    }

    /**
     * Cherche un ticket actif d'un patient.
     */
    private Ticket findActiveTicket(Connection connection, String patientName, String cabinetName) throws SQLException {
        String sql = "SELECT t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE t.patient_name = ? AND c.name = ? AND t.ticket_date = ? " +
                "AND t.status IN ('waiting', 'current') " +
                "ORDER BY t.ticket_number LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientName);
            statement.setString(2, cabinetName);
            statement.setDate(3, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTicket(resultSet);
            }
        }
    }

    /**
     * Retourne l'id du cabinet.
     */
    private int getCabinetId(Connection connection, String cabinetName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM cabinets WHERE name = ?")) {
            statement.setString(1, cabinetName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Cabinet introuvable: " + cabinetName);
                }
                return resultSet.getInt("id");
            }
        }
    }

    /**
     * Retourne l'id du patient si ce patient est inscrit.
     */
    private Integer getPatientId(Connection connection, String patientName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM patients WHERE name = ?")) {
            statement.setString(1, patientName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getInt("id");
            }
        }
    }

    /**
     * Retourne le premier ticket en attente.
     */
    private Integer getFirstWaitingTicketId(Connection connection, int cabinetId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM tickets " +
                        "WHERE cabinet_id = ? AND ticket_date = ? AND status = 'waiting' " +
                        "ORDER BY ticket_number LIMIT 1")) {
            statement.setInt(1, cabinetId);
            statement.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getInt("id");
            }
        }
    }

    /**
     * Retourne le prochain numero pour aujourd'hui.
     */
    private int getNextTicketNumber(Connection connection, int cabinetId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COALESCE(MAX(ticket_number), 0) + 1 AS next_number " +
                        "FROM tickets WHERE cabinet_id = ? AND ticket_date = ?")) {
            statement.setInt(1, cabinetId);
            statement.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("next_number");
            }
        }
    }

    /**
     * Recharge un ticket complet depuis son id.
     */
    private Ticket getTicketById(Connection connection, int ticketId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                        "t.created_at, t.status " +
                        "FROM tickets t " +
                        "JOIN cabinets c ON c.id = t.cabinet_id " +
                        "WHERE t.id = ?")) {
            statement.setInt(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Ticket introuvable apres insertion.");
                }
                return mapTicket(resultSet);
            }
        }
    }

    /**
     * Convertit une ligne SQL en objet Ticket.
     */
    private Ticket mapTicket(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        LocalDateTime createdDateTime = createdAt == null
                ? LocalDateTime.now()
                : createdAt.toLocalDateTime();

        return new Ticket(
                resultSet.getInt("ticket_number"),
                resultSet.getString("patient_name"),
                resultSet.getString("cabinet_name"),
                createdDateTime,
                resultSet.getString("status")
        );
    }

    /**
     * Detecte une erreur de doublon SQL.
     */
    private boolean isDuplicateKey(SQLException exception) {
        return exception.getErrorCode() == 1062 || "23000".equals(exception.getSQLState());
    }

    /**
     * Retourne une erreur plus claire pour un projet academique.
     */
    private RuntimeException buildDatabaseException(SQLException exception) {
        return new RuntimeException(
                "Erreur MySQL. Verifie que XAMPP/MySQL est lance, que le port 3000 est correct "
                        + "et que le driver JDBC est present dans WEB-INF/lib.",
                exception
        );
    }
}
