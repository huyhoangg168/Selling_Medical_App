package com.example.clientsellingmedicine.service;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.Helper.NotificationHelper;
import com.example.clientsellingmedicine.api.NotificationAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
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

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Log the from field to see the sender ID
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            // Kiểm tra null an toàn hơn cho ImageUrl
            String imageUrl = remoteMessage.getNotification().getImageUrl() != null ? remoteMessage.getNotification().getImageUrl().toString() : null;

            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.sendNotification(title, body, imageUrl);
        }

        // Check if the message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Data Payload: " + data.toString());
        }
    }

    @Override
    public void onNewToken(String token) {
        // Hàm này được gọi khi Firebase cấp token mới cho thiết bị
        handleNewToken(token);
    }

    // Xử lý lưu token
    private void handleNewToken(String token) {
        // 1. Lưu vào Encrypted Shared Preferences
        if (token != null && !token.isEmpty()) {
            Token saveToken = new Token(token);
            EncryptedSharedPrefManager.saveFirebaseToken(this, saveToken);

            // 2. Gửi lên Server (nếu Backend yêu cầu gửi dạng Object Token)
            sendTokenToBackend(saveToken);
        } else {
            Log.w("FCM", "Token is null or empty, not saving");
        }
    }

    // Gửi token lên Backend
    private void sendTokenToBackend(Token deviceToken) {
        // Lưu ý: Hàm này có thể fail nếu User chưa đăng nhập (ServiceBuilder ko có Auth header)
        // Điều này bình thường, ta sẽ gửi lại token này ở màn hình Login/Main sau.
        try {
            NotificationAPI notificationAPI = ServiceBuilder.buildService(NotificationAPI.class);
            Call<Void> request = notificationAPI.saveDevice(deviceToken);
            request.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("FCM", "Save firebase device token successfully!");
                    } else {
                        Log.d("FCM", "Save firebase device token failed! Code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("FCM", "Save firebase device token failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FCM", "Error sending token: " + e.getMessage());
        }
    }
}
