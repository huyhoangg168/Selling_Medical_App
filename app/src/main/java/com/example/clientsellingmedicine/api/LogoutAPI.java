package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.ResponseDto;

import retrofit2.Call;
import retrofit2.http.POST;

public interface LogoutAPI {
    @POST("/api/auth/logout")
    Call<ResponseDto> logout();
}
