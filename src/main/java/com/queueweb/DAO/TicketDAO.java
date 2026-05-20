package com.queueweb.DAO;

import com.queueweb.MDP.Cabinet;
import com.queueweb.MDP.Ticket;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO dedie aux tickets et a la logique de file d'attente.
 */
public class TicketDAO extends AbstractDAO {

    /**
     * Cree un ticket pour un patient.
     * Si un ticket actif existe deja, il est retourne.
     */
    public synchronized Ticket createTicket(String patientName, Integer patientId, Cabinet cabinet) {
        try {
            getConnection().setAutoCommit(false);

            Ticket existingTicket = findActiveTicket(patientName, cabinet.getId());
            if (existingTicket != null) {
                getConnection().commit();
                return existingTicket;
            }

            int nextNumber = getNextTicketNumber(cabinet.getId());

            try (PreparedStatement statement = getConnection().prepareStatement(
                    "INSERT INTO tickets (ticket_number, patient_name, patient_id, cabinet_id, status, ticket_date) " +
                            "VALUES (?, ?, ?, ?, 'waiting', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, nextNumber);
                statement.setString(2, patientName);

                if (patientId == null) {
                    statement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(3, patientId);
                }

                statement.setInt(4, cabinet.getId());
                statement.setDate(5, Date.valueOf(LocalDate.now()));
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    Ticket ticket = findById(generatedKeys.getInt(1));
                    getConnection().commit();
                    return ticket;
                }
            }
        } catch (SQLException exception) {
            rollbackQuietly();
            throw buildException("Impossible de creer le ticket.", exception);
        } finally {
            resetAutoCommit();
        }
    }

    /**
     * Retourne le ticket actif d'un patient pour aujourd'hui.
     */
    public synchronized Ticket findActiveTicket(String patientName, int cabinetId) {
        String sql = "SELECT t.id, t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE t.patient_name = ? AND t.cabinet_id = ? AND t.ticket_date = ? " +
                "AND t.status IN ('waiting', 'current') " +
                "ORDER BY t.ticket_number LIMIT 1";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, patientName);
            statement.setInt(2, cabinetId);
            statement.setDate(3, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTicket(resultSet);
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de recuperer le ticket actif.", exception);
        }
    }

    /**
     * Retourne le ticket actuellement appele.
     */
    public synchronized Ticket findCurrentTicket(int cabinetId) {
        String sql = "SELECT t.id, t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE t.cabinet_id = ? AND t.ticket_date = ? AND t.status = 'current' " +
                "ORDER BY t.ticket_number LIMIT 1";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, cabinetId);
            statement.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTicket(resultSet);
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de recuperer le ticket courant.", exception);
        }
    }

    /**
     * Retourne les tickets encore en attente.
     */
    public synchronized List<Ticket> findWaitingTickets(int cabinetId) {
        String sql = "SELECT t.id, t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE t.cabinet_id = ? AND t.ticket_date = ? AND t.status = 'waiting' " +
                "ORDER BY t.ticket_number";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, cabinetId);
            statement.setDate(2, Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (resultSet.next()) {
                    tickets.add(mapTicket(resultSet));
                }
                return tickets;
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de recuperer la liste d'attente.", exception);
        }
    }

    /**
     * Compte le nombre de personnes avant un ticket.
     * Le ticket courant est compte pour donner une position plus naturelle.
     */
    public synchronized int countPatientsAhead(int cabinetId, int ticketNumber) {
        String sql = "SELECT COUNT(*) AS total " +
                "FROM tickets " +
                "WHERE cabinet_id = ? AND ticket_date = ? " +
                "AND status IN ('waiting', 'current') AND ticket_number < ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, cabinetId);
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.setInt(3, ticketNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("total");
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de calculer la position du ticket.", exception);
        }
    }

    /**
     * Appelle le prochain patient du cabinet.
     */
    public synchronized Ticket callNextPatient(int cabinetId) {
        try {
            getConnection().setAutoCommit(false);

            try (PreparedStatement closeCurrent = getConnection().prepareStatement(
                    "UPDATE tickets SET status = 'done' " +
                            "WHERE cabinet_id = ? AND ticket_date = ? AND status = 'current'")) {
                closeCurrent.setInt(1, cabinetId);
                closeCurrent.setDate(2, Date.valueOf(LocalDate.now()));
                closeCurrent.executeUpdate();
            }

            Integer nextTicketId = findFirstWaitingTicketId(cabinetId);
            if (nextTicketId == null) {
                getConnection().commit();
                return null;
            }

            try (PreparedStatement promoteTicket = getConnection().prepareStatement(
                    "UPDATE tickets SET status = 'current' WHERE id = ?")) {
                promoteTicket.setInt(1, nextTicketId);
                promoteTicket.executeUpdate();
            }

            Ticket ticket = findById(nextTicketId);
            getConnection().commit();
            return ticket;
        } catch (SQLException exception) {
            rollbackQuietly();
            throw buildException("Impossible d'appeler le prochain patient.", exception);
        } finally {
            resetAutoCommit();
        }
    }

    /**
     * Supprime les tickets du jour pour un cabinet.
     */
    public synchronized void resetCabinet(int cabinetId) {
        String sql = "DELETE FROM tickets WHERE cabinet_id = ? AND ticket_date = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, cabinetId);
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw buildException("Impossible de reinitialiser la file.", exception);
        }
    }

    /**
     * Retourne un ticket a partir de son identifiant.
     */
    public synchronized Ticket findById(int ticketId) {
        String sql = "SELECT t.id, t.ticket_number, t.patient_name, c.name AS cabinet_name, " +
                "t.created_at, t.status " +
                "FROM tickets t " +
                "JOIN cabinets c ON c.id = t.cabinet_id " +
                "WHERE t.id = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapTicket(resultSet);
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de recuperer le ticket.", exception);
        }
    }

    /**
     * Recherche le premier ticket en attente du jour.
     */
    private Integer findFirstWaitingTicketId(int cabinetId) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(
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
     * Calcule le prochain numero a attribuer aujourd'hui.
     */
    private int getNextTicketNumber(int cabinetId) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(
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
     * Convertit une ligne SQL en objet Ticket.
     */
    private Ticket mapTicket(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        LocalDateTime createdDateTime = createdAt == null
                ? LocalDateTime.now()
                : createdAt.toLocalDateTime();

        Ticket ticket = new Ticket();
        ticket.setId(resultSet.getInt("id"));
        ticket.setNumber(resultSet.getInt("ticket_number"));
        ticket.setPatientName(resultSet.getString("patient_name"));
        ticket.setCabinetName(resultSet.getString("cabinet_name"));
        ticket.setStatus(resultSet.getString("status"));
        ticket.setCreatedAt(createdDateTime);
        return ticket;
    }

    /**
     * Annule une transaction en cas d'erreur.
     */
    private void rollbackQuietly() {
        try {
            getConnection().rollback();
        } catch (SQLException ignored) {
        }
    }

    /**
     * Remet la connexion en mode automatique.
     */
    private void resetAutoCommit() {
        try {
            getConnection().setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}
