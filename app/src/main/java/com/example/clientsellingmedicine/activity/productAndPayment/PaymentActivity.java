package com.example.clientsellingmedicine.activity.productAndPayment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.Adapter.confirmOrderAdapter;

import com.example.clientsellingmedicine.Adapter.couponCheckboxAdapter;
import com.example.clientsellingmedicine.DTO.ZalopayResponse;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.adress.RegisteredAddressActivity;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.interfaces.IOnVoucherItemClickListener;
import com.example.clientsellingmedicine.DTO.AddressDto;
import com.example.clientsellingmedicine.DTO.CartItemDTO;
import com.example.clientsellingmedicine.DTO.RedeemedCouponDTO;
import com.example.clientsellingmedicine.DTO.MomoResponse;
import com.example.clientsellingmedicine.DTO.OrderWithDetails;
import com.example.clientsellingmedicine.models.Order;
import com.example.clientsellingmedicine.api.AddressAPI;
import com.example.clientsellingmedicine.api.CouponAPI;
import com.example.clientsellingmedicine.api.OrderAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.BiometricHelper;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.Convert;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements IOnVoucherItemClickListener {

    private Context mContext;
    private RecyclerView rcvOrderItem;
    private LinearLayout ll_selectVoucher, ll_addAddress, ll_address;
    private TextView tv_totalPrice, tv_productDiscount, tv_awardPoint, tv_voucherCode, tv_addVoucher, tv_address, tv_updateAddress,
            tv_voucherDiscount, tv_totalDiscount, tv_finalTotalPrice, tv_addProduct, tv_paymentMethod, edt_noteOrder;
    private Button btn_payment, btn_addAddress, btn_Apply;
    ;

    private TextInputEditText txt_input_code;
    private confirmOrderAdapter confirmOrderAdapter;

    private Boolean isCouponDialogShowing = false;
    private Boolean isAddressDialogShowing = false;

    private Integer positionVoucherItemSelected = -1;

    private Integer voucherDiscountPercent = 0;

    private RedeemedCouponDTO redeemedCoupon = new RedeemedCouponDTO();

    private List<CartItemDTO> products;


    private couponCheckboxAdapter couponCheckboxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.payment_screen);

        addControl();
        addEvents();

    }

    private void addControl() {
        //RecyclerView
        rcvOrderItem = findViewById(R.id.rcvOrderItem);

        // LinearLayout
        ll_selectVoucher = findViewById(R.id.ll_selectVoucher);
        ll_addAddress = findViewById(R.id.ll_addAddress);
        ll_address = findViewById(R.id.ll_address);

        //TextView
        tv_totalPrice = findViewById(R.id.tv_totalPrice);
        tv_productDiscount = findViewById(R.id.tv_productDiscount);
        tv_voucherDiscount = findViewById(R.id.tv_voucherDiscount);
        tv_totalDiscount = findViewById(R.id.tv_totalDiscount);
        tv_finalTotalPrice = findViewById(R.id.tv_finalTotalPrice);
        tv_awardPoint = findViewById(R.id.tv_awardPoint);
        tv_voucherCode = findViewById(R.id.tv_voucherCode);
        tv_addVoucher = findViewById(R.id.tv_addVoucher);
        tv_addProduct = findViewById(R.id.tv_addProduct);
        tv_paymentMethod = findViewById(R.id.tv_paymentMethod);
        edt_noteOrder = findViewById(R.id.edt_noteOrder);
        tv_address = findViewById(R.id.tv_address);
        tv_updateAddress = findViewById(R.id.tv_updateAddress);

        //Button
        btn_payment = findViewById(R.id.btn_payment);
        btn_addAddress = findViewById(R.id.btn_addAddress);
    }

    private void addEvents() {
        // add voucher
        ll_selectVoucher.setOnClickListener(v -> showSelectCouponDialog());

        // add payment method
        tv_paymentMethod.setOnClickListener(v -> showSelectPaymentMethodDialog());

        // add address
        btn_addAddress.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RegisteredAddressActivity.class);
            startActivity(intent);
        });

        // update address
        tv_updateAddress.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RegisteredAddressActivity.class);
            startActivity(intent);
        });

        // add more product
        tv_addProduct.setOnClickListener(v -> {
            finish();
        });

        // payment
        btn_payment.setOnClickListener(v -> {
            handlePayment();
        });

        getData();
    }

    //Xử lý thanh toán
    private void handlePayment() {
        // 1. Kiểm tra địa chỉ (Giữ nguyên)
        if (tv_address.getText().toString().isEmpty()) {
            showAlertDialog("Chưa có địa chỉ", "Vui lòng thêm địa chỉ trước khi thanh toán !");
            return;
        }

        // 2. Tạo đối tượng Order (Giữ nguyên logic cũ)
        OrderWithDetails orderWithDetails = new OrderWithDetails();
        Order order = new Order();
        order.setPaymentMethod(tv_paymentMethod.getText().toString());
        order.setNote(edt_noteOrder.getText().toString());
        order.setUserAddress(tv_address.getText().toString());
        // Parse cẩn thận để tránh lỗi NumberFormatException
        try {
            String pointStr = tv_awardPoint.getText().toString().replace("+", "").trim();
            order.setPoint(Integer.parseInt(pointStr));
        } catch (Exception e) {
            order.setPoint(0);
        }

        order.setTotalCouponDiscount(Convert.convertCurrencyFormat(tv_voucherDiscount.getText().toString()));
        order.setTotalProductDiscount(Convert.convertCurrencyFormat(tv_productDiscount.getText().toString()));
        order.setTotal(Convert.convertCurrencyFormat(tv_finalTotalPrice.getText().toString()));
        order.setRedeemedCouponId(redeemedCoupon != null ? redeemedCoupon.getId() : 0);

        orderWithDetails.setOrder(order);
        orderWithDetails.setListCartItem(products);

        // 3. LOGIC MỚI: KIỂM TRA VÂN TAY
        boolean isBiometricEnabled = SharedPref.getBoolean(mContext, Constants.BIOMETRIC_PREFS_NAME, Constants.KEY_BIOMETRIC_ENABLED, false);

        if (isBiometricEnabled) {
            // Nếu ĐANG BẬT vân tay -> Hiện bảng xác thực
            BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                    // Vân tay đúng -> Mới gọi API thanh toán
                    Toast.makeText(mContext, "Xác thực thành công, đang xử lý...", Toast.LENGTH_SHORT).show();
                    submitOrderToBackend(orderWithDetails);
                }

                @Override
                public void onFailure() {
                    // Vân tay sai hoặc bấm hủy -> Không làm gì cả (hoặc báo lỗi)
                    Toast.makeText(mContext, "Xác thực thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu KHÔNG BẬT vân tay -> Gọi API luôn như cũ
            submitOrderToBackend(orderWithDetails);
        }
    }

    // Hàm này chỉ chuyên làm nhiệm vụ gọi API (đã tách ra từ handlePayment cũ)
    private void submitOrderToBackend(OrderWithDetails orderWithDetails) {
        String method = orderWithDetails.getOrder().getPaymentMethod();

        if (method.equalsIgnoreCase("MOMO"))
            newOrderWithMoMo(orderWithDetails);
        else if (method.equalsIgnoreCase("COD"))
            newOrderWithCOD(orderWithDetails);
        else if (method.equalsIgnoreCase("ZALOPAY"))
            newOrderWithZalopay(orderWithDetails);
    }

    //Lấy dữ liệu mặt hàng trong giỏ
    private void getData() {
        // get data
        Intent intent = getIntent();
        products = (List<CartItemDTO>) intent.getSerializableExtra("products");
        String totalPrice = intent.getStringExtra("totalPrice");
        String totalAmount = intent.getStringExtra("totalAmount");
        String totalProductDiscount = intent.getStringExtra("totalProductDiscount");
        String totalVoucherDiscount = intent.getStringExtra("totalVoucherDiscount");
        positionVoucherItemSelected = intent.getIntExtra("positionVoucherItemSelected", -1);
        redeemedCoupon = (RedeemedCouponDTO) intent.getSerializableExtra("couponDetail");

        // set data
        tv_totalPrice.setText(totalPrice);
        tv_productDiscount.setText(totalProductDiscount);
        tv_voucherDiscount.setText(totalVoucherDiscount);
        Integer totalDiscount = Convert.convertCurrencyFormat(totalProductDiscount) + Convert.convertCurrencyFormat(totalVoucherDiscount);
        tv_totalDiscount.setText(Convert.convertPrice(totalDiscount));
        tv_finalTotalPrice.setText(totalAmount);
        Integer awardPoint = Convert.convertCurrencyFormat(totalPrice) / 1000;
        tv_awardPoint.setText("+" + awardPoint);

        confirmOrderAdapter = new confirmOrderAdapter(products);
        rcvOrderItem.setAdapter(confirmOrderAdapter);
        rcvOrderItem.setLayoutManager(new LinearLayoutManager(mContext));

        if (redeemedCoupon != null) {
            tv_voucherCode.setVisibility(TextView.VISIBLE);
            tv_addVoucher.setVisibility(TextView.GONE);
            tv_voucherCode.setText(redeemedCoupon.getCode());
        }

        getAddress();

    }

    public void newOrderWithMoMo(OrderWithDetails orderWithDetails) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<MomoResponse> request = orderAPI.newOrderWithMoMo(orderWithDetails);

        request.enqueue(new Callback<MomoResponse>() {
            @Override
            public void onResponse(Call<MomoResponse> call, Response<MomoResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("tag", "onResponse: "+response.body());
                    MomoResponse momoResponse = response.body();
                    try{
                        //open MoMo APP to payment
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(momoResponse.getDeeplink().toString()));
                        finish();
                        startActivity(intent);
                    }catch (Exception e){
                        //open MoMo WEB to payment
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(momoResponse.getPayUrl().toString()));
//                        finish();
                        startActivity(intent);
                    }

                    // clear cart items in Shared Preferences
                    EncryptedSharedPrefManager.clearCartItems(mContext);
                }else if (response.code() == 409) {
                    handleOutOfStock409(response);
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    finish();
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Something was wrong", Toast.LENGTH_LONG).show();
                }
        }

            @Override
            public void onFailure(Call<MomoResponse> call, Throwable t) {
                if (t instanceof IOException) {
                    Log.d("tag", "onResponse: " + t.getMessage() + t.getStackTrace());
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void newOrderWithZalopay(OrderWithDetails orderWithDetails) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<ZalopayResponse> request = orderAPI.newOrderWithZalopay(orderWithDetails);

        request.enqueue(new Callback<ZalopayResponse>() {
            @Override
            public void onResponse(Call<ZalopayResponse> call, Response<ZalopayResponse> response) {
                if (response.isSuccessful()) {
                    ZalopayResponse zalopayResponse = response.body();
                    try{
                        //open MoMo APP to payment
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zalopayResponse.getOrder_url().toString()));
                        finish();
                        startActivity(intent);
                    }catch (Exception e){

                    }

                    // clear cart items in Encrypted Shared Preferences
                    EncryptedSharedPrefManager.clearCartItems(mContext);
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    finish();
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Something was wrong", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ZalopayResponse> call, Throwable t) {
                if (t instanceof IOException) {
                    Log.d("tag", "onResponse: " + t.getMessage() + t.getStackTrace());
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    //COD payment
    public void newOrderWithCOD(OrderWithDetails orderWithDetails) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<Order> request = orderAPI.newOrderWithCOD(orderWithDetails);

        request.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Order order = response.body();
                    Intent intent = new Intent(mContext, MomoPaymentResultActivity.class);
                    intent.putExtra("Order", order);
                    intent.putExtra("statusCode", response.code());
                    finish();
                    startActivity(intent);

                    // clear cart items in Encrypted Shared Preferences
                    EncryptedSharedPrefManager.clearCartItems(mContext);
                }else if (response.code() == 409) {
                    handleOutOfStock409(response);
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    finish();
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Something was wrong", Toast.LENGTH_LONG).show();
            }

        }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void getAddress() {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<List<AddressDto>> request = addressAPI.getAddress();

        request.enqueue(new Callback<List<AddressDto>>() {
            @Override
            public void onResponse(Call<List<AddressDto>> call, Response<List<AddressDto>> response) {
                if (response.isSuccessful()) {
                    List<AddressDto> list = response.body();
                    if (list.size() > 0) {
                        ll_address.setVisibility(LinearLayout.VISIBLE);
                        ll_addAddress.setVisibility(LinearLayout.GONE);
                        for (AddressDto addressDto : list) {
                            if (addressDto.getIs_default()) {
                                // set address default
                                tv_address.setText(addressDto.getSpecific_address() + ", " + addressDto.getWard() + ", " + addressDto.getDistrict() + ", " + addressDto.getProvince());
                                break;
                            }

                        }
                    } else {
                        ll_address.setVisibility(LinearLayout.GONE);
                        ll_addAddress.setVisibility(LinearLayout.VISIBLE);
                    }

                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    finish();
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AddressDto>> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    //Hiển thị dialog mã gg
    private void showSelectCouponDialog() {
        if (isCouponDialogShowing) {
            return;
        }
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_select_coupon);

        // add control
        ImageView iv_close = dialog.findViewById(R.id.iv_close);
        TextInputLayout txt_input_code_layout = dialog.findViewById(R.id.txt_input_code_layout);
        txt_input_code = dialog.findViewById(R.id.txt_input_code);
        btn_Apply = dialog.findViewById(R.id.btn_Apply);
        RecyclerView rcv_coupon = dialog.findViewById(R.id.rcv_coupon);

        // add event
        iv_close.setOnClickListener(v -> dialog.dismiss()); // close dialog

        txt_input_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    btn_Apply.setEnabled(true);
                } else {
                    btn_Apply.setEnabled(false);
                }
            }
        });

        couponCheckboxAdapter = new couponCheckboxAdapter(getCoupons(), PaymentActivity.this, positionVoucherItemSelected);
        //couponCheckboxAdapter = new couponCheckboxAdapter(getCoupons(), PaymentActivity.this);
        rcv_coupon.setAdapter(couponCheckboxAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rcv_coupon.setLayoutManager(layoutManager);

        btn_Apply.setOnClickListener(v -> {
            redeemedCoupon = couponCheckboxAdapter.getCouponSelected();
            voucherDiscountPercent = redeemedCoupon.getCoupon().getDiscountPercent();  // get voucher discount percent
            positionVoucherItemSelected = couponCheckboxAdapter.getPositionVoucherSelected(); // get position of voucher selected
            handlerApplyCoupon(redeemedCoupon);

            dialog.dismiss();
        });

        dialog.setOnShowListener(dialog1 -> {
            isCouponDialogShowing = true;
        });
        dialog.setOnDismissListener(dialog1 -> {
            isCouponDialogShowing = false;
        });

        // show dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    private void showSelectPaymentMethodDialog() {

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_select_payment_mathod);

        // add control
        ImageView iv_back = dialog.findViewById(R.id.iv_back);
        ListView lv_payment_method = dialog.findViewById(R.id.lv_payment_method);

        // add event
        iv_back.setOnClickListener(v -> dialog.dismiss());

        List<String> paymentMethod = new ArrayList<>();
        paymentMethod.add("COD");
        paymentMethod.add("ZaloPay");
        paymentMethod.add("VNPay");
        paymentMethod.add("MOMO");
        paymentMethod.add("Visa/MasterCard");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, paymentMethod);
        lv_payment_method.setAdapter(adapter);
        lv_payment_method.setOnItemClickListener((parent, view, position, id) -> {
            Object item = lv_payment_method.getItemAtPosition(position);
            if(item.toString().equalsIgnoreCase("VNPay") || item.toString().equalsIgnoreCase("Visa/MasterCard")){
                showAlertDialog("Coming Soon !","Phương thức thanh toán này hiện đang trong quá trình phát triển, chúng tôi sẽ cập nhật tính năng trong thời gian sớm nhất !");
            }
            else {
                tv_paymentMethod.setText(item.toString());
                dialog.dismiss();
            }

        });

        // show dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public List<RedeemedCouponDTO> getCoupons() {
        CouponAPI couponAPI = ServiceBuilder.buildService(CouponAPI.class);
        Call<List<RedeemedCouponDTO>> call = couponAPI.getRedeemedCoupons();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<RedeemedCouponDTO>> future = executorService.submit(() -> {
            try {
                Response<List<RedeemedCouponDTO>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    finish();
                    mContext.startActivity(intent);
                    return null;
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items (response)", Toast.LENGTH_LONG).show();
                    return null;
                }
            } catch (IOException e) {
                Toast.makeText(mContext, "A connection error occurred", Toast.LENGTH_LONG).show();
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

    public void handlerApplyCoupon(RedeemedCouponDTO redeemedCoupon) {
        if (redeemedCoupon != null) {
            tv_voucherCode.setVisibility(TextView.VISIBLE);
            tv_addVoucher.setVisibility(TextView.GONE);
            tv_voucherCode.setText(redeemedCoupon.getCode()); // display coupon code
            int total = Convert.convertCurrencyFormat(tv_totalPrice.getText().toString().trim()); // get total price
            int totalProductDiscount = Convert.convertCurrencyFormat(tv_productDiscount.getText().toString().trim()); // get total product discount
            int totalVoucherDiscount = redeemedCoupon.getCoupon().getDiscountPercent() * total / 100; // calculate voucher discount
            int totalAmountCart = total - totalVoucherDiscount - totalProductDiscount; // calculate total amount
            int totalDiscount = totalProductDiscount + totalVoucherDiscount;
            tv_totalDiscount.setText(Convert.convertPrice(totalDiscount));
            tv_voucherDiscount.setText(Convert.convertPrice(totalVoucherDiscount)); // display voucher discount
            tv_finalTotalPrice.setText(Convert.convertPrice(totalAmountCart)); // display total final
        } else {
            tv_voucherCode.setVisibility(TextView.GONE);
            tv_addVoucher.setVisibility(TextView.VISIBLE);
            int total = Convert.convertCurrencyFormat(tv_totalPrice.getText().toString().trim()); // get total price
            int totalProductDiscount = Convert.convertCurrencyFormat(tv_productDiscount.getText().toString().trim()); // get total product discount
            int totalAmountCart = total - totalProductDiscount; // calculate total amount
            tv_totalDiscount.setText(Convert.convertPrice(totalProductDiscount));
            tv_voucherDiscount.setText("0 đ"); // display voucher discount
            tv_finalTotalPrice.setText(Convert.convertPrice(totalAmountCart)); // display total amount
        }
    }

    @Override
    public void onVoucherItemClick(int position) {
        if (position == -1 && txt_input_code.getText().toString().isEmpty()) {
            btn_Apply.setEnabled(false);
            redeemedCoupon = couponCheckboxAdapter.getCouponSelected();
            voucherDiscountPercent = 0;
            positionVoucherItemSelected = -1;
            handlerApplyCoupon(redeemedCoupon);
        }
        if (position != -1) {
            btn_Apply.setEnabled(true);
        }
    }

    private void showAlertDialog(String title, String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setIcon(R.drawable.drug) // Đặt icon của Dialog
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                })
                .show();
    }

    private String extractErrorMessage(retrofit2.Response<?> response) {
        try {
            if (response == null || response.errorBody() == null) return null;
            String raw = response.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) return null;

            // thử parse JSON kiểu { "message": "..." }
            try {
                JSONObject obj = new JSONObject(raw);
                if (obj.has("message")) return obj.getString("message");
                if (obj.has("error")) return obj.getString("error");
            } catch (Exception ignored) {}

            return raw;
        } catch (Exception e) {
            return null;
        }
    }

    private void handleOutOfStock409(retrofit2.Response<?> response) {
        String msg = extractErrorMessage(response);
        if (msg == null || msg.trim().isEmpty()) {
            msg = "Sản phẩm đã hết hàng hoặc không đủ số lượng (người mua trước sẽ được). Vui lòng quay lại giỏ hàng và thử lại.";
        }
        showAlertDialog("Thanh toán không thành công", msg);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getAddress();
    }
}