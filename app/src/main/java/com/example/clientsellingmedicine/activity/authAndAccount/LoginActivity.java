package com.example.clientsellingmedicine.activity.authAndAccount;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.GoogleToken;
import com.example.clientsellingmedicine.DTO.UserLogin;
import com.example.clientsellingmedicine.DTO.Token;
// Import các class cho Key Exchange
import com.example.clientsellingmedicine.DTO.ExchangeKeyRequest;
import com.example.clientsellingmedicine.DTO.ExchangeKeyResponse;
import com.example.clientsellingmedicine.api.AuthAPI;
import com.example.clientsellingmedicine.utils.CryptoManager;

import com.example.clientsellingmedicine.activity.MainActivity;
import com.example.clientsellingmedicine.api.LoginAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private Context mContext;

    TextInputEditText edt_phone_number, edt_password;
    ImageView iv_back;
    Button btn_login, btn_google_signin;
    TextView tvRegister;

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.login_screen);

        addControl();
        addEvents();

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    LoginWithGoogle(new GoogleToken(idToken));
                } else {
                    showErrorDialog("Đăng nhập thất bại", "Vui lòng kiểm tra lại tài khoản Google !");
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        });
    }

    private void addControl() {
        edt_phone_number = findViewById(R.id.edt_phone_number);
        edt_password = findViewById(R.id.edt_password);
        btn_login = findViewById(R.id.btn_login);
        btn_google_signin = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.tvRegister);
        iv_back = findViewById(R.id.iv_back);
    }

    private void addEvents() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                btn_login.setEnabled(!edt_phone_number.getText().toString().trim().isEmpty() &&
                        !edt_password.getText().toString().trim().isEmpty());
            }
        };
        edt_phone_number.addTextChangedListener(textWatcher);
        edt_password.addTextChangedListener(textWatcher);

        btn_login.setOnClickListener(view -> {
            UserLogin userLogin = new UserLogin(edt_phone_number.getText().toString(), edt_password.getText().toString());
            Login(userLogin);
        });

        tvRegister.setOnClickListener(view -> {
            Intent i = new Intent(mContext, RegisterActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        iv_back.setOnClickListener(view -> finish());

        btn_google_signin.setOnClickListener(v -> oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(LoginActivity.this, result -> {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(LoginActivity.this, e -> {
                    Log.d("TAG", e.getLocalizedMessage());
                }));
    }

    // =========================================================================
    //  QUY TRÌNH ĐĂNG NHẬP MỚI:
    //  Login (lấy Token) -> Key Exchange (lấy AES Key) -> Get User (Lấy Role) -> Navigate
    // =========================================================================

    public void Login(UserLogin userLogin) {
        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> request = loginAPI.login(userLogin);
        request.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    // 1. Lưu JWT Token
                    Token token = response.body();
                    SharedPref.saveToken(mContext, Constants.TOKEN_PREFS_NAME, Constants.KEY_TOKEN, token);

                    // 2. Thực hiện Exchange Key bảo mật
                    performKeyExchangeAndNavigate();

                } else if (response.code() / 100 == 4) {
                    showErrorDialog("Sai thông tin đăng nhập", "Vui lòng kiểm tra lại thông tin !");
                } else {
                    Toast.makeText(mContext, "Somethings was wrong!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Toast.makeText(mContext, "Connection error", Toast.LENGTH_LONG).show();
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
                    // 1. Lưu JWT Token
                    Token token = response.body();
                    SharedPref.saveToken(mContext, Constants.TOKEN_PREFS_NAME, Constants.KEY_TOKEN, token);

                    // 2. Thực hiện Exchange Key bảo mật (Trước đây Google Login bỏ qua bước check role, giờ ta nên thêm vào)
                    performKeyExchangeAndNavigate();

                } else {
                    Toast.makeText(mContext, "Đăng nhập Google thất bại!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Toast.makeText(mContext, "Connection error", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- HÀM THỰC HIỆN KEY EXCHANGE ---
    private void performKeyExchangeAndNavigate() {
        CryptoManager cryptoManager = new CryptoManager(mContext);

        // 1. Tạo RSA Public Key
        String publicKey = cryptoManager.generateAndGetRSAPublicKey();

        if (publicKey == null) {
            // Nếu lỗi tạo key, vẫn cho đi tiếp nhưng tính năng bảo mật sẽ không chạy
            Toast.makeText(mContext, "Warning: Security Key generation failed", Toast.LENGTH_SHORT).show();
            fetchUserInfoAndNavigate();
            return;
        }

        // 2. Gửi Public Key lên Server
        AuthAPI authAPI = ServiceBuilder.buildService(AuthAPI.class);
        ExchangeKeyRequest req = new ExchangeKeyRequest();
        req.publicKey = publicKey;

        authAPI.exchangeKey(req).enqueue(new Callback<ExchangeKeyResponse>() {
            @Override
            public void onResponse(Call<ExchangeKeyResponse> call, Response<ExchangeKeyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 3. Nhận AES Key (đã mã hóa) từ Server -> Giải mã & Lưu
                    boolean success = cryptoManager.decryptAndSaveServerKey(response.body().encryptedKey);
                    if (!success) {
                        Log.e("Security", "Failed to save AES key");
                    }
                } else {
                    Log.e("Security", "Exchange Key API failed: " + response.code());
                }

                // Dù thành công hay thất bại Key Exchange, vẫn cho user vào App
                // (Hoặc bạn có thể chặn lại nếu yêu cầu bảo mật cao)
                fetchUserInfoAndNavigate();
            }

            @Override
            public void onFailure(Call<ExchangeKeyResponse> call, Throwable t) {
                Log.e("Security", "Exchange Key Network Error");
                fetchUserInfoAndNavigate();
            }
        });
    }

    // --- HÀM LẤY INFO USER & ĐIỀU HƯỚNG ---
    private void fetchUserInfoAndNavigate() {
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> callUser = userAPI.getUser();

        callUser.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDTO user = response.body();

                    // Lưu info user
                    SharedPref.saveUser(LoginActivity.this, Constants.USER_PREFS_NAME, Constants.KEY_USER, user);

                    // Điều hướng theo Role
                    if ("admin".equalsIgnoreCase(user.getRole())) {
                        startActivity(new Intent(LoginActivity.this, AdminProductActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                } else {
                    // Fallback mặc định
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                // Fallback mặc định
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    // Helper dialog
    private void showErrorDialog(String title, String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setIcon(R.drawable.ic_warning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {})
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}