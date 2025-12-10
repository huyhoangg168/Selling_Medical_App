package com.example.clientsellingmedicine.activity.productAndPayment;


import android.content.Context;
import android.content.Intent;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.clientsellingmedicine.R;

import com.example.clientsellingmedicine.activity.order.OrderDetailActivity;
import com.example.clientsellingmedicine.models.MoMoOrderInfo;
import com.example.clientsellingmedicine.models.Order;
import com.example.clientsellingmedicine.api.OrderAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Convert;


import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MomoPaymentResultActivity extends AppCompatActivity {
    private Context mContext;


    private ImageView ivBack, imageView;
    private Button btn_ViewOrder, btn_ContinueShopping;
    private TextView tv_Title, tv_Content, tv_Time, tv_PaymentMethod, tv_Amount, tv_OrderID, tv_OrderStatus;

    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.result_payment);

        addControl();
        addEvents();

    }

    private void addControl() {
        imageView = findViewById(R.id.imageView);
        ivBack = findViewById(R.id.ivBack);
        btn_ViewOrder = findViewById(R.id.btn_ViewOrder);
        btn_ContinueShopping = findViewById(R.id.btn_ContinueShopping);
        tv_Title = findViewById(R.id.tv_Title);
        tv_Content = findViewById(R.id.tv_Content);
        tv_Time = findViewById(R.id.tv_Time);
        tv_PaymentMethod = findViewById(R.id.tv_PaymentMethod);
        tv_Amount = findViewById(R.id.tv_Amount);
        tv_OrderID = findViewById(R.id.tv_OrderID);
        tv_OrderStatus = findViewById(R.id.tv_OrderStatus);
    }

    private void addEvents() {
        ivBack.setOnClickListener(v -> {
            finish();
        });

        btn_ViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, OrderDetailActivity.class);
            intent.putExtra("code", code);
            finish();
            startActivity(intent);
        });

        btn_ContinueShopping.setOnClickListener(v -> {
            finish(); //back to Cart activity
        });

        Intent intent = getIntent();
        if (intent != null) {
            // open from app => payment online

            if (intent.getData() != null) {
                Uri uri = intent.getData();
                Log.d("tag", "addEvents: "+uri);
                if(uri.getPath().equalsIgnoreCase("/momo")){
                    handlerPaymentResultWithMoMo(uri);
                } else if (uri.getPath().equalsIgnoreCase("/zalopay")) {
                    handlerPaymentResultWithZaloPay(uri);
                }

            } else {
                //open from Payment Activity => payment with COD
                Order order = (Order) intent.getSerializableExtra("Order");
                int statusCode = intent.getIntExtra("statusCode", -1);
                handlerPaymentResultWithCOD(order, statusCode);
            }
        }
    }

    private void handlerPaymentResultWithZaloPay(Uri uri) {
        String resultCode = uri.getQueryParameter("status");

        String amount = uri.getQueryParameter("amount");
        String apptransid = uri.getQueryParameter("apptransid");

        code = apptransid.split("_")[1];
        String responseTime = getCurrentTime();

        //display order info
        handlerDisplayOrderInfo(code, amount, "ZaloPay", responseTime);

        //payment failed
        if (Integer.parseInt(resultCode) != 1) {
            handlerPaymentFailed("Đã có lỗi bất ngờ xảy ra! Vui lòng thử lại sao ít phút!");

        }

        //Request to ZaloPay server get status and update order to DB
        checkZaloPayOrderStatus(apptransid);

    }

    private void handlerPaymentResultWithMoMo(Uri deepLinkUri) {
        //Log.d("tag", "handlerPaymentResultWithMoMo: "+deepLinkUri);


        String partnerCode = deepLinkUri.getQueryParameter("partnerCode");
        String orderId = deepLinkUri.getQueryParameter("orderId");
        code = orderId;// in deeplink , orderId = order code (unique)
        String requestId = deepLinkUri.getQueryParameter("requestId");
        String amount = deepLinkUri.getQueryParameter("amount");
        String orderInfo = deepLinkUri.getQueryParameter("orderInfo");
        String orderType = deepLinkUri.getQueryParameter("orderType");
        String transId = deepLinkUri.getQueryParameter("transId");
        String resultCode = deepLinkUri.getQueryParameter("resultCode");
        String message = deepLinkUri.getQueryParameter("message");
        String payType = deepLinkUri.getQueryParameter("payType");
        String responseTime = deepLinkUri.getQueryParameter("responseTime");
        String extraData = deepLinkUri.getQueryParameter("extraData");
        String signature = deepLinkUri.getQueryParameter("signature");

        String msg = "";
        try {
            msg = URLDecoder.decode(message, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //save momo order info to DB
        MoMoOrderInfo order = new MoMoOrderInfo(partnerCode, orderId, requestId, amount, orderInfo, orderType, transId, resultCode, message, payType, responseTime, extraData, signature);
        saveMoMoOrderInfo(order);


        //display order info
        handlerDisplayOrderInfo(code, amount, partnerCode, responseTime);

        //payment failed
        if (Integer.parseInt(resultCode) != 0) {
            handlerPaymentFailed(msg);
        }

        //Request to MoMo server get status and update order to DB
        checkMoMoOrderStatus(partnerCode, requestId, orderId);
    }

    private void handlerPaymentResultWithCOD(Order order, int statusCode) {

        code = order.getCode();
        String amount = order.getTotal().toString();
        String method = order.getPaymentMethod();
        String time = order.getOrderTime().toString();

        //display order info
        handlerDisplayOrderInfo(code, amount, method, time);

        //payment failed
        if (statusCode != 201) {
            handlerPaymentFailed("đã có lỗi xảy ra. Vui lòng thử lại sau ít phút hoặc liên hệ Admin để giải quyết !");
        }

    }

    private void handlerDisplayOrderInfo(String code, String amount, String method, String responseTime) {
        tv_OrderID.setText(code);
        tv_Amount.setText(Convert.convertPrice(Integer.parseInt(amount)));
        tv_PaymentMethod.setText(method);
        tv_OrderStatus.setText(method.equalsIgnoreCase("cod") ? "Chưa thanh toán" : "Chờ xác nhận");
        String time = Convert.convertToDate(responseTime);
        tv_Time.setText(time != null ? time : getCurrentTime());
    }

    private void handlerPaymentFailed(String msg) {
        imageView.setImageResource(R.drawable.failed);
        tv_Title.setText("Đặt hàng thất bại");
        tv_Content.setText("Đặt hàng không thành công vì " + msg);
    }

    private String getCurrentTime(){
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(now);
    }


    public void saveMoMoOrderInfo(MoMoOrderInfo order) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<MoMoOrderInfo> request = orderAPI.saveMoMoOrderInfo(order);

        request.enqueue(new Callback<MoMoOrderInfo>() {
            @Override
            public void onResponse(Call<MoMoOrderInfo> call, Response<MoMoOrderInfo> response) {
            }

            @Override
            public void onFailure(Call<MoMoOrderInfo> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured, Save MoMo Order Info Error!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Save MoMo Order Info Error!", Toast.LENGTH_LONG).show();
                }
            }

        });
    }


    public void checkZaloPayOrderStatus(String appTransId) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<ResponseBody> request = orderAPI.checkZalopayOrderStatus(appTransId);

        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string(); // Đọc nội dung phản hồi dưới dạng chuỗi
                        JSONObject jsonObject = new JSONObject(responseBody);
                        int momoOrderStatus = jsonObject.getInt("orderStatus");
                        if(momoOrderStatus == 1)
                            tv_OrderStatus.setText("Đã thanh toán");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void checkMoMoOrderStatus(String partnerCode, String requestId, String orderId) {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<ResponseBody> request = orderAPI.checkMoMoOrderStatus(partnerCode, requestId, orderId);

        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string(); // Đọc nội dung phản hồi dưới dạng chuỗi
                        JSONObject jsonObject = new JSONObject(responseBody);
                        int momoOrderStatus = jsonObject.getInt("orderStatus");
                        if(momoOrderStatus == 0 || momoOrderStatus == 9000)
                            tv_OrderStatus.setText("Đã thanh toán");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

}

