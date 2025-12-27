package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;

import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.activity.authAndAccount.AdminProductActivity;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.activity.authAndAccount.ProfileFragment;
import com.example.clientsellingmedicine.activity.authAndAccount.UnLoginProfileFragment;
import com.example.clientsellingmedicine.activity.coupon.ExchangeFragment;
import com.example.clientsellingmedicine.activity.order.OrderFragment;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView
        .OnNavigationItemSelectedListener {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;
    private static int request_notifi_permission = 2;
    private boolean doubleBackToExitPressedOnce = false;
    private Handler mHandler = new Handler();

    private BottomNavigationView bottomNavigationView;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        Token token = EncryptedSharedPrefManager.loadToken(mContext);
        UserDTO user = EncryptedSharedPrefManager.loadUser(mContext);

        // 1. Kiểm tra trạng thái "Soft Logout" (Chờ vân tay)
        // Nếu object Token tồn tại (để giữ refresh token) NHƯNG AccessToken lại là NULL
        // => Nghĩa là User đã Logout và đang chờ quét vân tay.
        // => KHÔNG ĐƯỢC CHO VÀO MAIN (Sẽ crash vì thiếu token gọi API).
        if (token != null && token.getToken() == null) {
            navigateToLogin();
            return; // Dừng code tại đây, không chạy xuống dưới nữa
        }

        // 2. Kiểm tra lỗi dữ liệu (Token Zombie)
        // Trường hợp hiếm: Có Token nhưng thông tin User lại bị Null (do lỗi lúc logout hoặc lỗi lưu trữ)
        // => Cũng đá về Login để đăng nhập lại cho sạch.
        if (token != null && user == null) {
            navigateToLogin();
            return;
        }

        // 3. Kiểm tra quyền Admin
        // Lúc này chắc chắn User != null và Token != null
        if (token != null && "admin".equalsIgnoreCase(user.getRole())) {
            startActivity(new Intent(MainActivity.this, AdminProductActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.fragment);

        bottomNavigationView
                = findViewById(R.id.bottom_navigation);

        bottomNavigationView
                .setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // request notification permission
        requestPermission();

        // Press the back button twice to return to home, if you are at home then finish()
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (bottomNavigationView.getSelectedItemId() == R.id.navigation_home) {
                    if (doubleBackToExitPressedOnce) {
                        finish();
                    } else {
                        doubleBackToExitPressedOnce = true;
                        Toast.makeText(getApplicationContext(), "Nhấn nút back thêm lần nữa để thoát app", Toast.LENGTH_SHORT).show();
                        mHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                    }
                } else {
                    goToHomeFragment();
                }
            }
        };


        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // [HÀM MỚI] Hàm điều hướng về Login và xóa Activity Stack
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Cờ này giúp xóa sạch MainActivity cũ khỏi bộ nhớ, tránh người dùng Back lại được
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    HomeFragment homeFragment = new HomeFragment();
    //    CategoryFragment categoryFragment = new CategoryFragment();
    ExchangeFragment exchangeFragment = new ExchangeFragment();
    OrderFragment orderFragment = new OrderFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    UnLoginProfileFragment unLoginProfileFragment = new UnLoginProfileFragment();


    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, homeFragment)
                    .commit();
            return true;
        } else if (id == R.id.navigation_medal) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, exchangeFragment)
                    .commit();
            return true;

        } else if (id == R.id.navigation_prescription) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, orderFragment)
                    .commit();
            return true;

        } else if (id == R.id.navigation_user) {
            Token token = EncryptedSharedPrefManager.loadToken(mContext);            if (token == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, unLoginProfileFragment)
                        .commit();
                return true;
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, profileFragment)
                        .commit();
                return true;
            }
        }

        return false;
    }


    public void goToHomeFragment() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, chuyển đến MainActivity hoặc màn hình chính của ứng dụng
            } else {
                // Quyền không được cấp, xử lý trường hợp này nếu cần
                if (request_notifi_permission == 1) {   //hiển thị thông báo nếu từ chối lần đầu tiên
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
                    builder.setIcon(R.drawable.drug)
                            .setTitle("Yêu cầu cho phép thông báo")
                            .setMessage("Ứng dụng cần cho phép thông báo để hiển thị thông tin mới nhất đến người dùng, vui lòng cấp quyền cho ứng dụng!")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                requestPermission();
                            })
                            .show();
                }
            }
        }
    }

    private void requestPermission() {
        request_notifi_permission -= 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                // Quyền đã được cấp, chuyển đến MainActivity hoặc màn hình chính của ứng dụng
            }
        } else {
            // Nếu thiết bị chạy dưới Android 13, không cần yêu cầu quyền này, chuyển đến MainActivity
        }
    }


}





