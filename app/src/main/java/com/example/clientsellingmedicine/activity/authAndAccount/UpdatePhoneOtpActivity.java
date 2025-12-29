package com.example.clientsellingmedicine.activity.authAndAccount;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePhoneOtpActivity extends AppCompatActivity {

    private Context mContext;
    private TextView tvCountDown, tvOtpPhone, tvResendOtp;
    private EditText edt1, edt2, edt3, edt4, edt5, edt6;
    private EditText[] editTexts = new EditText[6];

    // Logic đếm ngược & Firebase
    private final Integer COUNT_DOWN_TIME = 60; // 60 giây
    private CountDownTimer countDownTimer;
    private FirebaseAuth mAuth;
    private String verificationId, newPhoneNumber;

    // --- [MỚI] BIẾN CHỐNG SPAM & BRUTE FORCE ---
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private int failedAttemptCount = 0;
    private final int MAX_FAILED_ATTEMPTS = 3; // Cho phép sai tối đa 5 lần
    // ------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_confirm_screen);

        mContext = this;
        mAuth = FirebaseAuth.getInstance();

        addControls();
        addEvents();
    }

    private void addControls() {
        tvCountDown = findViewById(R.id.tvCountDown);
        tvOtpPhone = findViewById(R.id.tvOtpPhone);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        edt1 = findViewById(R.id.edtConfimNumber1);
        edt2 = findViewById(R.id.edtConfimNumber2);
        edt3 = findViewById(R.id.edtConfimNumber3);
        edt4 = findViewById(R.id.edtConfimNumber4);
        edt5 = findViewById(R.id.edtConfimNumber5);
        edt6 = findViewById(R.id.edtConfimNumber6);

        editTexts[0] = edt1; editTexts[1] = edt2; editTexts[2] = edt3;
        editTexts[3] = edt4; editTexts[4] = edt5; editTexts[5] = edt6;

        // Lấy dữ liệu từ màn hình trước gửi sang
        verificationId = getIntent().getStringExtra("verificationId");
        newPhoneNumber = getIntent().getStringExtra("newPhoneNumber");

        // --- [MỚI] NHẬN TOKEN TỪ MÀN HÌNH TRƯỚC (ChangePhoneActivity) ---
        // (Bạn nhớ sửa ChangePhoneActivity gửi kèm cái này sang nhé)
        mResendToken = getIntent().getParcelableExtra("resendToken");
        // ----------------------------------------------------------------

        // Hiển thị số điện thoại (Che bớt số)
        if (newPhoneNumber != null && newPhoneNumber.length() > 7) {
            String masked = newPhoneNumber.substring(0, 3) + "xxx" + newPhoneNumber.substring(newPhoneNumber.length() - 4);
            tvOtpPhone.setText(masked);
        } else {
            tvOtpPhone.setText(newPhoneNumber);
        }
    }

    private void addEvents() {
        startCountDownTimer(COUNT_DOWN_TIME);
        setupEditTexts();

        // Sự kiện gửi lại mã
        tvResendOtp.setOnClickListener(v -> resendVerificationCode());
    }

    // --- LOGIC NHẬP OTP (GIỮ NGUYÊN) ---
    private void setupEditTexts() {
        for (int i = 0; i < editTexts.length; i++) {
            final int index = i;
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < editTexts.length - 1) {
                        editTexts[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        editTexts[index - 1].requestFocus();
                    }
                    checkAndVerify();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            editTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (editTexts[index].getText().toString().isEmpty() && index > 0) {
                        editTexts[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    private void checkAndVerify() {
        StringBuilder otp = new StringBuilder();
        for (EditText editText : editTexts) {
            otp.append(editText.getText().toString());
        }
        if (otp.length() == 6) {
            verifyCode(otp.toString());
        }
    }

    // --- [NÂNG CẤP] XỬ LÝ VERIFY KÈM GIỚI HẠN SỐ LẦN SAI ---
    private void verifyCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // OTP ĐÚNG -> Gọi API Update của Backend
                        callApiUpdatePhone();
                    } else {
                        // --- OTP SAI ---
                        handleFailedAttempt();
                    }
                });
    }

    private void handleFailedAttempt() {
        failedAttemptCount++; // Tăng số lần sai
        int remaining = MAX_FAILED_ATTEMPTS - failedAttemptCount;

        if (remaining <= 0) {
            // Quá số lần cho phép -> Chặn
            showLimitReachedDialog();
        } else {
            // Còn lượt thử -> Cảnh báo
            Toast.makeText(mContext, "Mã OTP không đúng. Còn " + remaining + " lần thử.", Toast.LENGTH_LONG).show();
            resetInputs();
        }
    }

    private void showLimitReachedDialog() {
        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Cảnh báo bảo mật")
                .setMessage("Bạn đã nhập sai quá " + MAX_FAILED_ATTEMPTS + " lần. Vui lòng thực hiện lại thao tác đổi số điện thoại.")
                .setCancelable(false)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    finish(); // Thoát màn hình này
                })
                .show();
    }

    private void callApiUpdatePhone() {
        if (countDownTimer != null) countDownTimer.cancel();
        Toast.makeText(mContext, "Đang cập nhật số điện thoại...", Toast.LENGTH_SHORT).show();

        UserDTO userUpdate = new UserDTO();
        userUpdate.setPhone(newPhoneNumber);

        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        userAPI.updateUser(userUpdate).enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else if (response.code() == 409) {
                    showErrorDialog("Số điện thoại này đã được sử dụng bởi tài khoản khác.");
                } else {
                    showErrorDialog("Cập nhật thất bại. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                showErrorDialog("Lỗi kết nối mạng.");
            }
        });
    }

    // --- CÁC HÀM PHỤ TRỢ (Dialog, Timer, Resend) ---

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Thành công")
                .setMessage("Đổi số điện thoại thành công. Vui lòng đăng nhập lại bằng số mới.")
                .setCancelable(false)
                .setPositiveButton("Đăng nhập lại", (dialog, which) -> {
                    EncryptedSharedPrefManager.clearAll(mContext);
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("Đóng", (dialog, which) -> {
                    finish();
                })
                .show();
    }

    private void resetInputs() {
        for (EditText edt : editTexts) {
            edt.setText("");
        }
        editTexts[0].requestFocus();
    }

    private void startCountDownTimer(int initialTime) {
        tvResendOtp.setVisibility(View.GONE);
        tvCountDown.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(initialTime * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                String formattedTime = String.format("%02d:%02d",
                        (millisUntilFinished / 1000) / 60,
                        (millisUntilFinished / 1000) % 60);
                tvCountDown.setText(formattedTime);
            }
            @Override
            public void onFinish() {
                tvCountDown.setVisibility(View.GONE);
                tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    // --- [NÂNG CẤP] GỬI LẠI MÃ AN TOÀN ---
    private void resendVerificationCode() {
        if (newPhoneNumber == null || newPhoneNumber.trim().isEmpty()) {
            Toast.makeText(mContext, "Lỗi số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        tvResendOtp.setVisibility(View.GONE);
        Toast.makeText(mContext, "Đang gửi lại mã...", Toast.LENGTH_SHORT).show();

        // 1. Format số điện thoại an toàn
        String cleanPhone = newPhoneNumber.trim();
        String internationalPhone;
        if (cleanPhone.startsWith("0")) {
            internationalPhone = "+84" + cleanPhone.substring(1);
        } else if (cleanPhone.startsWith("+84")) {
            internationalPhone = cleanPhone;
        } else {
            internationalPhone = "+84" + cleanPhone;
        }

        Log.d("UPDATE_PHONE_OTP", "Resending to: " + internationalPhone);

        PhoneAuthOptions.Builder optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(internationalPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {}

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("UPDATE_PHONE_OTP", "Error: " + e.getMessage());
                        Toast.makeText(mContext, "Lỗi gửi lại mã: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvResendOtp.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = s; // Cập nhật ID mới
                        mResendToken = token; // Cập nhật Token mới

                        // [QUAN TRỌNG] Reset số lần sai về 0 khi gửi lại mã
                        failedAttemptCount = 0;

                        startCountDownTimer(COUNT_DOWN_TIME);
                        Toast.makeText(mContext, "Đã gửi lại mã!", Toast.LENGTH_SHORT).show();
                        resetInputs();
                    }
                });

        // 2. Gắn Token chống Spam
        if (mResendToken != null) {
            optionsBuilder.setForceResendingToken(mResendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build());
    }
}