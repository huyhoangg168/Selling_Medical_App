package com.example.clientsellingmedicine.activity.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.OrderDTO;
import com.example.clientsellingmedicine.DTO.OrderDetailDTO;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.Adapter.orderDetailAdapter;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.api.OrderAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Convert;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private Context mContext;
    private TextView tv_userName, tv_Phone, tv_Address, tv_orderTime,
            tv_totalPrice, tv_orderCode, tv_paymentMethod, tv_totalPoint, tv_totalPayment,
            tv_couponCode, tv_totalDiscountProduct, tv_totalDiscountCoupon, tv_totalDiscount;
    private ImageView iv_back;
    private RecyclerView rcvOrderDetail;

    private orderDetailAdapter orderDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.order_detail);

        addControl();
        addEvents();

    }

    private void addEvents() {
        iv_back.setOnClickListener(v -> {
            finish();
        });

        Intent intent = getIntent();
        String code = intent.getStringExtra("code");
        getOrderByCode(code);
    }

    private void addControl() {
        iv_back = findViewById(R.id.iv_back);

        tv_userName = findViewById(R.id.tv_userName);
        tv_Phone = findViewById(R.id.tv_Phone);
        tv_Address = findViewById(R.id.tv_Address);
        tv_orderTime = findViewById(R.id.tv_orderTime);
        tv_totalPrice = findViewById(R.id.tv_totalPrice);
        tv_orderCode = findViewById(R.id.tv_orderCode);
        tv_paymentMethod = findViewById(R.id.tv_paymentMethod);
        tv_totalPoint = findViewById(R.id.tv_totalPoint);
        tv_totalPayment = findViewById(R.id.tv_totalPayment);
        tv_couponCode = findViewById(R.id.tv_couponCode);
        tv_totalDiscountProduct = findViewById(R.id.tv_totalDiscountProduct);
        tv_totalDiscountCoupon = findViewById(R.id.tv_totalDiscountCoupon);
        tv_totalDiscount = findViewById(R.id.tv_totalDiscount);

        rcvOrderDetail = findViewById(R.id.rcvOrderDetail);

    }

    //Load data chi tiết đơn hàng
    private void loadData(OrderDTO order) {

        if (order != null) {
            displayUserInfor(order.getUser());
            getOrderItems(order.getId()); // get order items and total price
            tv_Address.setText(order.getUserAddress());
            String orderTime = Convert.convertToDate(order.getOrderTime().toString());
            tv_orderTime.setText(orderTime);
            tv_orderCode.setText(order.getCode());
            tv_totalDiscountProduct.setText(Convert.convertPrice(order.getTotalProductDiscount()));
            tv_totalDiscountCoupon.setText(Convert.convertPrice(order.getTotalCouponDiscount()));
            tv_totalDiscount.setText(Convert.convertPrice(order.getTotalDiscount()));
            tv_couponCode.setText(order.getRedeemed_coupons() != null ? order.getRedeemed_coupons().getCode() : "");
            tv_paymentMethod.setText(order.getPaymentMethod());
            tv_totalPoint.setText("+" + order.getPoint().toString());
            tv_totalPayment.setText(Convert.convertPrice(order.getTotal()));
        }
    }

    public void displayUserInfor(UserDTO user) {
        if (user.getUsername() != null) {
            tv_userName.setText(user.getUsername());
        } else if (user.getPhone() != null) {
            tv_userName.setText(user.getPhone());
        } else {
            tv_userName.setText(user.getEmail());
        }

        tv_Phone.setText(user.getPhone());
    }


    //Load data của đơn hàng theo orderID
    public void getOrderItems(Integer orderId) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<List<OrderDetailDTO>> request = orderAPI.getOrderItem(orderId);
        request.enqueue(new Callback<List<OrderDetailDTO>>() {

            @Override
            public void onResponse(Call<List<OrderDetailDTO>> call, Response<List<OrderDetailDTO>> response) {
                if (response.isSuccessful()) {
                    List<OrderDetailDTO> orderItems = response.body();
                    orderDetailAdapter = new orderDetailAdapter(orderItems); //Hiển thị dataload đc lên adapter
                    rcvOrderDetail.setAdapter(orderDetailAdapter);
                    rcvOrderDetail.setLayoutManager(new LinearLayoutManager(mContext));

                    // calculate total price
                    Integer totalPrice = 0;
                    for (OrderDetailDTO orderItem : orderItems) {
                        totalPrice += orderItem.getQuantity() * orderItem.getProductPrice();
                    }
                    tv_totalPrice.setText(Convert.convertPrice(totalPrice)); // set total price

                } else {
                    Toast.makeText(mContext, "Something was wrong", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderDetailDTO>> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Log.d("TAG", "onFailure: " + t.getMessage());
                Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void getOrderByCode(String code) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<OrderDTO> request = orderAPI.getOrderByCode(code);

        request.enqueue(new Callback<OrderDTO>() {
            @Override
            public void onResponse(Call<OrderDTO> call, Response<OrderDTO> response) {
                if (response.isSuccessful()) {
                    loadData(response.body());
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderDTO> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }
}
