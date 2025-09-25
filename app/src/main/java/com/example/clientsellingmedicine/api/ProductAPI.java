package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProductAPI {
    @GET("/api/product")
    Call<List<Product>> getProducts();


    @GET("/api/product/filter")
    Call<List<Product>> getProductsFilter(
            @Query("keySearch") String keySearch,
            @Query("categoryId") int categoryId,
            @Query("minPrice") int minPrice,
            @Query("maxPrice") int maxPrice
    );

    @GET("/api/product/newest")
    Call<List<Product>> getNewProducts();

    @GET("/api/product/best-selling")
    Call<List<Product>> getBestSellerProducts();


    @GET("/api/product/top-discounted")
    Call<List<Product>> getBestPromotionProducts();

    @GET("/api/product/discounted")
    Call<List<Product>> getHavePromotionProducts();
}
