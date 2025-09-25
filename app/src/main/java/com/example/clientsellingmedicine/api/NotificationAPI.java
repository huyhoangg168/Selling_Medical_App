package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.Notification;
import com.example.clientsellingmedicine.DTO.Token;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface NotificationAPI {
    @GET("/api/notification")
    Call<List<Notification>> getNotification();

    @POST("/api/notification/device-token")
    Call<Void> saveDevice(@Body Token deviceToken);
}
