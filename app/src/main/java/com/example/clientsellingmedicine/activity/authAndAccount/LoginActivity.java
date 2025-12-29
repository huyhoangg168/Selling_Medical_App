package com.example.clientsellingmedicine.activity.authAndAccount;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

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
import com.example.clientsellingmedicine.utils.BiometricHelper;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.safetynet.SafetyNet; // (Có thể cần nếu dùng SafetyNet cũ, nhưng với code này thì dòng dưới quan trọng hơn)
import com.google.android.gms.recaptcha.Recaptcha;
import com.google.android.gms.recaptcha.RecaptchaAction;
import com.google.android.gms.recaptcha.RecaptchaHandle;
import com.google.android.gms.recaptcha.RecaptchaResultData;
import org.json.JSONObject;

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
    ImageButton btn_fingerprint;

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    
    // Biometric feature
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;
    private String savedRefreshTokenForBiometric = null;
    
    // Recaptcha
    private static final String RECAPTCHA_SITE_KEY = "6LeEYDgsAAAAANgtg14NwAM7LsnbYNdCIg1GMdMH";
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

        // Setup biometric feature after a short delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                setupBiometricFeature();
            }
        }, 300);
    }

    private void addControl() {
        edt_phone_number = findViewById(R.id.edt_phone_number);
        edt_password = findViewById(R.id.edt_password);
        btn_login = findViewById(R.id.btn_login);
        btn_google_signin = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.tvRegister);
        iv_back = findViewById(R.id.iv_back);
        btn_fingerprint = findViewById(R.id.btn_fingerprint);
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

        btn_fingerprint.setOnClickListener(v -> {
            // Kiểm tra lại lần nữa cho chắc chắn
            if (savedRefreshTokenForBiometric != null) {
                showBiometricPrompt(savedRefreshTokenForBiometric);
            }
        });
    }

    // =========================================================================
    //  QUY TRÌNH ĐĂNG NHẬP MỚI:
    //  Login (lấy Token) -> Key Exchange (lấy AES Key) -> Get User (Lấy Role) -> Navigate
    // =========================================================================

    public void Login(UserLogin userLogin) {
        // Hiển thị loading nếu cần (LoadingManager.showLoading(mContext));

        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> request = loginAPI.login(userLogin);
        request.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                // LoadingManager.hideLoading();

                if (response.isSuccessful()) {
                    // --- TRƯỜNG HỢP THÀNH CÔNG (200) ---
                    Token token = response.body();
                    
                    // Lưu token vào EncryptedSharedPreferences
                    EncryptedSharedPrefManager.saveToken(LoginActivity.this, token);
                    
                    // Thực hiện Key Exchange và điều hướng
                    performKeyExchangeAndNavigate();


                } else {
                    // --- XỬ LÝ CÁC MÃ LỖI ---
                    handleLoginErrors(response, userLogin);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // LoadingManager.hideLoading();
                Toast.makeText(mContext, "Lỗi kết nối server!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginErrors(Response<Token> response, UserLogin currentUserLogin) {
        try {
            int code = response.code();
            String errorMessage = "Đã có lỗi xảy ra";

            // Lấy message lỗi từ Server gửi về
            if (response.errorBody() != null) {
                JSONObject errorObj = new JSONObject(response.errorBody().string());
                if(errorObj.has("message")) {
                    errorMessage = errorObj.getString("message");
                }
            }

            switch (code) {
                case 403:
                    // [Backend yêu cầu Captcha] -> Gọi hàm hiện Captcha
                    Toast.makeText(mContext, "Đang xác thực bảo mật...", Toast.LENGTH_SHORT).show();
                    showCaptchaAndRetry(currentUserLogin);
                    break;

                case 429:
                    // [Bị Khóa do Spam hoặc DDoS] -> Hiện Dialog chặn
                    showErrorDialog("Tạm khóa tài khoản", errorMessage);
                    // Disable nút login nếu muốn
                    btn_login.setEnabled(false);
                    break;

                case 401:
                    // [Sai mật khẩu hoặc username]
                    showErrorDialog("Đăng nhập thất bại", errorMessage);
                    break;

                default:
                    showErrorDialog("Lỗi", errorMessage);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Lỗi", "Không thể xử lý phản hồi từ server");
        }
    }

    // --- HÀM XỬ LÝ CAPTCHA ---
    // --- HÀM XỬ LÝ CAPTCHA ---
    private void showCaptchaAndRetry(UserLogin userLogin) {
        // 1. Khởi tạo (Init)
        Recaptcha.getClient(this).init(RECAPTCHA_SITE_KEY)
                .addOnSuccessListener(this, handle -> {
                    Log.d("Captcha", "Init thành công, đang execute...");

                    Recaptcha.getClient(this).execute(handle, new RecaptchaAction("LOGIN"))
                            .addOnSuccessListener(this, response -> {
                                // LoadingManager.hideLoading();
                                Toast.makeText(mContext, "Xác thực thành công, đang đăng nhập lại...", Toast.LENGTH_SHORT).show();

                                String captchaToken = response.getTokenResult();
                                Log.d("Captcha", "Token nhận được: " + captchaToken);

                                // Gọi lại Login kèm token
                                UserLogin newLoginRequest = new UserLogin(
                                        userLogin.getPhone(),
                                        userLogin.getPassword(),
                                        captchaToken
                                );
                                Login(newLoginRequest);
                            })
                            .addOnFailureListener(this, e -> {
                                // LoadingManager.hideLoading();
                                Log.e("Captcha", "Execute thất bại", e);
                                Toast.makeText(mContext, "Lỗi Captcha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(mContext, "Khởi tạo Captcha lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Captcha", "Init Error", e);
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
                    EncryptedSharedPrefManager.saveToken(LoginActivity.this, token);

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

                    // Lưu info user vào EncryptedSharedPreferences
                    EncryptedSharedPrefManager.saveUser(LoginActivity.this, user);

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

    // =========================================================================
    // BIOMETRIC AUTHENTICATION FEATURE
    // =========================================================================

    private void setupBiometricFeature() {
        // 1. Kiểm tra Setting của User
        boolean isBiometricEnabled = SharedPref.getBoolean(mContext, Constants.BIOMETRIC_PREFS_NAME, Constants.KEY_BIOMETRIC_ENABLED, false);

        // 2. Kiểm tra Token trong máy
        Token token = EncryptedSharedPrefManager.loadToken(mContext);

        // Logic hiển thị nút
        if (isBiometricEnabled && token != null && token.getRefreshToken() != null) {
            // Đủ điều kiện: Hiện nút và lưu token tạm để dùng khi click
            btn_fingerprint.setVisibility(View.VISIBLE);
            savedRefreshTokenForBiometric = token.getRefreshToken();

            // Tự động bật Prompt ngay khi vào màn hình (Trải nghiệm tốt)
            showBiometricPrompt(savedRefreshTokenForBiometric);

        } else {
            // Không đủ điều kiện (Chưa bật setting hoặc chưa từng đăng nhập): Ẩn nút
            btn_fingerprint.setVisibility(View.GONE);
            savedRefreshTokenForBiometric = null;
        }
    }

    private void showBiometricPrompt(String refreshToken) {
        BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
            @Override
            public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                // Vân tay đúng -> Gọi API xin Token mới
                performBiometricLogin(refreshToken);
            }

            @Override
            public void onFailure() {
                // Người dùng bấm Hủy hoặc sai vân tay quá nhiều lần
                // Không làm gì cả, nút btn_fingerprint vẫn còn đó để họ bấm lại nếu muốn
            }
        });
    }

    private void performBiometricLogin(String refreshToken) {
        // Hiển thị loading nếu muốn...
        Toast.makeText(mContext, "Đang xác thực...", Toast.LENGTH_SHORT).show();

        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        // Tạo request body
        com.example.clientsellingmedicine.DTO.RefreshTokenRequest request =
                new com.example.clientsellingmedicine.DTO.RefreshTokenRequest(refreshToken);

        Call<Token> call = loginAPI.refreshToken(request);
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lấy Access Token Mới
                    String newAccessToken = response.body().getToken();

                    // 2. Ghép với Refresh Token cũ để lưu lại
                    Token newTokenToSave = new Token();
                    newTokenToSave.setToken(newAccessToken);
                    newTokenToSave.setRefreshToken(refreshToken); // Giữ cái cũ

                    EncryptedSharedPrefManager.saveToken(LoginActivity.this, newTokenToSave);

                    // 3. Gọi tiếp API lấy thông tin User để biết Role và vào màn hình chính
                    fetchUserAndNavigate();

                } else {
                    // Token hết hạn (30 ngày) hoặc Server lỗi
                    Toast.makeText(mContext, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
                    // Xóa token hỏng đi để lần sau không hỏi vân tay nữa
                    EncryptedSharedPrefManager.clearAll(mContext);

                    btn_fingerprint.setVisibility(View.GONE);
                    savedRefreshTokenForBiometric = null;
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Toast.makeText(mContext, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserAndNavigate() {
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        userAPI.getUser().enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDTO user = response.body();
                    EncryptedSharedPrefManager.saveUser(LoginActivity.this, user);

                    if ("admin".equalsIgnoreCase(user.getRole())) {
                        startActivity(new Intent(LoginActivity.this, AdminProductActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                } else {
                    // Lấy user thất bại -> Vẫn cho vào Main nhưng có thể lỗi hiển thị
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
