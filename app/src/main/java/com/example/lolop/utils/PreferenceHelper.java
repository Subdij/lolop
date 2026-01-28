package com.example.lolop.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static final String PREF_NAME = "lolop_prefs";
    private static final String KEY_CURRENT_VERSION = "current_version";
    private static final String KEY_LAST_UPDATE_CHECK = "last_update_check";

    private final SharedPreferences sharedPreferences;

    public PreferenceHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Sauvegarde la dernière version du jeu connue.
     */
    public void setLastKnownVersion(String version) {
        sharedPreferences.edit().putString(KEY_CURRENT_VERSION, version).apply();
    }

    /**
     * Récupère la dernière version du jeu connue, ou null si aucune.
     */
    public String getLastKnownVersion() {
        return sharedPreferences.getString(KEY_CURRENT_VERSION, null);
    }

    /**
     * Sauvegarde le timestamp de la dernière vérification de mise à jour.
     */
    public void setLastUpdateCheck(long timestamp) {
        sharedPreferences.edit().putLong(KEY_LAST_UPDATE_CHECK, timestamp).apply();
    }

    /**
     * Récupère le timestamp de la dernière vérification de mise à jour.
     */
    public long getLastUpdateCheck() {
        return sharedPreferences.getLong(KEY_LAST_UPDATE_CHECK, 0);
    }
}
