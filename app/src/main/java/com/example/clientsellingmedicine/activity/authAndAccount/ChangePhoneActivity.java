package com.example.clientsellingmedicine.activity.authAndAccount;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.LoadingManager;
import com.example.clientsellingmedicine.utils.Validator;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePhoneActivity extends AppCompatActivity {
    private Context mContext;
    private ImageView ivBack;
    private EditText edtOldPhone, edtNewPhone;
    private AppCompatButton btnConfirmChange;

    // Firebase Vars
    private FirebaseAuth mAuth;
    private String verificationId;

    // Data
    private UserDTO currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone);
        mContext = this;

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        addControls();
        loadCurrentPhone(); // Load sđt cũ lên
        addEvents();
    }

    private void addControls() {
        ivBack = findViewById(R.id.ivBack);
        edtOldPhone = findViewById(R.id.edtOldPhone);
        edtNewPhone = findViewById(R.id.edtNewPhone);
        btnConfirmChange = findViewById(R.id.btnConfirmChange);
    }

    private void loadCurrentPhone() {
        // Lấy thông tin user từ SharedPref hoặc Intent
        currentUser = EncryptedSharedPrefManager.loadUser(mContext);
        if (currentUser != null && currentUser.getPhone() != null) {
            edtOldPhone.setText(currentUser.getPhone());
        }
    }

    private void addEvents() {
        ivBack.setOnClickListener(v -> finish());

        btnConfirmChange.setOnClickListener(v -> {
            String oldPhone = edtOldPhone.getText().toString().trim();
            String newPhone = edtNewPhone.getText().toString().trim();

            if (!validatePhone(oldPhone, newPhone)) {
                return;
            }

            // Gửi OTP đến số MỚI
            String internationalPhone = "+84" + newPhone.substring(1);
            sendVerificationCode(internationalPhone);
        });
    }

    private boolean validatePhone(String oldPhone, String newPhone) {
        if (newPhone.isEmpty()) {
            Toast.makeText(mContext, "Vui lòng nhập số điện thoại mới", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Validator.isValidPhoneNumber(newPhone)) { // Dùng hàm Validator có sẵn của bạn
            Toast.makeText(mContext, "Số điện thoại không đúng định dạng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (newPhone.equals(oldPhone)) {
            Toast.makeText(mContext, "Số mới không được trùng với số cũ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ----------------------------------------------------------------
    // FIREBASE OTP LOGIC
    // ----------------------------------------------------------------

    private void sendVerificationCode(String phoneNumber) {
        LoadingManager.showLoading(this);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Trong ChangePhoneActivity.java

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Để trống hoặc tự động đăng nhập nếu muốn
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    LoadingManager.hideLoading();
                    Toast.makeText(mContext, "Lỗi gửi mã OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    LoadingManager.hideLoading();

                    // --- THAY ĐỔI Ở ĐÂY ---
                    // Không hiện Dialog nữa, mà chuyển sang màn hình nhập OTP riêng
                    Intent intent = new Intent(mContext, UpdatePhoneOtpActivity.class);

                    // Truyền dữ liệu cần thiết sang
                    intent.putExtra("resendToken", token);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("newPhoneNumber", edtNewPhone.getText().toString().trim()); // Số đt mới

                    startActivity(intent);
                }
            };

    private void showOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Tạo View nhập OTP đơn giản
        // Bạn có thể tạo 1 file xml dialog riêng nếu muốn đẹp
        final EditText input = new EditText(this);
        input.setHint("Nhập mã 6 số gửi về SĐT mới");
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(50, 50, 50, 50);

        builder.setView(input);
        builder.setTitle("Xác thực thay đổi");
        builder.setMessage("Mã OTP đã được gửi đến số " + edtNewPhone.getText().toString());
        builder.setCancelable(false);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            // Do nothing here, we override onClick below to prevent auto-close on error
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override nút Positive để kiểm tra OTP xong mới đóng dialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = input.getText().toString().trim();
            if (code.length() < 6) {
                input.setError("Mã không hợp lệ");
                return;
            }
            dialog.dismiss();
            verifyCode(code);
        });
    }

    private void verifyCode(String code) {
        LoadingManager.showLoading(this);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // OTP ĐÚNG -> Gọi API Backend để cập nhật
                        callApiUpdatePhone();
                    } else {
                        LoadingManager.hideLoading();
                        Toast.makeText(mContext, "Mã OTP không đúng!", Toast.LENGTH_SHORT).show();
                        showOtpDialog(); // Cho nhập lại
                    }
                });
    }

    // ----------------------------------------------------------------
    // CALL API UPDATE & LOGOUT
    // ----------------------------------------------------------------

    private void callApiUpdatePhone() {
        String newPhone = edtNewPhone.getText().toString().trim();

        UserDTO userUpdate = new UserDTO();
        userUpdate.setPhone(newPhone);
        // Lưu ý: Backend cần check xem username/phone này đã tồn tại chưa

        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        userAPI.updateUser(userUpdate).enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                LoadingManager.hideLoading();
                if (response.isSuccessful()) {
                    // Cập nhật thành công -> Bắt buộc logout
                    performLogout();
                } else if (response.code() == 409) {
                    Toast.makeText(mContext, "Số điện thoại này đã được sử dụng bởi tài khoản khác!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Cập nhật thất bại. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                LoadingManager.hideLoading();
                Toast.makeText(mContext, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        Toast.makeText(mContext, "Đổi số thành công. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();

        // Xóa dữ liệu đăng nhập cũ
        EncryptedSharedPrefManager.clearAll(mContext);

        // Về màn hình Login
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}