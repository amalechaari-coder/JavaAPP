package com.queueweb.MDP;

/**
 * Modele simple d'un patient.
 */
public class Patient {
    private int id;
    private String name;
    private String password;

    /**
     * Retourne l'identifiant du patient.
     */
    public int getId() {
        return id;
    }

    /**
     * Definit l'identifiant du patient.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne le nom du patient.
     */
    public String getName() {
        return name;
    }

    /**
     * Definit le nom du patient.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne le mot de passe saisi par le patient.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Definit le mot de passe du patient.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
