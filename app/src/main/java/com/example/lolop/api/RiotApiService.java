package com.example.lolop.api;

import com.example.lolop.model.ChampionListResponse;
import com.example.lolop.model.ItemResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RiotApiService {
    @GET("cdn/{version}/data/{language}/champion.json")
    Call<ChampionListResponse> getChampions(@Path("version") String version, @Path("language") String language);

    @GET("cdn/{version}/data/{language}/champion/{championId}.json")
    Call<ChampionListResponse> getChampionDetail(@Path("version") String version, @Path("language") String language, @Path("championId") String championId);

    @GET("cdn/{version}/data/{language}/item.json")
    Call<ItemResponse> getItems(@Path("version") String version, @Path("language") String language);

    @GET("api/versions.json")
    Call<List<String>> getVersions();
}