package com.example.clientsellingmedicine.utils;

import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.api.LoginAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Response;

public class Validator {
    public static boolean isTokenValid(Token token) {
        return checkTokenValidity(token);
    }

    private static final String VIETNAM_PHONE_REGEX = "^(0)(3|5|7|8|9)\\d{8}$";

    // Hàm kiểm tra số điện thoại
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }

        String cleaned = phoneNumber.replaceAll(" ", "");

        // Kiểm tra số điện thoại có khớp với biểu thức chính quy
        return Pattern.matches(VIETNAM_PHONE_REGEX, cleaned);
    }

    public static boolean checkTokenValidity(Token token) {
        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Boolean> call = loginAPI.checkToken(token);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    Response<Boolean> response = call.execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        } finally {
            executorService.shutdown();
        }
    }
}
