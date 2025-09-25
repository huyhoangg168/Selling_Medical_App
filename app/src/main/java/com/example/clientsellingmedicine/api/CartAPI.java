package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.CartItemDTO;
import com.example.clientsellingmedicine.models.CartItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CartAPI {
    @GET("/api/cart")
    Call<List<CartItemDTO>> getCart();

    @GET("/api/cart/distinct-product-count")
    Call<Integer> getTotalItem();

    @POST("/api/cart")
    Call<CartItem> addCartItem(@Body CartItem cartItem);

    @DELETE("/api/cart/{id}")
    Call<CartItemDTO> deleteCartItem(@Path("id") Integer cartItemId);

//    @PUT("/api/cart")
//    Call<CartItem> updateCartItem(@Body CartItem cartItem);

    @PATCH("/api/cart")
    Call<CartItem> updateCartItem(@Body CartItem cartItem);


}
