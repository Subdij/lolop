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

    public static Context onAttach(Context context) {
        String lang = getPersistedLocaleFallback(context);
        return setLocale(context, lang, false);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang, false);
    }

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
    public static String getApiLanguage(Context context) {
        String lang = getLanguage(context);
        if (lang.equals("fr")) {
            return "fr_FR";
        } else {
            return "en_US";
        }
    }

    public static Context setLocale(Context context, String language) {
        return setLocale(context, language, true);
    }

    private static Context setLocale(Context context, String language, boolean persist) {
        if (persist) {
            persist(context, language);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences("lolop_prefs", Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences("lolop_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

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
