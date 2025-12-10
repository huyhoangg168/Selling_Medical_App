package com.example.clientsellingmedicine.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.DTO.OrderDTO;
import com.example.clientsellingmedicine.DTO.OrderDetailDTO;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.authAndAccount.AdminProductActivity;
import com.example.clientsellingmedicine.api.OrderAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminApproveOrderActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private AdminOrderAdapter adapter;
    private OrderAPI orderAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approve_order);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvOrders = findViewById(R.id.rvOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_orders);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(new ArrayList<>(), new AdminOrderAdapter.OnActionListener() {
            @Override
            public void onApprove(OrderDTO order) {
                updateStatus(order, 1);
            }

            @Override
            public void onReject(OrderDTO order) {
                updateStatus(order, 0);
            }
        });
        rvOrders.setAdapter(adapter);

        orderAPI = ServiceBuilder.buildService(OrderAPI.class);

        loadAdminOrders();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, AdminProductActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                //Đang ở màn này
                return true;
            } else if (id == R.id.nav_admin) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadAdminOrders() {
        orderAPI.getAdminOrders().enqueue(new Callback<List<OrderDTO>>() {
            @Override
            public void onResponse(Call<List<OrderDTO>> call, Response<List<OrderDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDTO> list = response.body();
                    if (list.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvOrders.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);
                        adapter.updateData(list);

                        // 🔹 Sau khi có danh sách, load thêm sản phẩm cho từng đơn
                        for (OrderDTO order : list) {
                            loadOrderItemsFor(order);
                        }
                    }
                } else {
                    // ...
                }
            }

            @Override
            public void onFailure(Call<List<OrderDTO>> call, Throwable t) {
                // ...
            }
        });
    }

    private void loadOrderItemsFor(OrderDTO order) {
        orderAPI.getOrderItem(order.getId()).enqueue(new Callback<List<OrderDetailDTO>>() {
            @Override
            public void onResponse(Call<List<OrderDetailDTO>> call, Response<List<OrderDetailDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderDetailDTO> items = response.body();
                    StringBuilder sb = new StringBuilder();

                    for (OrderDetailDTO item : items) {
                        // Tùy cấu trúc OrderDetailDTO của bạn:
                        // ví dụ: item.getQuantity(), item.getProduct().getName()
                        sb.append(item.getQuantity())
                                .append("x ")
                                .append(item.getProduct().getName())
                                .append(", ");
                    }
                    if (sb.length() > 2) {
                        sb.setLength(sb.length() - 2); // bỏ ", " cuối
                    }

                    order.setProductSummary(sb.toString());
                    adapter.notifyDataSetChanged(); // cập nhật lại list
                }
            }

            @Override
            public void onFailure(Call<List<OrderDetailDTO>> call, Throwable t) { }
        });
    }


    private void updateStatus(OrderDTO order, int newStatus) {
        Map<String, Integer> body = new HashMap<>();
        body.put("status", newStatus);

        orderAPI.updateOrderStatus(order.getCode(), body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminApproveOrderActivity.this,
                                    newStatus == 1 ? "Duyệt đơn thành công" : "Đã từ chối đơn",
                                    Toast.LENGTH_SHORT).show();
                            order.setStatus(newStatus);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminApproveOrderActivity.this,
                                    "Không cập nhật được trạng thái", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(AdminApproveOrderActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
