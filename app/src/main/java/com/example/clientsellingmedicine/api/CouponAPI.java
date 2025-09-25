package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.CouponDTO;
import com.example.clientsellingmedicine.DTO.RedeemedCouponDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CouponAPI {

    @GET("/api/redeemed-coupons")
    Call<List<RedeemedCouponDTO>> getRedeemedCoupons();

    @GET("/api/coupon")
    Call<List<CouponDTO>> getCoupons();

    @POST("/api/redeemed-coupons")
    Call<RedeemedCouponDTO> exchangeCoupon(@Body CouponDTO Coupon);
}
