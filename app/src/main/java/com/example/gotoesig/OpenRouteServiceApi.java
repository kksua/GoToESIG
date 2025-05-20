package com.example.gotoesig;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpenRouteServiceApi {

    @GET("geocode/search")
    Call<JsonObject> geocodeAddress(
            @Query("api_key") String apiKey,
            @Query("text") String address
    );

    @GET("v2/directions/driving-car")
    Call<JsonObject> getDirections(
            @Query("api_key") String apiKey,
            @Query("start") String start,
            @Query("end") String end
    );

    @POST("v2/matrix/driving-car")
    Call<JsonObject> getTimeDistanceMatrix(
            @Header("Authorization") String apiKey,
            @Body JsonObject payload
    );

    @POST("v2/directions/driving-car")
    @Headers("Content-Type: application/json")
    Call<RouteResponse> getRoute(
            @Header("Authorization") String apiKey,
            @Body RouteRequest request
    );
}
