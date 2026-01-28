package com.example.lolop.model;

import java.util.Map;

public class ChampionListResponse {
    private String version;
    private Map<String, Champion> data;

    /** Retourne la version du jeu. */
    public String getVersion() {
        return version;
    }

    /**
     * Retourne la map contenant les données des champions (Clé: ID du champion).
     */
    public Map<String, Champion> getData() {
        return data;
    }
}