package com.zipgo.zipgoassignment;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetPolyline {

    @GET("json")
    Call<JsonObject> getPolylineData(@Query("origin") String origin, @Query("destination") String destination, @Query("waypoints") String waypoints, @Query("key") String key);
}
