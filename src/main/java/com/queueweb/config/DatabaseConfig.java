package com.queueweb.config;

/**
 * Configuration simple de la base MySQL.
 * Les valeurs par defaut utilisent le port 3000 indique par l'utilisateur.
 */
public final class DatabaseConfig {
    public static final String HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    public static final String PORT = System.getenv().getOrDefault("DB_PORT", "3000");
    public static final String DATABASE_NAME = System.getenv().getOrDefault("DB_NAME", "queue_jee_app");
    public static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    public static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    public static final String SERVER_URL =
            "jdbc:mysql://" + HOST + ":" + PORT
                    + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Casablanca";

    public static final String DATABASE_URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Casablanca";

    private DatabaseConfig() {
    }
}
