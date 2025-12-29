package com.example.clientsellingmedicine.activity.authAndAccount;

import android.view.KeyEvent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.DTO.UserLogin;
import com.example.clientsellingmedicine.DTO.UserRegister;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.MainActivity;
import com.example.clientsellingmedicine.api.LoginAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpConfirmActivity extends AppCompatActivity {
    private Context mContext;
    private TextView tvCountDown, tvOtpPhone, tvResendOtp;
    private EditText edt1, edt2, edt3, edt4, edt5, edt6;
    private EditText[] editTexts = {edt1, edt2, edt3, edt4, edt5, edt6};
    private final Integer COUNT_DOWN_TIME = 60;
    private String verificationId, phoneNumber, password, confirmPassword;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private int failedAttemptCount = 0;
    private final int MAX_FAILED_ATTEMPTS = 3; // Giới hạn cho phép sai 3 lần

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_confirm_screen);
        mContext = this;
        mAuth = FirebaseAuth.getInstance(); //Khởi tạo mAuth
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
        editTexts[0] = edt1;
        editTexts[1] = edt2;
        editTexts[2] = edt3;
        editTexts[3] = edt4;
        editTexts[4] = edt5;
        editTexts[5] = edt6;

        verificationId = getIntent().getStringExtra("verificationId");
        password = getIntent().getStringExtra("password");
        confirmPassword = getIntent().getStringExtra("confirmPassword");
        mResendToken = getIntent().getParcelableExtra("resendToken");

        // Lấy nguyên số điện thoại từ màn hình trước (ví dụ: 0987654321)
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        // -----------------------

        // Logic che số điện thoại để hiển thị (Masking)
        if (phoneNumber != null && phoneNumber.length() > 7) {
            String firstThreeDigits = phoneNumber.substring(0, 3);
            String lastFourDigits = phoneNumber.substring(phoneNumber.length() - 4);
            String maskedPhoneNumber = firstThreeDigits + "xxx" + lastFourDigits;
            tvOtpPhone.setText(maskedPhoneNumber);
        } else {
            tvOtpPhone.setText(phoneNumber);
        }

    }

    private void addEvents() {
        startCountDownTimer(COUNT_DOWN_TIME);
        setupEditTexts();

        tvResendOtp.setOnClickListener(v -> {
            resendVerificationCode();
        });
    }

    private void verifyCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp); //xác thực giữa otp và ID từ firebase
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        UserRegister user = new UserRegister(phoneNumber, password, confirmPassword);
                        registerUser(user);

                    } else {
                        failedAttemptCount++; // Tăng số lần sai
                        int remainingAttempts = MAX_FAILED_ATTEMPTS - failedAttemptCount;

                        if (remainingAttempts <= 0) {
                            // --- VƯỢT QUÁ GIỚI HẠN -> CHẶN ---
                            showLimitReachedDialog();
                        } else {
                            // --- CHƯA VƯỢT QUÁ -> CẢNH BÁO ---
                            String msg = "Mã OTP không đúng. Bạn còn " + remainingAttempts + " lần thử.";
                            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();

                            // Xóa ô nhập để nhập lại
                            for (EditText editText : editTexts) {
                                editText.setText("");
                            }
                            editTexts[0].requestFocus();
                        }
                    }
                });
    }


    private void startCountDownTimer(int initialTime) {
        countDownTimer = new CountDownTimer(initialTime * 1000L, 1000L) { // Convert seconds to milliseconds

            @Override
            public void onTick(long millisUntilFinished) {
                long remainingSeconds = millisUntilFinished / 1000;
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;

                // Format the time as "mm:ss"
                String formattedTime = String.format("%02d:%02d", minutes, seconds);
                tvCountDown.setText(formattedTime);
            }

            @Override
            public void onFinish() {
                tvCountDown.setText("00:00");
                tvResendOtp.setVisibility(View.VISIBLE); // Hiện nút gửi lại
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
                    } else if (s.length() == 0 && index > 0) {
                        editTexts[index - 1].requestFocus();
                    }
                    checkAndVerify(); // Nhập đủ otp sẽ tự
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
        if (otp.length() == editTexts.length) {
            verifyCode(otp.toString());
        }
    }

    private void registerUser(UserRegister userRegister){
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<ResponseDto> request = userAPI.registerUser(userRegister);
        request.enqueue(new Callback<ResponseDto>() {

            @Override
            public void onResponse(Call<ResponseDto> call, Response<ResponseDto> response) {
                if (response.isSuccessful()) {
                    if(response.body().getStatus() == 201){
                        handleRegisterSuccess();
                    }
                    else
                        Toast.makeText(mContext, "Đăng ký thất bại, đã có lỗi xảy ra. Vui lòng thử lại sau ít phút !", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Đăng ký thất bại, đã có lỗi xảy ra. Vui lòng thử lại sau ít phút !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDto> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void handleRegisterSuccess(){
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Stop the countdown timer
        }

        Toast.makeText(mContext, "Đăng ký thành công! Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        // Gọi hàm tự động đăng nhập ngay lập tức
        performAutoLogin();
    }

    private void performAutoLogin() {
        // Tạo đối tượng UserLogin từ thông tin đã có sẵn trong Activity này
        // Lưu ý: phoneNumber ở đây đã được xử lý thành dạng 0xxxx (đã gán ở onCreate)
        UserLogin userLogin = new UserLogin(phoneNumber, password);

        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> request = loginAPI.login(userLogin);

        request.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lưu Token
                    Token token = response.body();
                    EncryptedSharedPrefManager.saveToken(mContext, token);

                    // 2. Gọi tiếp API lấy thông tin User để lưu vào SharedPref (quan trọng cho các màn sau)
                    fetchUserAndNavigate();
                } else {
                    // Trường hợp hiếm: Đăng ký xong nhưng Login lỗi -> Đẩy về màn Login thủ công
                    navigateToLoginManual("Đăng ký thành công nhưng đăng nhập tự động thất bại. Vui lòng đăng nhập lại.");
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                navigateToLoginManual("Lỗi kết nối khi đăng nhập tự động.");
            }
        });
    }

    private void fetchUserAndNavigate() {
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> callUser = userAPI.getUser();

        callUser.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDTO user = response.body();
                    // 3. Lưu thông tin User
                    EncryptedSharedPrefManager.saveUser(mContext, user);

                    // 4. Chuyển thẳng vào MainActivity
                    Intent intent = new Intent(mContext, MainActivity.class);
                    // Xóa các activity trước đó để user không back lại màn OTP/Register được
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Lấy user thất bại, nhưng đã có Token -> Vẫn cho vào Main (có thể tải lại user sau)
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                // Lỗi mạng khi lấy user -> Vẫn vào Main
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    // Hàm phụ trợ để fallback về màn Login nếu tự động đăng nhập lỗi
    private void navigateToLoginManual(String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setIcon(R.drawable.ic_warning)
                .setTitle("Thông báo")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Do nothing
                })
                .setOnDismissListener(dialog -> {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void resendVerificationCode() {
        // 1. Kiểm tra an toàn: Nếu null hoặc rỗng thì dừng ngay tránh crash
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(mContext, "Lỗi: Không tìm thấy số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(mContext, "Đang gửi lại mã...", Toast.LENGTH_SHORT).show();
        tvResendOtp.setVisibility(View.GONE);

        // 2. Xử lý chuẩn hóa số điện thoại (Logic đa năng)
        String cleanPhone = phoneNumber.trim(); // Xóa khoảng trắng thừa
        String internationalPhoneNumber;

        if (cleanPhone.startsWith("0")) {
            // Trường hợp 09xx -> +849xx
            internationalPhoneNumber = "+84" + cleanPhone.substring(1);
        } else if (cleanPhone.startsWith("+84")) {
            // Trường hợp đã chuẩn +84 -> Giữ nguyên
            internationalPhoneNumber = cleanPhone;
        } else {
            // Trường hợp thiếu số 0 (ví dụ 987...) -> Thêm +84 vào đầu
            internationalPhoneNumber = "+84" + cleanPhone;
        }

        // [DEBUG] In ra log để xem chính xác số gửi đi là gì (Quan trọng để check lỗi)
        android.util.Log.d("OTP_CHECK", "Số gốc: " + phoneNumber + " | Số gửi đi: " + internationalPhoneNumber);
        // Chuyển sang dùng Builder để có thể setForceResendingToken
        PhoneAuthOptions.Builder optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(internationalPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Tự động điền nếu cần
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(mContext, "Gửi lại mã thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        tvResendOtp.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Cập nhật verificationId mới
                        verificationId = newVerificationId;

                        // --- CẬP NHẬT TOKEN MỚI CHO LẦN SAU ---
                        mResendToken = token;
                        // ---------------------------------------

                        Toast.makeText(mContext, "Đã gửi lại mã OTP!", Toast.LENGTH_SHORT).show();

                        // Reset ô nhập liệu
                        for (EditText edt : editTexts) {
                            edt.setText("");
                        }
                        editTexts[0].requestFocus();

                        startCountDownTimer(COUNT_DOWN_TIME);
                    }
                });

        // --- QUAN TRỌNG: GẮN TOKEN VÀO REQUEST ---
        if (mResendToken != null) {
            optionsBuilder.setForceResendingToken(mResendToken);
        }
        // -----------------------------------------

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build());
    }

    private void showLimitReachedDialog() {
        new MaterialAlertDialogBuilder(mContext)
                .setTitle("Cảnh báo bảo mật")
                .setMessage("Bạn đã nhập sai mã xác thực quá 3 lần. Vì lý do bảo mật, vui lòng thực hiện lại thao tác đăng ký.")
                .setCancelable(false) // Không cho bấm ra ngoài
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Chuyển về màn hình Đăng ký hoặc Login tùy bạn
                    // Ở đây mình cho finish() để quay lại màn hình trước đó (Register)
                    finish();
                })
                .show();
    }
}


