package com.queueweb.DAO;

import com.queueweb.MDP.Patient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO dedie aux patients.
 */
public class PatientDAO extends AbstractDAO {

    /**
     * Retourne un patient a partir de son nom.
     */
    public Patient findByName(String name) {
        String sql = "SELECT id, name, password FROM patients WHERE name = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapPatient(resultSet);
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de chercher le patient.", exception);
        }
    }

    /**
     * Insere un nouveau patient.
     * Retourne false si le nom existe deja.
     */
    public boolean insert(Patient patient) {
        String sql = "INSERT INTO patients (name, password) VALUES (?, ?)";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, patient.getName());
            statement.setString(2, patient.getPassword());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062 || "23000".equals(exception.getSQLState())) {
                return false;
            }
            throw buildException("Impossible d'ajouter le patient.", exception);
        }
    }

    /**
     * Convertit une ligne SQL en objet Patient.
     */
    private Patient mapPatient(ResultSet resultSet) throws SQLException {
        Patient patient = new Patient();
        patient.setId(resultSet.getInt("id"));
        patient.setName(resultSet.getString("name"));
        patient.setPassword(resultSet.getString("password"));
        return patient;
    }
}
