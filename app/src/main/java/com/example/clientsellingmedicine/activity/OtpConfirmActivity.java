package com.example.clientsellingmedicine.activity;

import android.view.KeyEvent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.DTO.UserRegister;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpConfirmActivity extends AppCompatActivity {
    private Context mContext;
    private TextView tvCountDown, tvOtpPhone;
    private EditText edt1, edt2, edt3, edt4, edt5, edt6;
    private EditText[] editTexts = {edt1, edt2, edt3, edt4, edt5, edt6};
    private final Integer COUNT_DOWN_TIME = 90;
    private String verificationId, phoneNumber, password, confirmPassword;

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_confirm_screen);
        mContext = this;

        addControls();
        addEvents();

    }

    private void addControls() {
        tvCountDown = findViewById(R.id.tvCountDown);
        tvOtpPhone = findViewById(R.id.tvOtpPhone);
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
        phoneNumber = "0" + getIntent().getStringExtra("phoneNumber").substring(3); //convert international phoneNumber to VN phone number:  +84xxxxx to 0xxxxxx
        password = getIntent().getStringExtra("password");
        confirmPassword = getIntent().getStringExtra("confirmPassword");

        String firstThreeDigits = phoneNumber.substring(0,3);
        String lastFourDigits = phoneNumber.substring(phoneNumber.length() - 4);
        // Replace the remaining digits with "xxx"
        String maskedPhoneNumber =  firstThreeDigits+"xxx"+lastFourDigits;
        // Set the masked phone number on the TextView
        tvOtpPhone.setText(maskedPhoneNumber); //example: 096xxx0128

    }

    private void addEvents() {
        startCountDownTimer(COUNT_DOWN_TIME);
        setupEditTexts();
    }

    private void verifyCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        UserRegister user = new UserRegister(phoneNumber, password, confirmPassword);
                        registerUser(user);

                    } else {
                        Toast.makeText(mContext, "Mã OTP không đúng, vui lòng thử lại !", Toast.LENGTH_LONG).show();
                        for (EditText editText : editTexts) {
                            editText.setText("");
                        }
                        editTexts[0].requestFocus();
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
                finish(); // Finish activity when countdown reaches 0
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

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setIcon(R.drawable.successfully)
                .setTitle("Đăng ký thành công")
                .setMessage("Chúc mừng bạn đã đăng ký tài khoản Medimate thành công.\nHãy tiến hành đăng nhập vào Medimate và bắt đầu mua sắm nào !")
                //.setCancelable(false) // Bấm ra ngoài không mất dialog
                .setPositiveButton("OK", (dialog, which) -> {
                    // Do nothing here; action handled in onDismiss
                })
                .setOnDismissListener(dialog -> {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                })
                .show();
    }
}


