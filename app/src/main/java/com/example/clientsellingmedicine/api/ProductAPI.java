package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.DTO.ProductRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    @POST("/api/product")
    Call<Product> createProduct(@Body ProductRequest product);

    @PATCH("/api/product/{id}")
    Call<Void> updateProduct(@Path("id") int id, @Body ProductRequest product);

    @DELETE("/api/product/{id}")
    Call<Void> deleteProduct(@Path("id") int id);

}
