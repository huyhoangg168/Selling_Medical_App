package com.example.clientsellingmedicine.service;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.Helper.NotificationHelper;
import com.example.clientsellingmedicine.api.NotificationAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Log the from field to see the sender ID
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {

            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String imageUrl = remoteMessage.getNotification().getImageUrl() != null ? remoteMessage.getNotification().getImageUrl().toString() : null;

            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.sendNotification(title, body, imageUrl);
        }

        // Check if the message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            // Extract data payload and process it
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Data Payload: " + data.toString());
            // Here you can handle the data payload
            // e.g., update the database, perform background tasks, etc.
        }
    }

    @Override
    public void onNewToken(String token) {
        saveFirebaseDeviceToken(token);
    }

    // save to Shared Preferences
    private void saveFirebaseDeviceToken(String token) {
        Token saveToken = new Token(token);
        SharedPref.saveToken(this, Constants.FIREBASE_TOKEN_PREFS_NAME, Constants.KEY_FIREBASE_TOKEN, saveToken);

        //save to DB
        saveFirebaseDeviceToken(saveToken); //sometime it failed because user not login
    }

    private void saveFirebaseDeviceToken(Token deviceToken){
        NotificationAPI notificationAPI = ServiceBuilder.buildService(NotificationAPI.class);
        Call<Void> request = notificationAPI.saveDevice(deviceToken);
        request.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Save firebase device token successfully ! ");
                }
                else {
                    Log.d("FCM", "Save firebase device token failed !");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("FCM", "Save firebase device token failed !");
            }
        });
    }
}
