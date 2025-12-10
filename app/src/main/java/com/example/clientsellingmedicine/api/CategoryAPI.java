package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryAPI {
    @GET("/api/category")
    Call<List<Category>> getCategories();
}
