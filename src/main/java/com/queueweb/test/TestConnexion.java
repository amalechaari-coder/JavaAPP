package com.queueweb.test;

import com.queueweb.DAO.AbstractDAO;
import com.queueweb.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;

/**
 * Petit test console pour verifier la connexion JDBC.
 */
public class TestConnexion {

    /**
     * Lance un test simple de connexion MySQL.
     */
    public static void main(String[] args) {
        AbstractDAO.initializeDatabase(Arrays.asList(
                "Cabinet General",
                "Cabinet Dentaire",
                "Cabinet Pediatrique"
        ));

        try (Connection connection = DriverManager.getConnection(
                DatabaseConfig.DATABASE_URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        )) {
            System.out.println("Connexion JDBC reussie vers " + DatabaseConfig.DATABASE_NAME + ".");
        } catch (Exception exception) {
            System.out.println("Connexion JDBC echouee: " + exception.getMessage());
        } finally {
            AbstractDAO.closeSharedConnection();
        }
    }
}
