package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.Adapter.couponAdapter;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.interfaces.IOnButtonExchangeCouponClickListener;
import com.example.clientsellingmedicine.DTO.CouponDTO;
import com.example.clientsellingmedicine.DTO.RedeemedCouponDTO;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.api.CouponAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExchangeFragment extends Fragment implements IOnButtonExchangeCouponClickListener {
    private Context mContext;

    private couponAdapter couponAdapter;
    private TextView tvPoints;
    private RecyclerView rcvAccoumlatePointsItem;
    private LinearLayout ll_Empty;
    private Button btnHistory;
    private UserDTO user;
    public ExchangeFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accumulate_points_screen, container, false);
        mContext = view.getContext();
        addControl(view);
        addEvents();
        return view;
    }



    private void addControl(View view) {
        rcvAccoumlatePointsItem = view.findViewById(R.id.rcvAccoumlatePointsItem);
        tvPoints = view.findViewById(R.id.tvPoints);
        ll_Empty = view.findViewById(R.id.ll_Empty);
        btnHistory = view.findViewById(R.id.btnHistory);
    }
    private void addEvents() {
        loadData();
        // Go to history screen
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RewardPointsHistoryActvity.class);
            intent.putExtra("points", user.getPoint());
            startActivity(intent);
        });
    }

    private void loadData(){
        getPoints();
        getCoupons();
    }

    private void getPoints(){
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> request = userAPI.getUser();

        request.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if(response.isSuccessful()){
                    user = response.body();
                    // Set point
                    tvPoints.setText(String.valueOf(user.getPoint()));
                } else if(response.code() == 401) {
                    navigateToLogin();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve points", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve points", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void getCoupons(){
        CouponAPI couponAPI = ServiceBuilder.buildService(CouponAPI.class);
        Call<List<CouponDTO>> request = couponAPI.getCoupons();

        request.enqueue(new Callback<List<CouponDTO>>() {
            @Override
            public void onResponse(Call<List<CouponDTO>> call, Response<List<CouponDTO>> response) {
                if(response.isSuccessful()){
                    if(response.body().size()>0){
                        couponAdapter = new couponAdapter(response.body(), ExchangeFragment.this);
                        rcvAccoumlatePointsItem.setAdapter(couponAdapter);
                        rcvAccoumlatePointsItem.setLayoutManager(new LinearLayoutManager(mContext));
                    }else {
                        ll_Empty.setVisibility(View.VISIBLE);
                    }

                } else if(response.code() == 401) {
                    navigateToLogin();
                } else {
                    Toast.makeText(mContext, "Something was wrong", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<CouponDTO>> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onButtonExchangeCouponItemClick(CouponDTO coupon) {
        Integer point = user.getPoint();
        if(point < coupon.getPoint()){
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
            builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                    .setTitle("Thông Báo")
                    .setMessage("Điểm của bạn không đủ, vui lòng kiểm tra lại!")
                    .setCancelable(false) // Bấm ra ngoài không mất dialog
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Xử lý khi nhấn nút OK

                    })
                    .show();
        }else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
            builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                    .setTitle("Xác nhận")
                    .setMessage("Bạn muốn quy đổi mã giảm giá này chứ!")
                    .setCancelable(false) // Bấm ra ngoài không mất dialog
                    .setPositiveButton("OK", (dialog, which) -> {
                        exchangeCoupon(coupon);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Xử lý khi nhấn nút Cancel
                    })
                    .show();

        }

    }

    public void exchangeCoupon(CouponDTO coupon){
        CouponAPI couponAPI = ServiceBuilder.buildService(CouponAPI.class);
        Call<RedeemedCouponDTO> request = couponAPI.exchangeCoupon(coupon);

        request.enqueue(new Callback<RedeemedCouponDTO>() {
            @Override
            public void onResponse(Call<RedeemedCouponDTO> call, Response<RedeemedCouponDTO> response) {
                if(response.isSuccessful()){
                    getPoints();
                    Toast.makeText(mContext, "Quy đổi mã giảm giá thành công!", Toast.LENGTH_LONG).show();
                } else if(response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Quy đổi mã giảm giá thất bại", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RedeemedCouponDTO> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to exchange coupon", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
