package com.queueweb.MDP;

/**
 * Modele simple d'un cabinet.
 */
public class Cabinet {
    private int id;
    private String name;

    /**
     * Retourne l'identifiant du cabinet.
     */
    public int getId() {
        return id;
    }

    /**
     * Definit l'identifiant du cabinet.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne le nom du cabinet.
     */
    public String getName() {
        return name;
    }

    /**
     * Definit le nom du cabinet.
     */
    public void setName(String name) {
        this.name = name;
    }
}
