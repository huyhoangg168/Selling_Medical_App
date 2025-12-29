package com.example.clientsellingmedicine.activity.productAndPayment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.Adapter.productAdapter;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.interfaces.IOnButtonAddToCartClickListener;
import com.example.clientsellingmedicine.interfaces.IOnProductItemClickListener;
import com.example.clientsellingmedicine.DTO.CartItemDTO;
import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.models.CartItem;
import com.example.clientsellingmedicine.api.CartAPI;
import com.example.clientsellingmedicine.api.ProductAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.Convert;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends AppCompatActivity implements IOnProductItemClickListener, IOnButtonAddToCartClickListener {
    private Context mContext;
    private RecyclerView rcvProduct;
    private productAdapter productAdapter;

    private LinearLayout ll_Sort_low_to_high, ll_Sort_high_to_low;

    private FrameLayout redCircleCart;
    private ImageView imgFilter,ivBack,ivCart;

    private TextView tvNumberCart;
    private TextInputEditText edtSearch;

    private boolean isSortLowToHigh = false;
    private boolean isSortHighToLow = false;

    private Handler handler = new Handler();
    private Runnable searchRunnable;

    private String productTypes; // products of [top-discounted, newest, best-seller, discounted]
    // option filter
    private Integer categoryID = -1;
    private String keySearch;
    private Integer maxPrice = Integer.MAX_VALUE;
    private Integer minPrice = 0;

    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.product_screen);

        addControl();
        addEvents();

    }

    private void addControl() {
        rcvProduct = findViewById(R.id.rcvProduct);
        //loadingLayout = findViewById(R.id.loadingLayout);
        edtSearch = findViewById(R.id.search_text);
        imgFilter = findViewById(R.id.iv_filter);
        ivBack = findViewById(R.id.iv_back);
        ll_Sort_low_to_high = findViewById(R.id.ll_Sort_low_to_high);
        ll_Sort_high_to_low = findViewById(R.id.ll_Sort_high_to_low);
        tvNumberCart = findViewById(R.id.tvNumberCart);
        redCircleCart = findViewById(R.id.redCircleCart);
        ivCart = findViewById(R.id.ivCart);
    }

    private void addEvents() {

        // filter product by price
        imgFilter.setOnClickListener(v -> {
            showFilterPriceDialog();
        });

        // go to cart screen
        ivCart.setOnClickListener(view -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        //Back to home screen
        ivBack.setOnClickListener(view -> finish());

        // sort product by price low to high
        ll_Sort_low_to_high.setOnClickListener(v -> {
            if (isSortLowToHigh == false) {
                // sort high to low is selected
                if (isSortHighToLow == true) {
                    ll_Sort_high_to_low.setBackground(getResources().getDrawable(R.drawable.retangle_radius));
                    isSortHighToLow = false;
                }
                ll_Sort_low_to_high.setBackground(getResources().getDrawable(R.drawable.rectangle_radius_selected));
                productAdapter.sortProducts(Comparator.comparingDouble(Product::getPrice));
                productAdapter.notifyDataSetChanged();
                isSortLowToHigh = true;
            }
        });

        // sort product by price high to low
        ll_Sort_high_to_low.setOnClickListener(v -> {
            if (isSortHighToLow == false) {
                // sort low to high is selected
                if (isSortLowToHigh == true) {
                    ll_Sort_low_to_high.setBackground(getResources().getDrawable(R.drawable.retangle_radius));
                    isSortLowToHigh = false;
                }
                ll_Sort_high_to_low.setBackground(getResources().getDrawable(R.drawable.rectangle_radius_selected));
                productAdapter.sortProducts(Comparator.comparingDouble(Product::getPrice).reversed());
                productAdapter.notifyDataSetChanged();
                isSortHighToLow = true;
            }
        });

        // search product by name
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> performSearch(s.toString(), categoryID, minPrice, maxPrice);
                handler.postDelayed(searchRunnable, 500);

            }
        });


        // display data
        getTotalCartItem();
        initRecyclerview();
    }

    public void initRecyclerview() {
        // Get category ID and Product Types
        Intent intent = getIntent();
        if (intent.hasExtra("categoryID")) {
            categoryID = intent.getIntExtra("categoryID",-1) ;
        }
        if (intent.hasExtra("productTypes")) {
            productTypes = intent.getStringExtra("productTypes");
        }

        // get products by category ID or Product Types
        // by category ID
        if (categoryID != -1) {
            products = getProductsFiltered(keySearch, categoryID, minPrice, maxPrice);
            productAdapter = new productAdapter(products, this, this);
            rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));
            rcvProduct.setAdapter(productAdapter);
        }
        // by Product Types
        else {
             if (productTypes != null && productTypes.equals("top_sale")) {
                products = getProductsTopSale();
                productAdapter = new productAdapter(products, this, this);
                rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));
                rcvProduct.setAdapter(productAdapter);

            } else if (productTypes != null && productTypes.equals("top_discount")) {
                products = getProductsTopDiscount();
                productAdapter = new productAdapter(products, this, this);
                rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));
                rcvProduct.setAdapter(productAdapter);

            }
            else { // productTypes = Newest product
                products = getAllNewProducts();
                productAdapter = new productAdapter(products, this, this);
                rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));
                rcvProduct.setAdapter(productAdapter);

            }
        }



    }


    public List<Product> getProductsTopDiscount() {
        ProductAPI productAPI = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> call = productAPI.getHavePromotionProducts();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<com.example.clientsellingmedicine.DTO.Product>> future = executorService.submit((Callable<List<com.example.clientsellingmedicine.DTO.Product>>) () -> {
            try {
                Response<List<Product>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();

                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Product Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }

    public List<Product> getAllNewProducts() {
        ProductAPI productAPI = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> call = productAPI.getNewProducts();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<com.example.clientsellingmedicine.DTO.Product>> future = executorService.submit((Callable<List<com.example.clientsellingmedicine.DTO.Product>>) () -> {
            try {
                Response<List<Product>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();

                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Product Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }
    }
    public List<Product> getProductsTopSale() {
        ProductAPI productAPI = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> call = productAPI.getBestSellerProducts();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<com.example.clientsellingmedicine.DTO.Product>> future = executorService.submit((Callable<List<com.example.clientsellingmedicine.DTO.Product>>) () -> {
            try {
                Response<List<Product>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();

                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Product Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }
    }

    public List<Product> getProducts() {
        ProductAPI addressService = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> call = addressService.getProducts();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Product>> future = executorService.submit((Callable<List<Product>>) () -> {
            try {
                Response<List<Product>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();

                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Product Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }

    //Hàm lấy số lượng sp trong giỏ
    private void getTotalCartItem(){
        CartAPI cartAPI = ServiceBuilder.buildService(CartAPI.class);
        Call<Integer> request = cartAPI.getTotalItem();

        request.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.isSuccessful()){
                    displayCartItemCount(response.body());
                } else if(response.code() == 401) {
                    displayCartItemCount(0);
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void displayCartItemCount(int num) {
        if(num <= 0){
            redCircleCart.setVisibility(GONE);
        }
        else if(num > 99){
            tvNumberCart.setText("99");
            redCircleCart.setVisibility(VISIBLE);
        }
        else {
            tvNumberCart.setText(String.valueOf(num));
            redCircleCart.setVisibility(VISIBLE);
        }
    }

    public List<Product> getProductsFiltered(String keySearch, Integer categoryId, Integer minPrice, Integer maxPrice) {
        ProductAPI addressService = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> call = addressService.getProductsFilter(keySearch, categoryId, minPrice, maxPrice);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Product>> future = executorService.submit(() -> {
            try {
                Response<List<Product>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();

                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Product Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }

    //onClick item product in recyclerview
    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(this, DetailProductActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }


    private void performSearch(String searchText, Integer categoryId, Integer min, Integer max) {
        if(categoryId == -1){
            List<Product> filteredProducts =  products.stream()
                    .filter(product -> product.getPrice() >= min && product.getPrice() <= max)
                    .filter(product -> product.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
            productAdapter.setList(filteredProducts);
            return;
        }

        List<Product> filteredProducts = getProductsFiltered(searchText,categoryId,min,max);
        if (filteredProducts == null || filteredProducts.size() == 0) {
            Toast.makeText(mContext, "No result found", Toast.LENGTH_SHORT).show();
        } else {
            // sort before display data
            if(isSortHighToLow){
                sortProductsByPrice( filteredProducts, Comparator.comparingDouble(Product::getPrice).reversed());
            }
            else {
                sortProductsByPrice( filteredProducts, Comparator.comparingDouble(Product::getPrice));
            }
            //display data
            productAdapter.setList(filteredProducts);
        }
    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onButtonAddToCartClick(Product product) {
        showAddToCartDialog(product);
    }


    private void showAddToCartDialog(Product product) {

        final AtomicInteger quantity = new AtomicInteger(1);
        final Dialog dialog = new Dialog(this);


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_add_to_cart);

        // add control
        TextView tv_product_name = dialog.findViewById(R.id.tv_product_name);
        TextView tv_product_price = dialog.findViewById(R.id.tv_product_price);
        TextView tv_quantity = dialog.findViewById(R.id.tv_quantity);
        ImageView iv_product = dialog.findViewById(R.id.iv_product);
        LinearLayout ll_minus = dialog.findViewById(R.id.ll_minus);
        LinearLayout ll_plus = dialog.findViewById(R.id.ll_plus);
        ImageView iv_back = dialog.findViewById(R.id.iv_back);
        Button btn_AddToCart = dialog.findViewById(R.id.btn_AddToCart);
        Button btn_BuyNow = dialog.findViewById(R.id.btn_BuyNow);

        // add event
        iv_back.setOnClickListener(v -> dialog.dismiss());
        tv_product_name.setText(product.getName());
        tv_product_price.setText(Convert.convertPrice(product.getPrice()));
        Glide.with(dialog.getContext())
                .load(product.getImage())
                .placeholder(R.drawable.loading_icon) // Hình ảnh thay thế khi đang tải
                .error(R.drawable.error_image) // Hình ảnh thay thế khi có lỗi
                .into(iv_product);

        // set quantity minus
        ll_minus.setOnClickListener(v -> {
            if (quantity.get() > 1) {
                quantity.decrementAndGet();
                tv_quantity.setText(String.valueOf(quantity.get()));
            }
        });

        // set quantity plus
        ll_plus.setOnClickListener(v -> {
            quantity.incrementAndGet();
            tv_quantity.setText(String.valueOf(quantity.get()));
        });

        btn_AddToCart.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(0, product.getId(), quantity.get());
            addToCart(cartItem)
                    .thenAccept(result -> {
                        if (result == 201) {
                            // reset total cart
                            getTotalCartItem();

                            //get CartItems Checked from SharedPreferences
                            List<CartItemDTO> listCartItemsChecked = getCartItemCheckedFromSharePrefs();

                            // update CartItems Checked to SharedPreferences
                            CartItemDTO cart = new CartItemDTO(product, quantity.get());
                            listCartItemsChecked.add(cart);
                            EncryptedSharedPrefManager.saveCartItems(this, listCartItemsChecked);

                            Toast.makeText(mContext, "Sản phẩm đã được thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
                        }
                        else if(result == 401){
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else {
                            Toast.makeText(mContext, "Thêm vào giỏ hàng thất bại", Toast.LENGTH_LONG).show();
                        }
                    })
                    .exceptionally(ex -> {
                        Log.e("Error", "Failed to add item to cart: " + ex.getMessage());
                        return null;
                    });
            dialog.dismiss();
        });


        // show dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // Thêm xử lý cho nút Mua ngay
        btn_BuyNow.setOnClickListener(v -> {
            // Tạo CartItemDTO cho sản phẩm vừa chọn
            CartItemDTO cartItemDTO = new CartItemDTO(product, quantity.get());
            ArrayList<CartItemDTO> productsToBuy = new ArrayList<>();
            productsToBuy.add(cartItemDTO);

            // Tính toán giá trị cần thiết
            int price = product.getPrice() * quantity.get();
            int productDiscount = 0;
            int voucherDiscount = 0;
            int totalAmount = price - productDiscount - voucherDiscount;

            Intent intent = new Intent(mContext, PaymentActivity.class);
            intent.putExtra("products", productsToBuy);
            intent.putExtra("totalPrice", com.example.clientsellingmedicine.utils.Convert.convertPrice(price));
            intent.putExtra("totalProductDiscount", com.example.clientsellingmedicine.utils.Convert.convertPrice(productDiscount));
            intent.putExtra("totalVoucherDiscount", com.example.clientsellingmedicine.utils.Convert.convertPrice(voucherDiscount));
            intent.putExtra("totalAmount", com.example.clientsellingmedicine.utils.Convert.convertPrice(totalAmount));
            // Các giá trị voucher/coupon khác nếu cần
            dialog.dismiss();
            mContext.startActivity(intent);
        });

        // show dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    private void showFilterPriceDialog() {

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_filter_price);

        // add control
        LinearLayout ll_price1 = dialog.findViewById(R.id.ll_price1);
        LinearLayout ll_price2 = dialog.findViewById(R.id.ll_price2);
        LinearLayout ll_price3 = dialog.findViewById(R.id.ll_price3);
        LinearLayout ll_price4 = dialog.findViewById(R.id.ll_price4);
        EditText et_min_price = dialog.findViewById(R.id.et_min_price);
        EditText et_max_price = dialog.findViewById(R.id.et_max_price);
        ImageView iv_close = dialog.findViewById(R.id.iv_close);
        Button btn_NonSelect = dialog.findViewById(R.id.btn_NonSelect);
        Button btn_Apply = dialog.findViewById(R.id.btn_Apply);

        // add event
        iv_close.setOnClickListener(v -> dialog.dismiss());


        //if filter price is selected
        if (minPrice == 0 && maxPrice == 200000) {
            ll_price1.setBackground(getResources().getDrawable(R.drawable.bg_type_address));
            et_min_price.setText("0.000đ");
            et_max_price.setText("200.000đ");
        } else if (minPrice == 200000 && maxPrice== 1000000) {
            ll_price2.setBackground(getResources().getDrawable(R.drawable.bg_type_address));
            et_min_price.setText("200.000đ");
            et_max_price.setText("1.000.000đ");
        } else if (minPrice == 1000000 && maxPrice == 3000000) {
            ll_price3.setBackground(getResources().getDrawable(R.drawable.bg_type_address));
            et_min_price.setText("1.000.000đ");
            et_max_price.setText("3.000.000đ");
        } else if (minPrice == 3000000 && maxPrice == Integer.MAX_VALUE) {
            ll_price4.setBackground(getResources().getDrawable(R.drawable.bg_type_address));
            et_min_price.setText("3.000.000đ");
        }


        // action select price
        View.OnClickListener onClickListener = v -> {
            ll_price1.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            ll_price2.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            ll_price3.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            ll_price4.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            v.setBackground(getResources().getDrawable(R.drawable.bg_type_address));
            if (v.getId() == R.id.ll_price1) {
                et_min_price.setText("0.000đ");
                et_max_price.setText("200.000đ");
            } else if (v.getId() == R.id.ll_price2) {
                et_min_price.setText("200.000đ");
                et_max_price.setText("1.000.000đ");
            } else if (v.getId() == R.id.ll_price3) {
                et_min_price.setText("1.000.000đ");
                et_max_price.setText("3.000.000đ");
            } else if (v.getId() == R.id.ll_price4) {
                et_min_price.setText("3.000.000đ");
                et_max_price.setText("");
            }
        };
        ll_price1.setOnClickListener(onClickListener);
        ll_price2.setOnClickListener(onClickListener);
        ll_price3.setOnClickListener(onClickListener);
        ll_price4.setOnClickListener(onClickListener);

        // remove filter price
        btn_NonSelect.setOnClickListener(v -> {
            ll_price1.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            ll_price2.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            ll_price3.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            ll_price4.setBackground(getResources().getDrawable(R.drawable.order_selection_background));
            et_min_price.setText("");
            et_max_price.setText("");
            minPrice = 0;
            maxPrice = Integer.MAX_VALUE;
            // search product after filter
            performSearch(edtSearch.getText().toString(), categoryID, minPrice, maxPrice);
            // dismiss dialog
            dialog.dismiss();
        });

        btn_Apply.setOnClickListener(v -> {
            String textMinPrice =  et_min_price.getText().toString();
            String textMaxPrice =  et_max_price.getText().toString();

            minPrice = !textMinPrice.isEmpty() ? Convert.convertCurrencyFormat(et_min_price.getText().toString()) : 0;
            maxPrice = !textMaxPrice.isEmpty() ? Convert.convertCurrencyFormat(et_max_price.getText().toString()) : Integer.MAX_VALUE;

            // search product after filter
            performSearch(edtSearch.getText().toString(), categoryID, minPrice, maxPrice);
            // dismiss dialog
            dialog.dismiss();
        });

        // show dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    public void sortProductsByPrice(List<Product> list, Comparator<Product> comparator) {
        Collections.sort(list, comparator);
    }


    private CompletableFuture<Integer> addToCart(CartItem cartItem) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        CartAPI cartAPI = ServiceBuilder.buildService(CartAPI.class);
        Call<CartItem> request = cartAPI.addCartItem(cartItem);

        request.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful()) {
                    int result = response.code();
                    future.complete(result);
                }
                else if(response.code() == 401){
                    int result = response.code();
                    future.complete(result);
                }else {
                    future.completeExceptionally(new Exception("Failed to add item to cart"));
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                if (t instanceof IOException) {
                    future.completeExceptionally(new Exception("A connection error occurred"));
                } else {
                    future.completeExceptionally(new Exception("Failed to add item to cart"));
                }
            }
        });

        return future;
    }


    private List<CartItemDTO> getCartItemCheckedFromSharePrefs() {
        Type cartItemType = new TypeToken<List<CartItemDTO>>() {}.getType();
        List<CartItemDTO> listCartItemChecked = EncryptedSharedPrefManager.loadCartItems(mContext, cartItemType);
        return listCartItemChecked;
    }
}