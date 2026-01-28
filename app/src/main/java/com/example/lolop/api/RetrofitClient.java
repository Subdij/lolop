package com.example.lolop.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://ddragon.leagueoflegends.com/";
    private static Retrofit retrofit = null;

    /**
     * Crée et retourne l'instance unique du service API Riot.
     * Configure Retrofit avec l'URL de base et le convertisseur GSON.
     */
    public static RiotApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RiotApiService.class);
    }
}