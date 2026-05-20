package com.queueweb.DAO;

import com.queueweb.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Classe mere de tous les DAO.
 * Elle centralise une seule connexion JDBC et l'initialisation de la base.
 */
public abstract class AbstractDAO {
    private static Connection sharedConnection;
    private static boolean initialized;
    private static boolean driverLoaded;

    /**
     * Retourne la connexion partagee par les classes DAO.
     */
    protected synchronized Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }

        if (sharedConnection == null || sharedConnection.isClosed()) {
            sharedConnection = DriverManager.getConnection(
                    DatabaseConfig.DATABASE_URL,
                    DatabaseConfig.USER,
                    DatabaseConfig.PASSWORD
            );
        }

        return sharedConnection;
    }

    /**
     * Charge le driver JDBC une seule fois.
     */
    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(
                    "Le driver MySQL n'est pas present. Verifie la dependance dans pom.xml.",
                    exception
            );
        }
    }

    /**
     * Initialise la base, les tables et les cabinets par defaut.
     */
    public static synchronized void initializeDatabase(List<String> cabinetNames) {
        if (initialized) {
            return;
        }

        loadDriver();
        createDatabaseIfNeeded();
        createTablesIfNeeded();
        seedCabinets(cabinetNames);
        initialized = true;
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
                    "Impossible de creer la base. Verifie XAMPP, MySQL et le port configure.",
                    exception
            );
        }
    }

    /**
     * Cree les tables de l'application.
     */
    private static void createTablesIfNeeded() {
        try (Connection connection = DriverManager.getConnection(
                DatabaseConfig.DATABASE_URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
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
            throw new RuntimeException("Impossible de creer les tables.", exception);
        }
    }

    /**
     * Ajoute les cabinets par defaut si besoin.
     */
    private static void seedCabinets(List<String> cabinetNames) {
        String sql = "INSERT IGNORE INTO cabinets (name) VALUES (?)";

        try (Connection connection = DriverManager.getConnection(
                DatabaseConfig.DATABASE_URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String cabinetName : cabinetNames) {
                statement.setString(1, cabinetName);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Impossible d'initialiser les cabinets.", exception);
        }
    }

    /**
     * Ferme la connexion partagee a la fin d'un test console.
     */
    public static synchronized void closeSharedConnection() {
        if (sharedConnection == null) {
            return;
        }

        try {
            sharedConnection.close();
        } catch (SQLException ignored) {
        } finally {
            sharedConnection = null;
        }
    }

    /**
     * Construit une erreur claire pour un projet academique.
     */
    protected RuntimeException buildException(String message, SQLException exception) {
        return new RuntimeException(message, exception);
    }
}
