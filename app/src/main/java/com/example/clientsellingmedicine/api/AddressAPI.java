package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.AddressDto;
import com.example.clientsellingmedicine.DTO.District;
import com.example.clientsellingmedicine.DTO.Province;
import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.DTO.Ward;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AddressAPI {
    @GET("/api/address")
    Call<List<AddressDto>> getAddress();

    @POST("/api/address")
    Call<AddressDto> addAddress(@Body AddressDto addressDto);

    @PATCH("/api/address")
    Call<ResponseDto> updateAddress(@Body AddressDto addressDto);

    @DELETE("/api/address/{id}")
    Call<ResponseDto> deleteAddress(@Path("id") Integer addressId);

    @GET("/api/address/province")
    Call<List<Province>> getProvinces();

    @GET("/api/address/district/{id}")
    Call<List<District>> getDistricts(@Path("id") Integer provinceId);

    @GET("/api/address/ward/{id}")
    Call<List<Ward>> getWards(@Path("id") Integer districtId);
}
