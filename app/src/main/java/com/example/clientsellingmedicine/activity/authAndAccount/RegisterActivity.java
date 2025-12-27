package com.example.clientsellingmedicine.activity.authAndAccount;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.activity.MainActivity;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.LoadingManager;
import com.airbnb.lottie.LottieAnimationView;
import com.example.clientsellingmedicine.DTO.GoogleToken;
import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.interfaces.PhoneNumberCheckCallback;
import com.example.clientsellingmedicine.api.LoginAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.example.clientsellingmedicine.utils.Validator;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RegisterActivity extends AppCompatActivity {
    private Context mContext;
    private FirebaseAuth mAuth;
    private TextInputEditText tvConfirmPassword, tvPassword, tvPhone;
    private CheckBox cbPolicy;
    private Button btnRegister, btn_google_signin_register;

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;

    private LottieAnimationView lottieAnimationView;

    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.register_screen);

        addControl();
        addEvents();
        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        //login with google
        oneTapClient = Identity.getSignInClient(this); //ggonetap khởi tạo
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id)) // gg client ID
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                String idToken = credential.getGoogleIdToken(); // GG trả về token GG
                if (idToken != null) {
                    LoginWithGoogle(new GoogleToken(idToken)); //GỬi token lên BE
                } else {
                    displayAlertDialog("Đăng nhập thất bại","Vui lòng kiểm tra lại tài khoản đăng nhập");
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        });
    }

    private void addControl() {
        cbPolicy = findViewById(R.id.cbPolicy);
        tvConfirmPassword = findViewById(R.id.tvConfirmPassword);
        tvPassword = findViewById(R.id.tvPassword);
        tvPhone = findViewById(R.id.tvPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btn_google_signin_register = findViewById(R.id.btn_google_signin_register);
    }

    private void addEvents() {

        cbPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked);
        });

        btnRegister.setOnClickListener(v -> {

            String phoneNumber = tvPhone.getText().toString();
            String internationalPhoneNumber = "+84" + phoneNumber.toString().substring(1);

            Boolean isInputCorrect = validateFields(); //validate input
            if(!isInputCorrect)
                return;

           // lottieAnimationView.playAnimation();
            LoadingManager.showLoading(mContext);

            checkPhoneNumberAlreadyExists(phoneNumber, new PhoneNumberCheckCallback() {
                @Override
                public void onSuccess(boolean isExists) {
                    if (isExists) {
                        displayAlertDialog("Thông báo","Số điện thoại này đã tồn tại. Vui lòng sử dụng một số điện thoại khác để đăng ký !");
                        LoadingManager.hideLoading();
                    } else {
                        sendVerificationCode(internationalPhoneNumber);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle error
                    Toast.makeText(mContext, "Đã có lỗi xảy ra, vui lòng thử lại sau ít phút !", Toast.LENGTH_LONG).show();
                    Log.d("tag", "onError: "+errorMessage);
                    LoadingManager.hideLoading();
                }
            });

        });

        btn_google_signin_register.setOnClickListener(v -> oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(RegisterActivity.this, result -> {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(RegisterActivity.this, e -> {
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Log.d("TAG", e.getLocalizedMessage());
                }));
    }

    private void sendVerificationCode(String phoneNumber) {
        //FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(90L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // (optional) activity for callback
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        //Xác thực tự động
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // Handle error
                        Log.d("tag", "onVerificationFailed: " + e.getMessage());
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                        LoadingManager.hideLoading();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        LoadingManager.hideLoading();
                        // Save the verification ID and resending token
                        Intent intent = new Intent(mContext, OtpConfirmActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("password", tvPassword.getText().toString().trim());
                        intent.putExtra("confirmPassword", tvConfirmPassword.getText().toString().trim());
                        startActivity(intent);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options); // hàm gửi otp
    }

    private void checkPhoneNumberAlreadyExists(String phoneNumber, PhoneNumberCheckCallback callback) {
        // Convert phone number to JSON
        String phoneNumberJson = "{\"phoneNumber\": \"" + phoneNumber + "\"}";
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(phoneNumberJson, JsonObject.class);

        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<Boolean> request = userAPI.checkPhoneNumber(jsonObject);

        request.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    Boolean isExists = response.body();
                    if (isExists != null) {
                        // Notify callback of the result
                        callback.onSuccess(isExists);

                    } else {
                        callback.onError("Unexpected response from server");
                    }
                } else {
                    callback.onError("Server error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }


    private void LoginWithGoogle(GoogleToken googleToken) {
        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> request = loginAPI.loginWithGoogle(googleToken);
        request.enqueue(new Callback<Token>() {

            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    Token token = response.body();
                    EncryptedSharedPrefManager.saveToken(mContext, token);
                    Intent intent = new Intent(mContext, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Đăng nhập thất bại, đã có lỗi xảy ra !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void displayAlertDialog (String title, String content) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                .setTitle(title)
                .setMessage(content)
//                            .setCancelable(false) // Bấm ra ngoài không mất dialog
                .setPositiveButton("OK", (dialog, which) -> {
                    // Xử lý khi nhấn nút OK
                })
                .show();
    }

    private boolean validateFields(){
        String phone = tvPhone.getText().toString().trim();
        String password = tvPassword.getText().toString().trim();
        String confirmPassword = tvConfirmPassword.getText().toString().trim();

        if(phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            displayAlertDialog("Thông tin không hợp lệ","Vui lòng không bỏ trống thông tin !");
            return false;
        }

        if(!Validator.isValidPhoneNumber(phone)){
            displayAlertDialog("Thông tin không hợp lệ","Vui lòng điền đúng định dạng số điện thoại !");
            return false;
        }

        if(password.length() < 6 ){
            displayAlertDialog("Thông tin không hợp lệ","Mật khẩu phải có ít nhất 6 kí tự !");
            return false;
        }

        if(!password.equals(confirmPassword)){
            displayAlertDialog("Thông tin không hợp lệ","Mật khẩu và xác nhận mật khẩu không trùng khớp !");
            return false;
        }

        return true;
    }

}


