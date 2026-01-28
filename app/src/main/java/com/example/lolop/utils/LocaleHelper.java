package com.example.lolop.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    /**
     * Attache le contexte avec la langue persistée au démarrage de l'activité/application.
     * Utilise la langue par défaut du système si aucune langue n'est définie.
     */
    public static Context onAttach(Context context) {
        String lang = getPersistedLocaleFallback(context);
        return setLocale(context, lang, false);
    }

    /**
     * Attache le contexte avec une langue par défaut spécifique.
     */
    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang, false);
    }

    /**
     * Récupère la langue actuellement sélectionnée.
     */
    public static String getLanguage(Context context) {
        return getPersistedLocaleFallback(context);
    }

    // Helper to get persisted or fallback
    private static String getPersistedLocaleFallback(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("lolop_prefs", Context.MODE_PRIVATE);
        if (preferences.contains(SELECTED_LANGUAGE)) {
            return preferences.getString(SELECTED_LANGUAGE, "en");
        }
        // Dynamic Fallback
        String deviceLang = Locale.getDefault().getLanguage();
        if ("fr".equals(deviceLang)) {
            return "fr";
        }
        return "en";
    }

    // Returns the API compliant language code (e.g., "fr_FR", "en_US")
    /**
     * Retourne le code de langue complet (ex: fr_FR, en_US) attendu par l'API Riot.
     */
    public static String getApiLanguage(Context context) {
        String lang = getLanguage(context);
        if (lang.equals("fr")) {
            return "fr_FR";
        } else {
            return "en_US";
        }
    }

    /**
     * Définit la langue de l'application et persiste le choix.
     */
    public static Context setLocale(Context context, String language) {
        return setLocale(context, language, true);
    }

    /**
     * Méthode interne pour changer la locale et mettre à jour la configuration des ressources.
     * Gère la compatibilité entre les versions d'Android.
     */
    private static Context setLocale(Context context, String language, boolean persist) {
        if (persist) {
            persist(context, language);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }

    /**
     * Récupère la langue sauvegardée dans les SharedPreferences.
     */
    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences("lolop_prefs", Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    /**
     * Sauvegarde la langue sélectionnée dans les SharedPreferences.
     */
    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences("lolop_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    /**
     * Met à jour la configuration de la locale pour Android N et supérieur.
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    /**
     * Met à jour la configuration de la locale pour les versions antérieures à Android N.
     */
    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}
