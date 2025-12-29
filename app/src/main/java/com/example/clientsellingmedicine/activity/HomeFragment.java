package com.example.clientsellingmedicine.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;


import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;


import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.clientsellingmedicine.Adapter.productAdapter;
import com.example.clientsellingmedicine.Adapter.feedAdapter;
import com.example.clientsellingmedicine.DTO.Notification;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.activity.notificationAndNews.HealthyNewsDetailActivity;
import com.example.clientsellingmedicine.activity.notificationAndNews.NotificationActivity;
import com.example.clientsellingmedicine.activity.productAndPayment.CartActivity;
import com.example.clientsellingmedicine.activity.productAndPayment.DetailProductActivity;
import com.example.clientsellingmedicine.activity.productAndPayment.PaymentActivity;
import com.example.clientsellingmedicine.activity.productAndPayment.ProductActivity;
import com.example.clientsellingmedicine.interfaces.IOnButtonAddToCartClickListener;
import com.example.clientsellingmedicine.interfaces.IOnFeedItemClickListener;
import com.example.clientsellingmedicine.interfaces.IOnProductItemClickListener;
import com.example.clientsellingmedicine.DTO.CartItemDTO;
import com.example.clientsellingmedicine.DTO.Feed;
import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.models.CartItem;
import com.example.clientsellingmedicine.api.CartAPI;
import com.example.clientsellingmedicine.api.NotificationAPI;
import com.example.clientsellingmedicine.api.ProductAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.Convert;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment implements IOnProductItemClickListener, IOnFeedItemClickListener, IOnButtonAddToCartClickListener {
    private Context mContext;

    IOnProductItemClickListener listener;
    IOnFeedItemClickListener feedListener;
    productAdapter productAdapter;


    feedAdapter feedAdapter;
    TextView tv_DisplayAllTopSale,tv_DisplayAllTopDiscount,tvNumberCart,tv_DisplayAllNewProduct,tvNumberNotification;
    RecyclerView rcvTopProductSelling, rcvTopProductsDiscount,rcvFeeds,rcvTopNewProduct;
    ImageView ivCart,ivNotification,iv_medicine,iv_health_care,iv_personal_care,iv_convenient_product,iv_functional_food,iv_mom_baby,iv_beauty_care,iv_medical_equipment;

    TextInputEditText searchText;

    FrameLayout redCircleCart, redCircleNotificate;

    ImageSlider imageSlider;
    private SearchView searchView;
    private String lastQuery;

    private boolean isReloadData = false;

    public HomeFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen, container, false);
        mContext = view.getContext();
        // toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // Tắt title mặc định của Toolbar
        }
        setHasOptionsMenu(true);

        addControl(view);
        addEvents();

        return view;


    }


    private void addControl(View view) {
        tv_DisplayAllTopSale = view.findViewById(R.id.tv_DisplayAllTopSale);
        rcvTopProductSelling = view.findViewById(R.id.rcvTopProductSelling);
        tv_DisplayAllTopDiscount = view.findViewById(R.id.tv_DisplayAllTopDiscount);
        tv_DisplayAllNewProduct = view.findViewById(R.id.tv_DisplayAllNewProduct);
        rcvTopProductsDiscount =view.findViewById(R.id.rcvTopProductDiscount);
        rcvFeeds = view.findViewById(R.id.rcvFeeds);
        rcvTopNewProduct = view.findViewById(R.id.rcvTopNewProduct);
        searchText = view.findViewById(R.id.search_text);
        ivCart = view.findViewById(R.id.ivCart);
        ivNotification = view.findViewById(R.id.ivNotification);
        tvNumberCart = view.findViewById(R.id.tvNumberCart);
        tvNumberNotification = view.findViewById(R.id.tvNumberNotification);
        redCircleCart = view.findViewById(R.id.redCircleCart);
        redCircleNotificate = view.findViewById(R.id.redCircleNotificate);
        imageSlider = view.findViewById(R.id.image_slider);
        iv_medicine = view.findViewById(R.id.iv_medicine);
        iv_health_care = view.findViewById(R.id.iv_health_care);
        iv_personal_care = view.findViewById(R.id.iv_personal_care);
        iv_convenient_product = view.findViewById(R.id.iv_convenient_product);
        iv_functional_food = view.findViewById(R.id.iv_functional_food);
        iv_mom_baby = view.findViewById(R.id.iv_mom_baby);
        iv_beauty_care = view.findViewById(R.id.iv_beauty_care);
        iv_medical_equipment = view.findViewById(R.id.iv_medical_equipment);
    }
    private void addEvents(){
        rcvFeeds.addItemDecoration(new DividerItemDecoration(HomeFragment.this.getContext(), DividerItemDecoration.VERTICAL));
        View.OnClickListener onClickListener = v -> {
            Intent intent = new Intent(getActivity(), ProductActivity.class);
            int categoryID = -1;

            if (v.getId() == R.id.iv_medicine) {
                categoryID = 1; 
            } else if (v.getId() == R.id.iv_health_care) {
                categoryID = 2;
            } else if (v.getId() == R.id.iv_personal_care) {
                categoryID = 3;
            } else if (v.getId() == R.id.iv_convenient_product) {
                categoryID = 4;
            } else if (v.getId() == R.id.iv_functional_food) {
                categoryID = 5;
            } else if (v.getId() == R.id.iv_mom_baby) {
                categoryID = 6;
            } else if (v.getId() == R.id.iv_beauty_care) {
                categoryID = 7;
            } else if (v.getId() == R.id.iv_medical_equipment) {
                categoryID = 8;
            }
            if (categoryID != -1) {
                intent.putExtra("categoryID", categoryID);
                startActivity(intent);
            }
        };
        iv_medicine.setOnClickListener(onClickListener);
        iv_health_care.setOnClickListener(onClickListener);
        iv_personal_care.setOnClickListener(onClickListener);
        iv_convenient_product.setOnClickListener(onClickListener);
        iv_functional_food.setOnClickListener(onClickListener);
        iv_mom_baby.setOnClickListener(onClickListener);
        iv_beauty_care.setOnClickListener(onClickListener);
        iv_medical_equipment.setOnClickListener(onClickListener);

        tv_DisplayAllTopSale.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProductActivity.class);
            intent.putExtra("productTypes", "top_sale");
            startActivity(intent);
        });

        tv_DisplayAllTopDiscount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProductActivity.class);
            intent.putExtra("productTypes", "top_discount");
            startActivity(intent);
        });

        tv_DisplayAllNewProduct.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProductActivity.class);
            intent.putExtra("productTypes", "new_product");
            startActivity(intent);
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL){

                    performSearch(searchText.getText().toString());

                    // Clear text and hide keyboard
                    searchText.setText("");
                    hideKeyboard(mContext,v);
                    return true;
                }
                return false;
            }
        });

        ivNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        ivCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CartActivity.class);
                startActivity(intent);
            }
        });

        // load data when fragment is created
        loadData();
    }

    public void loadData() {
        // Don't need user login
        getTopProductsSelling();
        getTopProductsDiscount();
        getTopNewProducts();
        showSlider();
        getFeeds();

        //Sometimes it may fail because the user is not logged in
        getCountCartItems();
        getCountNotifications();
        saveFirebaseDeviceToken();
    }

    //Hàm lấy dữ liệu danh mục bán chạy
    private void getTopProductsSelling(){
        ProductAPI productAPI = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> request = productAPI.getBestSellerProducts();

        request.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if(response.isSuccessful()) {
                    productAdapter = new productAdapter(response.body(), HomeFragment.this, HomeFragment.this);

                    rcvTopProductSelling.setAdapter(productAdapter);
                    LinearLayoutManager layoutManager
                            = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                    rcvTopProductSelling.setLayoutManager(layoutManager);

                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Somethings was wrong!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Hàm lấy danh mục sản phẩm mới
    private void getTopNewProducts(){
        ProductAPI productAPI = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> request = productAPI.getNewProducts();

        request.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if(response.isSuccessful()) {
                    productAdapter = new productAdapter(response.body(), HomeFragment.this, HomeFragment.this);

                    rcvTopNewProduct.setAdapter(productAdapter);
                    LinearLayoutManager layoutManager
                            = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                    rcvTopNewProduct.setLayoutManager(layoutManager);

                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Somethings was wrong!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Hàm lấy danh mục ưu đãi giảm giá
    private void getCountCartItems(){
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
                    displayCartItemCount(0);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
            }
        });
    }

    //Hàm đếm notify
    private void getCountNotifications(){
        NotificationAPI notificationAPI = ServiceBuilder.buildService(NotificationAPI.class);
        Call<List<Notification>> request = notificationAPI.getNotification();
        request.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful()) {
                    List<Notification> notificationList = response.body().stream().collect(Collectors.toList());
                    if (notificationList != null && notificationList.size() > 0) {

                        // get all notification has seen in Share Prefs
                        List<Notification> listNotificationsHaveSeen = getNotificationsFromSharePrefs();
                        if(listNotificationsHaveSeen.size() > 0){
                            //remove notification have seen in Share Prefs if not in data response
                            listNotificationsHaveSeen.removeIf(notifHaveSeen -> notificationList.stream()
                                    .noneMatch(notification -> notification.getId() == notifHaveSeen.getId()));
                            //display notification not seen
                            int notificationNotSeenCount = notificationList.size() - listNotificationsHaveSeen.size();   // notification not seen = all - have seen
                            displayNotificationCount(notificationNotSeenCount);
                        }else {
                            displayNotificationCount(notificationList.size());
                        }
                    } else {
                        displayNotificationCount(0);
                    }
                } else if (response.code() == 401) {
                    displayNotificationCount(0);
                }
            }
            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {

            }
        });
    }

    private List<Notification> getNotificationsFromSharePrefs() {
        Type notificatetionType = new TypeToken<List<Notification>>() {
        }.getType();
        List<Notification> listNotificationsHaveSeen = EncryptedSharedPrefManager.loadNotifications(mContext, notificatetionType);
        if (listNotificationsHaveSeen != null && listNotificationsHaveSeen.size() > 0)
            return listNotificationsHaveSeen;
        return new ArrayList<>();
    }

    private void getTopProductsDiscount(){
        ProductAPI productAPI = ServiceBuilder.buildService(ProductAPI.class);
        Call<List<Product>> request = productAPI.getBestPromotionProducts();

        request.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if(response.isSuccessful()){
                    productAdapter productDiscountAdapter = new productAdapter(response.body(), HomeFragment.this,HomeFragment.this);
                    rcvTopProductsDiscount.setAdapter(productDiscountAdapter);
                    LinearLayoutManager layoutManager
                            = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                    rcvTopProductsDiscount.setLayoutManager(layoutManager);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Somethings was wrong!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Lưu token của device để firebase gửi thông báo
    private void saveFirebaseDeviceToken(){
        // Lấy token từ EncryptedSharedPreferences (tự động giải mã)
        Token deviceToken = EncryptedSharedPrefManager.loadFirebaseToken(mContext);

        // Kiểm tra token có tồn tại trước khi gửi lên server
        if (deviceToken != null && deviceToken.getToken() != null && !deviceToken.getToken().isEmpty()) {
            NotificationAPI notificationAPI = ServiceBuilder.buildService(NotificationAPI.class);
            Call<Void> request = notificationAPI.saveDevice(deviceToken);

            request.enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("FCM", "Save firebase device token successfully ! ");
                    }
                    else {
                        Log.d("FCM", "Save firebase device token failed ! ");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("FCM", "Save firebase device token failed ! ");
                }
            });
        } else {
            Log.d("FCM", "Firebase device token not found or empty");
        }
    }   }

    private void getFeeds(){
        new FetchFeedTask().execute((Void) null);
    }
    private void performSearch(String searchText) {
        Toast.makeText(mContext, searchText, Toast.LENGTH_LONG).show();

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

    private void displayNotificationCount(int num) {
        if(num <= 0){
            redCircleNotificate.setVisibility(GONE);
        }
        else if(num > 99){
            tvNumberNotification.setText("99");
            redCircleNotificate.setVisibility(VISIBLE);
        }
        else {
            tvNumberNotification.setText(String.valueOf(num));
            redCircleNotificate.setVisibility(VISIBLE);
        }
    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSlider(){
        ArrayList<SlideModel> imageList = new ArrayList<>();

        imageList.add(new SlideModel("https://data-service.pharmacity.io/pmc-ecm-webapp-config-api/production/banner/795%20x%20302%20(x2)%20(1)-1710388934651.png", ScaleTypes.FIT));
        imageList.add(new SlideModel("https://prod-cdn.pharmacity.io/e-com/images/ecommerce/20240308081457-0-795%20x%20302%20%28x2%29.png", ScaleTypes.FIT));
        imageList.add(new SlideModel("https://prod-cdn.pharmacity.io/e-com/images/ecommerce/20240305075612-0-PMCE_795x302(x2).png", ScaleTypes.FIT));
        imageList.add(new SlideModel("https://prod-cdn.pharmacity.io/e-com/images/ecommerce/20240311085012-0-795x302%28x2%29%20%281%29.png", ScaleTypes.FIT));
        imageList.add(new SlideModel("https://data-service.pharmacity.io/pmc-ecm-webapp-config-api/production/banner/795%20x%20302%20(x2)%20(1)-1710388934651.png", ScaleTypes.FIT));
        // imageList.add(SlideModel("String Url" or R.drawable, "title") You can add title
         imageList.add(new SlideModel("https://prod-cdn.pharmacity.io/e-com/images/ecommerce/20240308084056-0-dealhot_dealhot_1590x604.jpg", ScaleTypes.FIT));

        imageSlider.setImageList(imageList,ScaleTypes.FIT);

    }


    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(getActivity(), DetailProductActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", (Serializable) product);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public void onItemClick(Feed feed) {
        Intent intent = new Intent(getActivity(), HealthyNewsDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("url",  feed.getLink());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onButtonAddToCartClick(Product product) {
        showAddToCartDialog(product);
    }


    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {
        private String urlLink = "https://vnexpress.net/rss/suc-khoe.rss";

        private List<Feed> listNews;
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(urlLink).get();
                listNews = parseFeed(doc);
                return true;
            } catch (IOException e) {
                Log.e("TAG", "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Fill RecyclerView
                feedAdapter = new feedAdapter(listNews, HomeFragment.this);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                rcvFeeds.setAdapter(feedAdapter);
                rcvFeeds.setLayoutManager(linearLayoutManager);

            } else {

            }
        }
    }


    public static List<Feed> parseFeed(Document doc) {
        List<Feed> items = new ArrayList<>();
        Elements itemElements = doc.select("item");
        int count = Math.min(itemElements.size(), 5); // Giới hạn chỉ lấy 10 mục đầu tiên
        for (int i = 0; i < count; i++) {
            Element itemElement = itemElements.get(i);
            String title = itemElement.selectFirst("title").text();

            String description = itemElement.selectFirst("description").text();
            Document docc = Jsoup.parse(description);
            String extractedText = docc.text().trim();

            Element linkElement = itemElement.selectFirst("link");
            String link = linkElement.text();

            String pubDate = itemElement.selectFirst("pubDate").text();

            Element imgElement = itemElement.selectFirst("enclosure[url]");
            String img = "";
            if (imgElement != null) {
                img = imgElement.attr("url");
            }

            Feed feed = new Feed(title, link, extractedText, pubDate, img);
            items.add(feed);
        }

        return items;
    }


    private void showAddToCartDialog(Product product) {

        final AtomicInteger quantity = new AtomicInteger(1);
        final Dialog dialog = new Dialog(HomeFragment.this.getContext());


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
                            getCountCartItems();

                            //get CartItems Checked from SharedPreferences
                            List<CartItemDTO> listCartItemsChecked = getCartItemCheckedFromSharePrefs();

                            // update CartItems Checked to SharedPreferences
                            CartItemDTO cart = new CartItemDTO(product, quantity.get()); //cartItem save to SharedPreferences
                            listCartItemsChecked.add(cart);
                            EncryptedSharedPrefManager.saveCartItems(getContext(), listCartItemsChecked);

                            Toast.makeText(mContext, "Sản phẩm đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
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

    private List<CartItemDTO> getCartItemCheckedFromSharePrefs() {
        Type cartItemType = new TypeToken<List<CartItemDTO>>() {}.getType();
        List<CartItemDTO> listCartItemChecked = EncryptedSharedPrefManager.loadCartItems(mContext, cartItemType);
        return listCartItemChecked;
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

    @Override
    public void onResume() {
        super.onResume();
        if (isReloadData) {
            loadData();
            isReloadData = false;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        isReloadData = true;
    }
}
