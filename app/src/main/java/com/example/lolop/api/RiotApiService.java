package com.example.lolop.api;

import com.example.lolop.model.ChampionListResponse;
import com.example.lolop.model.ItemResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RiotApiService {
    @GET("cdn/{version}/data/fr_FR/champion.json")
    Call<ChampionListResponse> getChampions(@Path("version") String version);

    @GET("cdn/{version}/data/fr_FR/champion/{championId}.json")
    Call<ChampionListResponse> getChampionDetail(@Path("version") String version, @Path("championId") String championId);

    @GET("cdn/{version}/data/fr_FR/item.json")
    Call<ItemResponse> getItems(@Path("version") String version);
}