package com.example.officialtsr.api;

import com.example.officialtsr.models.TrafficSign;
import com.example.officialtsr.models.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface TrafficSignApiService {

    @GET("test")
    Call<String> getTestString();

    @POST("user")
    Call<Void> pushUser(@Body User user);

    @Multipart
    @POST("detect_and_classify_simple")
    Call<ResponseBody> detectAndClassifySimple(@Part MultipartBody.Part image);
}
