package com.example.clientsellingmedicine.interfaces;

public interface PhoneNumberCheckCallback {
    void onSuccess(boolean isExists);
    void onError(String errorMessage);
}