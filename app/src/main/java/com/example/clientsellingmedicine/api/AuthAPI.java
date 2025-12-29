package com.example.clientsellingmedicine.api;
import com.example.clientsellingmedicine.DTO.ExchangeKeyRequest;
import com.example.clientsellingmedicine.DTO.ExchangeKeyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthAPI {
    // Gửi Public Key -> Nhận về Encrypted AES Key
    @POST("api/auth/exchange-key")
    Call<ExchangeKeyResponse> exchangeKey(@Body ExchangeKeyRequest request);
}