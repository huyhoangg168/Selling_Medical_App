package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.Unit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UnitAPI {
    @GET("/api/unit")
    Call<List<Unit>> getUnits();
}
