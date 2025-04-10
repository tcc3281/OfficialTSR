package com.example.officialtsr.api;

import com.example.officialtsr.models.TrafficSign;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TrafficSignApiService {

    @GET("traffic-signs")
    Call<List<TrafficSign>> getTrafficSigns();

    @GET("test")
    Call<String> getTestString();
}
