package com.example.clientsellingmedicine.activity.productAndPayment;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.example.clientsellingmedicine.DTO.Category;
import com.example.clientsellingmedicine.DTO.Unit;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.DTO.ProductRequest;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.api.CategoryAPI;
import com.example.clientsellingmedicine.api.ProductAPI;
import com.example.clientsellingmedicine.api.UnitAPI;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtName, edtPrice, edtQuantity, edtDiscount,
            edtDescription, edtImageUrl;
    private Button btnSave;
    private Integer editingCategoryId = null;
    private Integer editingUnitId = null;

    MaterialAutoCompleteTextView edtCategoryId, edtUnitId;
    List<Category> categoryList = new ArrayList<>();
    List<Unit> unitList = new ArrayList<>();
    Integer selectedCategoryId = null;
    Integer selectedUnitId = null;

    private boolean isEditMode = false;
    private int editingProductId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initViews();

        loadCategories();
        loadUnits();

        // Kiểm tra có phải edit không
        if (getIntent() != null && getIntent().hasExtra("product_id")) {
            isEditMode = true;
            editingProductId = getIntent().getIntExtra("product_id", -1);

            // Fill sẵn dữ liệu text
            edtName.setText(getIntent().getStringExtra("product_name"));
            edtPrice.setText(String.valueOf(getIntent().getIntExtra("product_price", 0)));
            edtQuantity.setText(String.valueOf(getIntent().getIntExtra("product_quantity", 0)));
            edtDiscount.setText(String.valueOf(getIntent().getIntExtra("product_discount", 0)));
            // 🔹 Convert HTML -> text thường
            String descHtml = getIntent().getStringExtra("product_description");
            if (descHtml != null) {
                String plainText = HtmlCompat.fromHtml(
                        descHtml,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString().trim();
                edtDescription.setText(plainText);
            }

            edtImageUrl.setText(getIntent().getStringExtra("product_image"));

            // 👉 Lưu lại ID để lát nữa dropdown tự chọn đúng
            editingCategoryId = getIntent().getIntExtra("product_category_id", 1);
            editingUnitId = getIntent().getIntExtra("product_unit_id", 1);

            setTitle("Sửa sản phẩm");
        } else {
            setTitle("Thêm sản phẩm");
        }


        btnSave.setOnClickListener(v -> submitForm());
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtDescription = findViewById(R.id.edtDescription);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        edtCategoryId = findViewById(R.id.edtCategoryId);
        edtUnitId = findViewById(R.id.edtUnitId);
        btnSave = findViewById(R.id.btnSave);
    }

    private void submitForm() {
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String quantityStr = edtQuantity.getText().toString().trim();
        String discountStr = edtDiscount.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();
        String cateStr = edtCategoryId.getText().toString().trim();
        String unitStr = edtUnitId.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Tên, giá và số lượng là bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceStr);
        int quantity = Integer.parseInt(quantityStr);
        int discount = TextUtils.isEmpty(discountStr) ? 0 : Integer.parseInt(discountStr);

// Lấy id danh mục từ dropdown
        int cateId;
        if (selectedCategoryId != null) {
            cateId = selectedCategoryId;
        } else if (isEditMode && editingCategoryId != null) {
            // trường hợp sửa mà chưa chọn lại dropdown
            cateId = editingCategoryId;
        } else {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

// Lấy id đơn vị từ dropdown
        int unitId;
        if (selectedUnitId != null) {
            unitId = selectedUnitId;
        } else if (isEditMode && editingUnitId != null) {
            unitId = editingUnitId;
        } else {
            Toast.makeText(this, "Vui lòng chọn đơn vị", Toast.LENGTH_SHORT).show();
            return;
        }


        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setPrice(price);
        req.setQuantity(quantity);
        req.setDiscountPercent(discount);
        req.setDescription(desc);
        req.setImage(imageUrl);
        req.setId_category(cateId);
        req.setId_unit(unitId);
        req.setStatus(1);

        ProductAPI api = ServiceBuilder.buildService(ProductAPI.class);

        if (isEditMode && editingProductId != -1) {
            // Gọi PATCH
            api.updateProduct(editingProductId, req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddProductActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AddProductActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Gọi POST
            api.createProduct(req).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddProductActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Lỗi thêm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Toast.makeText(AddProductActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadCategories() {
        CategoryAPI api = ServiceBuilder.buildService(CategoryAPI.class);
        api.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());

                    List<String> names = new ArrayList<>();
                    for (Category c : categoryList) {
                        names.add(c.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddProductActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names
                    );
                    edtCategoryId.setAdapter(adapter);

                    edtCategoryId.setOnClickListener(v -> {
                        if (!categoryList.isEmpty()) {
                            edtCategoryId.showDropDown();
                        }
                    });

                    edtCategoryId.setOnItemClickListener((parent, view, position, id) -> {
                        selectedCategoryId = categoryList.get(position).getId();
                    });

                    // Nếu đang ở chế độ edit, chọn sẵn đúng category
                    if (isEditMode && editingCategoryId != null) {
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).getId().equals(editingCategoryId)) {
                                edtCategoryId.setText(categoryList.get(i).getName(), false);
                                selectedCategoryId = editingCategoryId;
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddProductActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUnits() {
        UnitAPI api = ServiceBuilder.buildService(UnitAPI.class);
        api.getUnits().enqueue(new Callback<List<Unit>>() {
            @Override
            public void onResponse(Call<List<Unit>> call, Response<List<Unit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    unitList.clear();
                    unitList.addAll(response.body());

                    List<String> names = new ArrayList<>();
                    for (Unit u : unitList) {
                        names.add(u.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddProductActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names
                    );
                    edtUnitId.setAdapter(adapter);

                    edtUnitId.setOnClickListener(v -> {
                        if (!unitList.isEmpty()) {
                            edtUnitId.showDropDown();
                        }
                    });

                    edtUnitId.setOnItemClickListener((parent, view, position, id) -> {
                        selectedUnitId = unitList.get(position).getId();
                    });

                    if (isEditMode && editingUnitId != null) {
                        for (int i = 0; i < unitList.size(); i++) {
                            if (unitList.get(i).getId().equals(editingUnitId)) {
                                edtUnitId.setText(unitList.get(i).getName(), false);
                                selectedUnitId = editingUnitId;
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Unit>> call, Throwable t) { }
        });
    }

}
