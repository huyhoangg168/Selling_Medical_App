package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.DTO.UserRegister;
import com.google.gson.JsonObject;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface UserAPI {
    @GET("/api/user")
    Call<UserDTO> getUser();
    @PATCH("/api/user")
    Call<UserDTO> updateUser(@Body UserDTO user);
    @POST("/api/auth/signup")
    Call<ResponseDto> registerUser(@Body UserRegister userRegister);
    @POST("/api/user/check-phone-number")
    Call<Boolean> checkPhoneNumber(@Body JsonObject phoneNumber);
}
