package com.example.clientsellingmedicine.api;


import android.os.Build;

import com.example.clientsellingmedicine.activity.MyApplication;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.clientsellingmedicine.BuildConfig;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceBuilder {

    private static final String URL =  BuildConfig.API_URL;

    // Create logger
    private static HttpLoggingInterceptor logger =
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    // Create OkHttp Client
    private static OkHttpClient.Builder okHttp =
            new OkHttpClient.Builder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        private boolean isRefreshing = false; // check fresh token status

                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request originalRequest = chain.request();
                            //Build new request
                            Request.Builder builder = originalRequest.newBuilder();

                            Token token = EncryptedSharedPrefManager.loadToken(MyApplication.getContext());
                            setAuthHeader(builder, token);

                            // Add headers to the builder before building the request
                            builder.addHeader("x-device-type", Build.DEVICE)
                                    .addHeader("Accept-Language", Locale.getDefault().getLanguage());

                            // Build the request with all the required headers
                            Request request = builder.build();

                            Response response = chain.proceed(request);
                            // Access token is expired
                            if (response.code() == 401) {
                                if (!isRefreshing) { // Kiểm tra trạng thái làm mới token
                                    synchronized (okHttp) {
                                        isRefreshing = true; // Đánh dấu bắt đầu quá trình làm mới token
                                        Token currentToken = EncryptedSharedPrefManager.loadToken(MyApplication.getContext());
                                        if (currentToken != null && currentToken.getToken().equals(token.getToken())) {
                                            int code = refreshToken() / 100;
                                            if (code == 2) { // Nếu refresh token thành công
                                                Token newToken = EncryptedSharedPrefManager.loadToken(MyApplication.getContext());                                                setAuthHeader(builder, newToken);
                                                Request newRequest = builder.build();
                                                Response newResponse = chain.proceed(newRequest);
                                                isRefreshing = false; // Đánh dấu kết thúc quá trình làm mới token
                                                return newResponse; // Trả về response mới
                                            }
                                        }
                                    }
                                }
                            }
                            return response;
                        }
                    })
                    .addInterceptor(logger);


    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .create();
    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttp.build());

    private static Retrofit retrofit = builder.build();

    public static <S> S buildService(Class<S> serviceType) {
        return retrofit.create(serviceType);
    }


    private static void setAuthHeader(Request.Builder builder, Token token) {
        if (token !=null && token.getToken() != null && token.getToken() != "") //Add Auth token to each request if authorized
            builder.header("Authorization", String.format("Bearer %s", token.getToken()));
    }

    private static int refreshToken() {
        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> requestRefreshToken = loginAPI.refreshToken();

        try {
            retrofit2.Response<Token> response = requestRefreshToken.execute();

            if(response.isSuccessful()) {
                Token newToken = response.body();
                EncryptedSharedPrefManager.saveToken(MyApplication.getContext(), newToken);
            }
            return response.code();
        } catch (IOException e) {
            return 500; // Trả về mã lỗi mặc định
        }
    }


}