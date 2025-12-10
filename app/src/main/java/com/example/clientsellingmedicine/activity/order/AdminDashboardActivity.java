package com.example.clientsellingmedicine.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.DTO.AdminOrderStatistics;
import com.example.clientsellingmedicine.DTO.AdminStatisticsResponse;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.authAndAccount.AdminProductActivity;
import com.example.clientsellingmedicine.api.OrderAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvTotalOrders,
            tvSuccessOrders, tvFailedOrders, tvPendingOrders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupBottomNav();
        loadStatistics();

    }

    private void initViews() {
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvSuccessOrders = findViewById(R.id.tvSuccessOrders);
        tvFailedOrders = findViewById(R.id.tvFailedOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_admin); // id tab Dashboard trong menu_bottom_nav

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, AdminProductActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, AdminApproveOrderActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin) {
                return true; // đang ở dashboard
            }
            return false;
        });
    }

    private void loadStatistics() {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<AdminStatisticsResponse> call = orderAPI.getAdminStatistics();

        call.enqueue(new Callback<AdminStatisticsResponse>() {
            @Override
            public void onResponse(Call<AdminStatisticsResponse> call,
                                   Response<AdminStatisticsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminStatisticsResponse body = response.body();

                    // Optional: check status == "success"
                    if ("success".equalsIgnoreCase(body.getStatus())
                            && body.getData() != null) {
                        bindData(body.getData());
                    } else {
                        Toast.makeText(AdminDashboardActivity.this,
                                "Không lấy được thống kê", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Không lấy được thống kê", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminStatisticsResponse> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void bindData(AdminOrderStatistics stats) {
        // Tổng số đơn
        tvTotalOrders.setText(String.valueOf(stats.getTotalOrders()));

        // Định dạng tiền: 15.750.000đ
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        String moneyStr = nf.format(stats.getTotalRevenue()) + "đ";
        tvTotalRevenue.setText(moneyStr);

        tvSuccessOrders.setText("Thành công: " + stats.getSuccessCount());
        tvPendingOrders.setText("Đang xử lý: " + stats.getPendingCount());
        tvFailedOrders.setText("Thất bại / Hủy: " + stats.getFailedCount());
    }
}
