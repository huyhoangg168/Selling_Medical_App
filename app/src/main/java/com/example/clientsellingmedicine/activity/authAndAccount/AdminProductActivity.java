package com.example.clientsellingmedicine.activity.authAndAccount;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.order.AdminApproveOrderActivity;
import com.example.clientsellingmedicine.activity.order.AdminDashboardActivity;
import com.example.clientsellingmedicine.activity.productAndPayment.AddProductActivity;
import com.example.clientsellingmedicine.api.LogoutAPI;
import com.example.clientsellingmedicine.api.ProductAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductActivity extends AppCompatActivity {


    RecyclerView rvProducts;
    FloatingActionButton fabAdd;
    List<Product> productList = new ArrayList<>();
    ProductAdminAdapter adapter;
    Button btnLogout;
    private List<Product> originalList = new ArrayList<>(); // giữ bản gốc để sort/filter
    private Spinner spSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);


        rvProducts = findViewById(R.id.rvProducts);
        fabAdd = findViewById(R.id.fabAdd);
        btnLogout =  findViewById(R.id.btnLogout);
        spSort = findViewById(R.id.spSort);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_inventory);

        adapter = new ProductAdminAdapter(this, productList);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);

        setupSortSpinner();
        loadProducts();


        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddProductActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder =
                    new MaterialAlertDialogBuilder(AdminProductActivity.this);

            builder.setIcon(R.drawable.drug)
                    .setTitle("Xác Nhận Đăng Xuất")
                    .setMessage("Bạn có muốn đăng xuất khỏi ứng dụng không?")
                    .setCancelable(false)

                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        Logout();
                    })

                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inventory) {
                // Đang ở màn này rồi, không làm gì
                return true;
            } else if (id == R.id.nav_orders) {
                // TODO: mở màn Duyệt đơn hàng
                startActivity(new Intent(this, AdminApproveOrderActivity.class));
                overridePendingTransition(0, 0);
                finish();
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

    private void setupSortSpinner() {
        String[] options = new String[]{
                "Mặc định",
                "Giá: thấp → cao",
                "Giá: cao → thấp",
                "Giảm giá: nhiều → ít",
                "Giảm giá: ít → nhiều",
                "Hết hàng (số lượng = 0)",
                "Còn hàng (> 0)"
        };

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);

        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySortFilter(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }


    private void loadProducts() {
        ProductAPI api = ServiceBuilder.buildService(ProductAPI.class);
        api.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if(response.isSuccessful()){
                    originalList.clear();
                    originalList.addAll(response.body());

                    productList.clear();
                    productList.addAll(originalList);
                    adapter.notifyDataSetChanged();

                    // áp lại rule sort hiện tại (nếu user đã chọn spinner trước đó)
                    applySortFilter(spSort.getSelectedItemPosition());
                }
            }


            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
            }
        });
    }

    private void applySortFilter(int position) {
        productList.clear();

        // Bắt đầu từ originalList mỗi lần để filter/sort không bị chồng chéo
        List<Product> temp = new ArrayList<>(originalList);

        switch (position) {
            case 0: // Mặc định: giữ nguyên thứ tự API trả về
                productList.addAll(temp);
                break;

            case 1: // Giá thấp → cao
                Collections.sort(temp, Comparator.comparing(Product::getPrice, Comparator.nullsLast(Integer::compareTo)));
                productList.addAll(temp);
                break;

            case 2: // Giá cao → thấp
                Collections.sort(temp, Comparator.comparing(Product::getPrice, Comparator.nullsLast(Integer::compareTo)).reversed());
                productList.addAll(temp);
                break;

            case 3: // Giảm giá nhiều → ít
                Collections.sort(temp, Comparator.comparing(Product::getDiscountPercent, Comparator.nullsLast(Integer::compareTo)).reversed());
                productList.addAll(temp);
                break;

            case 4: // Giảm giá ít → nhiều
                Collections.sort(temp, Comparator.comparing(Product::getDiscountPercent, Comparator.nullsLast(Integer::compareTo)));
                productList.addAll(temp);
                break;

            case 5: // Hết hàng (số lượng = 0)
                for (Product p : temp) {
                    if (p.getQuantity() != null && p.getQuantity() == 0) {
                        productList.add(p);
                    }
                }
                break;

            case 6: // Còn hàng (> 0)
                for (Product p : temp) {
                    if (p.getQuantity() != null && p.getQuantity() > 0) {
                        productList.add(p);
                    }
                }
                break;
        }

        adapter.notifyDataSetChanged();
    }

    public void Logout() {
        LogoutAPI logoutAPI = ServiceBuilder.buildService(LogoutAPI.class);
        Call<ResponseDto> request = logoutAPI.logout();

        request.enqueue(new Callback<ResponseDto>() {

            @Override
            public void onResponse(Call<ResponseDto> call, Response<ResponseDto> response) {
                if (response.isSuccessful()) {
                    // remove token
                    SharedPref.removeData(AdminProductActivity.this,
                            Constants.TOKEN_PREFS_NAME,
                            Constants.KEY_TOKEN);

                    // remove checkbox product
                    SharedPref.removeData(AdminProductActivity.this,
                            Constants.CART_PREFS_NAME,
                            Constants.KEY_CART_ITEMS_CHECKED);

                    // return to login screen
                    Intent intent = new Intent(AdminProductActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else if (response.code() == 401) {
                    Intent intent = new Intent(AdminProductActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(AdminProductActivity.this,
                            "Failed to retrieve items (response)",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDto> call, Throwable t) {
                Toast.makeText(AdminProductActivity.this,
                        "Failed to logout (network)",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // mỗi lần quay lại màn admin thì load lại list
        loadProducts();
    }
}