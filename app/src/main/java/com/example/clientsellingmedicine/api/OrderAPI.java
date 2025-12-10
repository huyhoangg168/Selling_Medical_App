package com.example.clientsellingmedicine.api;

import com.example.clientsellingmedicine.DTO.AdminOrderStatistics;
import com.example.clientsellingmedicine.DTO.AdminStatisticsResponse;
import com.example.clientsellingmedicine.DTO.MomoResponse;
import com.example.clientsellingmedicine.DTO.OrderDTO;
import com.example.clientsellingmedicine.DTO.OrderDetailDTO;
import com.example.clientsellingmedicine.DTO.OrderWithDetails;
import com.example.clientsellingmedicine.DTO.ZalopayResponse;
import com.example.clientsellingmedicine.models.MoMoOrderInfo;
import com.example.clientsellingmedicine.models.Order;

import java.util.List;
import java.util.Map;


import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderAPI {
    @GET("/api/order")
    Call<List<OrderDTO>> getOrders();

    @GET("/api/order/{code}")
    Call<OrderDTO>getOrderByCode(@Path("code")String code);

    @GET("/api/order-detail/{id}")
    Call<List<OrderDetailDTO>> getOrderItem(@Path("id")int id);

    @POST("/api/order/momo")
    Call<MomoResponse> newOrderWithMoMo(@Body OrderWithDetails order);

    @POST("/api/order/momo/info")
    Call<MoMoOrderInfo> saveMoMoOrderInfo(@Body MoMoOrderInfo order);

    @GET("/api/order/momo/check-order-status")
    Call<ResponseBody> checkMoMoOrderStatus(
            @Query("partnerCode") String partnerCode,
            @Query("requestId") String requestId,
            @Query("orderId") String orderId
    );

    @POST("/api/order/zalopay")
    Call<ZalopayResponse> newOrderWithZalopay(@Body OrderWithDetails order);

    @GET("/api/order/zalopay/{app_trans_id}")
    Call<ResponseBody> checkZalopayOrderStatus(@Path("app_trans_id")String app_trans_id);

    @POST("/api/order/cod")
    Call<Order> newOrderWithCOD(@Body OrderWithDetails order);

    //Thêm MỚI cho admin:
    // 1. Lấy danh sách đơn cho admin (gợi ý: chỉ trả về pending ở BE)
    @GET("/api/order/admin")
    Call<List<OrderDTO>> getAdminOrders();

    // 2. Cập nhật trạng thái đơn (0 = từ chối, 1 = thành công, 2 = chờ duyệt)
    @PATCH("/api/order/{code}/status")
    Call<ResponseBody> updateOrderStatus(
            @Path("code") String code,
            @Body Map<String, Integer> body
    );

    // 3. Thống kê cho admin (tổng đơn, doanh thu, thành công/thất bại/đang chờ)
    @GET("/api/order/admin/statistics")
    Call<AdminStatisticsResponse> getAdminStatistics();


}
