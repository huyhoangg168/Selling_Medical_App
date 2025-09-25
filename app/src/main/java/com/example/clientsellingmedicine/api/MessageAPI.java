package com.example.clientsellingmedicine.api;


import retrofit2.Call;
import retrofit2.http.GET;

public interface MessageAPI {
    @GET("messages")
    Call<String> getMessages();
}
