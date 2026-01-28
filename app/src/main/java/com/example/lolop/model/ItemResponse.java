package com.example.lolop.model;

import java.util.Map;

public class ItemResponse {
    private String version;
    private Map<String, Item> data;

    /** Retourne la version du jeu. */
    public String getVersion() {
        return version;
    }

    /** Retourne la map contenant les données des objets (Clé: ID de l'objet). */
    public Map<String, Item> getData() {
        return data;
    }
}