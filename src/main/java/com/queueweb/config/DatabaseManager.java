package com.queueweb.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * Gere la creation de la base et l'ouverture des connexions JDBC.
 */
public final class DatabaseManager {

    private DatabaseManager() {
    }

    /**
     * Initialise la base et les tables de l'application.
     */
    public static void initializeDatabase(List<String> cabinetNames) {
        ensureDriverIsAvailable();
        createDatabaseIfNeeded();
        createTablesIfNeeded();
        seedCabinets(cabinetNames);
    }

    /**
     * Ouvre une connexion vers la base principale du projet.
     */
    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.DATABASE_URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
    }

    /**
     * Verifie qu'un driver JDBC MySQL ou MariaDB existe dans WEB-INF/lib.
     */
    private static void ensureDriverIsAvailable() {
        List<String> driverClasses = Arrays.asList(
                "com.mysql.cj.jdbc.Driver",
                "org.mariadb.jdbc.Driver"
        );

        for (String driverClass : driverClasses) {
            try {
                Class.forName(driverClass);
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new RuntimeException(
                "Aucun driver JDBC MySQL/MariaDB n'a ete trouve. Ajoute le jar dans WEB-INF/lib."
        );
    }

    /**
     * Cree la base si elle n'existe pas encore.
     */
    private static void createDatabaseIfNeeded() {
        String sql = "CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.DATABASE_NAME;

        try (Connection connection = DriverManager.getConnection(
                DatabaseConfig.SERVER_URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Impossible de creer ou d'ouvrir la base MySQL. Verifie XAMPP et le port MySQL.",
                    exception
            );
        }
    }

    /**
     * Cree les tables utilisees par l'application.
     */
    private static void createTablesIfNeeded() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS patients (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL UNIQUE, " +
                            "password VARCHAR(100) NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS cabinets (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL UNIQUE" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tickets (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "ticket_number INT NOT NULL, " +
                            "patient_name VARCHAR(100) NOT NULL, " +
                            "patient_id INT NULL, " +
                            "cabinet_id INT NOT NULL, " +
                            "status VARCHAR(20) NOT NULL, " +
                            "ticket_date DATE NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL, " +
                            "FOREIGN KEY (cabinet_id) REFERENCES cabinets(id) ON DELETE CASCADE" +
                            ")"
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Impossible de creer les tables MySQL.", exception);
        }
    }

    /**
     * Insere les cabinets par defaut si besoin.
     */
    private static void seedCabinets(List<String> cabinetNames) {
        String sql = "INSERT IGNORE INTO cabinets (name) VALUES (?)";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String cabinetName : cabinetNames) {
                statement.setString(1, cabinetName);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Impossible d'initialiser les cabinets.", exception);
        }
    }
}
