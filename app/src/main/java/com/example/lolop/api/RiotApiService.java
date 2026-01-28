package com.example.lolop.api;

import com.example.lolop.model.ChampionListResponse;
import com.example.lolop.model.ItemResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RiotApiService {
    /**
     * Récupère la liste complète des champions pour une version et une langue
     * données.
     */
    @GET("cdn/{version}/data/{language}/champion.json")
    Call<ChampionListResponse> getChampions(@Path("version") String version, @Path("language") String language);

    /**
     * Récupère les détails complets d'un champion spécifique.
     */
    @GET("cdn/{version}/data/{language}/champion/{championId}.json")
    Call<ChampionListResponse> getChampionDetail(@Path("version") String version, @Path("language") String language,
            @Path("championId") String championId);

    /**
     * Récupère la liste complète des objets du jeu.
     */
    @GET("cdn/{version}/data/{language}/item.json")
    Call<ItemResponse> getItems(@Path("version") String version, @Path("language") String language);

    /**
     * Récupère la liste des versions (patchs) du jeu disponibles.
     */
    @GET("api/versions.json")
    Call<List<String>> getVersions();
}