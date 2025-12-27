package com.example.clientsellingmedicine.DTO;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @SerializedName(value = "token", alternate = {"accessToken"})
    private String token;

    @SerializedName("refreshToken")
    private String refreshToken;
}
