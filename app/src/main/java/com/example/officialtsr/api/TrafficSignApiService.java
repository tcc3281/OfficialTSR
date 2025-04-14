package com.example.officialtsr.api;

import com.example.officialtsr.models.TrafficSign;
import com.example.officialtsr.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TrafficSignApiService {

    @GET("traffic-signs")
    Call<List<TrafficSign>> getTrafficSigns();

    @GET("test")
    Call<String> getTestString();

    @POST("user")
    Call<Void> pushUser(@Body User user);
}
