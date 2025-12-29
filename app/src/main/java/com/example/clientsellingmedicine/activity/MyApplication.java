package com.example.clientsellingmedicine.activity;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.cloudinary.android.MediaManager;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initCloudinaryConfig();
        getFCMToken();
        subscribeToTopic();
    }

    public static Context getContext() {
        return context;
    }

    private void initCloudinaryConfig() {

        Map config = new HashMap();
        config.put("cloud_name", "dwrd1yxgh");
        config.put("api_key", "716447925773513");
        config.put("api_secret", "JD584oxI3Qb9VTy6ZiQJYqSO6YY");
        //config.put("secure", true);
        MediaManager.init(this, config);
    }

    private void getFCMToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);

                    // save to Shared Preferences
                    saveFirebaseDeviceToken(token);
                });
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("global")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to global topic!";
                    if (!task.isSuccessful()) {
                        msg = "Subscription to global topic failed!";
                    }
                    Log.d("FCM", msg);
                });
    }

    // save to Encrypted Shared Preferences
    private void saveFirebaseDeviceToken(String token) {
        Token saveToken = new Token(token);
        EncryptedSharedPrefManager.saveFirebaseToken(this, saveToken);
    }

}
