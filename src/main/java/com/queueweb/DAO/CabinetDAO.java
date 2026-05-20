package com.queueweb.DAO;

import com.queueweb.MDP.Cabinet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO dedie aux cabinets.
 */
public class CabinetDAO extends AbstractDAO {

    /**
     * Retourne tous les cabinets dans l'ordre d'insertion.
     */
    public List<Cabinet> findAll() {
        String sql = "SELECT id, name FROM cabinets ORDER BY id";

        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Cabinet> cabinets = new ArrayList<>();

            while (resultSet.next()) {
                cabinets.add(mapCabinet(resultSet));
            }

            return cabinets;
        } catch (SQLException exception) {
            throw buildException("Impossible de charger les cabinets.", exception);
        }
    }

    /**
     * Cherche un cabinet par son nom.
     */
    public Cabinet findByName(String name) {
        String sql = "SELECT id, name FROM cabinets WHERE name = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCabinet(resultSet);
            }
        } catch (SQLException exception) {
            throw buildException("Impossible de chercher le cabinet.", exception);
        }
    }

    /**
     * Convertit une ligne SQL en objet Cabinet.
     */
    private Cabinet mapCabinet(ResultSet resultSet) throws SQLException {
        Cabinet cabinet = new Cabinet();
        cabinet.setId(resultSet.getInt("id"));
        cabinet.setName(resultSet.getString("name"));
        return cabinet;
    }
}
