package com.example.clientsellingmedicine.activity.productAndPayment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import lombok.NonNull;

public class PaymentOtpActivity extends AppCompatActivity {

    private Context mContext;
    private TextView tvCountDown, tvOtpPhone, tvResendOtp;
    private EditText edt1, edt2, edt3, edt4, edt5, edt6;
    private EditText[] editTexts;

    private final Integer COUNT_DOWN_TIME = 60;
    private String verificationId, phoneNumber;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private int failedAttemptCount = 0;
    private final int MAX_FAILED_ATTEMPTS = 3;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_confirm_screen); // Nhớ trỏ đúng layout mới
        mContext = this;
        mAuth = FirebaseAuth.getInstance();

        addControls();
        addEvents();
    }

    private void addControls() {
        tvCountDown = findViewById(R.id.tvCountDown);
        tvOtpPhone = findViewById(R.id.tvOtpPhone);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        // Map các EditText (Đảm bảo ID trong layout XML khớp nhé)
        edt1 = findViewById(R.id.edtConfimNumber1);
        edt2 = findViewById(R.id.edtConfimNumber2);
        edt3 = findViewById(R.id.edtConfimNumber3);
        edt4 = findViewById(R.id.edtConfimNumber4);
        edt5 = findViewById(R.id.edtConfimNumber5);
        edt6 = findViewById(R.id.edtConfimNumber6);
        editTexts = new EditText[]{edt1, edt2, edt3, edt4, edt5, edt6};

        // Lấy dữ liệu từ PaymentActivity truyền sang
        Intent intent = getIntent();
        verificationId = intent.getStringExtra("verificationId");
        phoneNumber = intent.getStringExtra("phoneNumber");
        mResendToken = intent.getParcelableExtra("resendToken");

        // Hiển thị số điện thoại (Che bớt số)
        if (phoneNumber != null && phoneNumber.length() > 7) {
            String masked = phoneNumber.substring(0, 3) + "xxx" + phoneNumber.substring(phoneNumber.length() - 4);
            tvOtpPhone.setText(masked);
        } else {
            tvOtpPhone.setText(phoneNumber);
        }
    }

    private void addEvents() {
        startCountDownTimer(COUNT_DOWN_TIME);
        setupEditTexts();

        tvResendOtp.setOnClickListener(v -> resendVerificationCode());
    }

    // --- LOGIC QUAN TRỌNG NHẤT: XÁC THỰC MÃ ---
    private void verifyCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        // Dùng signInWithCredential để kiểm tra mã OTP có đúng với verificationId không
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // NẾU THÀNH CÔNG: Trả kết quả OK về PaymentActivity
                        onVerificationSuccess();
                    } else {
                        // NẾU THẤT BẠI: Xử lý đếm số lần sai
                        handleVerificationFailed();
                    }
                });
    }

    private void onVerificationSuccess() {
        if (countDownTimer != null) countDownTimer.cancel();

        Toast.makeText(mContext, "Xác thực thanh toán thành công!", Toast.LENGTH_SHORT).show();

        // Trả về kết quả OK cho PaymentActivity
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish(); // Đóng Activity này ngay lập tức
    }

    private void handleVerificationFailed() {
        failedAttemptCount++;
        int remaining = MAX_FAILED_ATTEMPTS - failedAttemptCount;

        if (remaining <= 0) {
            Toast.makeText(mContext, "Bạn đã nhập sai quá nhiều lần. Giao dịch bị hủy.", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED); // Trả về hủy
            finish();
        } else {
            Toast.makeText(mContext, "Mã OTP sai. Còn " + remaining + " lần thử.", Toast.LENGTH_SHORT).show();
            clearInputs();
        }
    }

    // --- CÁC HÀM HỖ TRỢ UI (Giống hệt file cũ) ---

    private void startCountDownTimer(int initialTime) {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(initialTime * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvCountDown.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountDown.setText("00:00");
                tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

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
                    }
                    if (isOtpFull()) {
                        verifyCode(getOtpString());
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Xử lý nút xóa lùi (Backspace)
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

    private boolean isOtpFull() {
        for (EditText edt : editTexts) {
            if (edt.getText().toString().trim().isEmpty()) return false;
        }
        return true;
    }

    private String getOtpString() {
        StringBuilder sb = new StringBuilder();
        for (EditText edt : editTexts) sb.append(edt.getText().toString());
        return sb.toString();
    }

    private void clearInputs() {
        for (EditText edt : editTexts) edt.setText("");
        editTexts[0].requestFocus();
    }

    private void resendVerificationCode() {
        if (phoneNumber == null) return;
        Toast.makeText(mContext, "Đang gửi lại mã...", Toast.LENGTH_SHORT).show();
        tvResendOtp.setVisibility(View.GONE);

        String cleanPhone = phoneNumber.startsWith("0") ? "+84" + phoneNumber.substring(1) : phoneNumber;

        PhoneAuthOptions.Builder options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(cleanPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {}

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(mContext, "Lỗi gửi mã: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvResendOtp.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        mResendToken = token;
                        Toast.makeText(mContext, "Đã gửi lại OTP!", Toast.LENGTH_SHORT).show();
                        startCountDownTimer(COUNT_DOWN_TIME);
                        clearInputs();
                    }
                });

        if (mResendToken != null) options.setForceResendingToken(mResendToken);
        PhoneAuthProvider.verifyPhoneNumber(options.build());
    }
}