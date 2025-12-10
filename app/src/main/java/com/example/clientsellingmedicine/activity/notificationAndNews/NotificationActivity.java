package com.example.clientsellingmedicine.activity.notificationAndNews;

import android.content.Context;
// Imports for UI elements and RecyclerView
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
// Imports for UI elements
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.Adapter.notificationAdapter;
// Import for your reward points history adapter (if applicable)
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.interfaces.IOnNotificationItemClickListener;
import com.example.clientsellingmedicine.DTO.Notification;
import com.example.clientsellingmedicine.api.NotificationAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements IOnNotificationItemClickListener {
    private LinearLayout layoutEmptyNotification;

    private Context mContext;
    private RecyclerView rcvNotification;
    private List<Notification> notificationList;
    private ImageView ivBackNotification;
    private TextView tvReadAllNotification;
    private notificationAdapter mAdapter;

    IOnNotificationItemClickListener iOnNotificationItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        // Set the correct layout for notifications
        setContentView(R.layout.notification_screen);
        addControl();
        addEvents();
    }

    private void addControl() {
        layoutEmptyNotification = findViewById(R.id.layoutEmptyNotification);
        rcvNotification = findViewById(R.id.rcvNotification);
        ivBackNotification = findViewById(R.id.ivBackNotification);
        tvReadAllNotification = findViewById(R.id.tvReadAllNotification);
        iOnNotificationItemClickListener = this;
    }

    private void addEvents() {
        ivBackNotification.setOnClickListener(v -> {
            finish();
        });
        tvReadAllNotification.setOnClickListener(v -> {
            seenAllNotification();
        });
    }

    public void getNotification() {
        NotificationAPI notificationAPI = ServiceBuilder.buildService(NotificationAPI.class);
        Call<List<Notification>> request = notificationAPI.getNotification();
        request.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful()) {
                    notificationList = response.body().stream().collect(Collectors.toList());
                    if (notificationList != null && notificationList.size() > 0) {
                        // Check for notifications and set visibility accordingly
                        rcvNotification.setVisibility(View.VISIBLE);
                        layoutEmptyNotification.setVisibility(View.GONE);

                        //remove notification have seen in Share Prefs if not in data response
                        List<Notification> listNotificationsHaveSeen = getNotificationsFromSharePrefs();
                        if(listNotificationsHaveSeen.size() > 0){
                            listNotificationsHaveSeen.removeIf(notifHaveSeen -> notificationList.stream()
                                    .noneMatch(notification -> notification.getId() == notifHaveSeen.getId()));
                        }

                        // Create and set adapter for the RecyclerView
                        mAdapter = new notificationAdapter(notificationList, iOnNotificationItemClickListener, listNotificationsHaveSeen);
                        rcvNotification.setAdapter(mAdapter);
                        rcvNotification.setLayoutManager(new LinearLayoutManager(mContext));
                    } else {
                        rcvNotification.setVisibility(View.GONE);
                        layoutEmptyNotification.setVisibility(View.VISIBLE);
                    }

                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Somethings was wrong. Please try again later !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    @Override
    public void onItemClick(Notification notification) {
        seenNotification(notification);
        open(notification);
    }

    private void seenNotification(Notification notification) {
        List<Notification> listNotificationsHaveSeen = getNotificationsFromSharePrefs();

        boolean exists = listNotificationsHaveSeen.stream().anyMatch(item -> item.getId() == notification.getId());
        if (!exists) {
            listNotificationsHaveSeen.add(notification);
            SharedPref.saveData(mContext, listNotificationsHaveSeen, Constants.NOTIFICATE_PREFS_NAME, Constants.KEY_NOTIFICATE);
        }
    }

    private void seenAllNotification() {
        //save all notification to SharedPreferences => all notification is seen
        if (notificationList != null && notificationList.size() > 0) {
            SharedPref.saveData(mContext, notificationList, Constants.NOTIFICATE_PREFS_NAME, Constants.KEY_NOTIFICATE);
            mAdapter.setmNotificationsHaveSeen(notificationList);
            mAdapter.notifyDataSetChanged();
        }
    }
    public void open(Notification notification) {
        Intent intent = new Intent(mContext, DetailNotificationActivity.class); // Replace with your actual activity class
        intent.putExtra("notification", notification); // Key to pass the notification object
        startActivity(intent);
    }

    private List<Notification> getNotificationsFromSharePrefs() {
        Type notificatetionType = new TypeToken<List<Notification>>() {
        }.getType();
        List<Notification> listNotificationsHaveSeen = SharedPref.loadData(mContext, Constants.NOTIFICATE_PREFS_NAME, Constants.KEY_NOTIFICATE, notificatetionType);
        return listNotificationsHaveSeen;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNotification();
    }
}
