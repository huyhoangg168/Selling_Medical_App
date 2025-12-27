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

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private Context mContext;

    TextInputEditText edt_phone_number, edt_password;
    ImageView iv_back;
    Button btn_login,btn_google_signin;
    TextView tvRegister;
    ImageButton btn_fingerprint;

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    // Biến lưu trạng thái token để dùng lại khi click nút
    private String savedRefreshTokenForBiometric = null;
    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
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
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

         activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
             try {
                 SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                 String idToken = credential.getGoogleIdToken();
                 if (idToken !=  null) {
                     LoginWithGoogle(new GoogleToken(idToken));
                 } else {
                     MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
                     builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                             .setTitle("Đăng nhập thất bại")
                             .setMessage("Vui lòng kiểm tra lại tài khoản Google đã đăng nhập !")
//                            .setCancelable(false) // Bấm ra ngoài không mất dialog
                             .setPositiveButton("OK", (dialog, which) -> {
                                 // Xử lý khi nhấn nút OK
                             })
                             .show();
                 }
             } catch (ApiException e) {
                 e.printStackTrace();
             }
         });

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) { // Kiểm tra activity còn sống không
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Enable the button if both EditTexts have values
                btn_login.setEnabled(!edt_phone_number.getText().toString().trim().isEmpty() && !edt_password.getText().toString().trim().isEmpty());
            }
        };
        edt_phone_number.addTextChangedListener(textWatcher);
        edt_password.addTextChangedListener(textWatcher);
        btn_login.setOnClickListener(view -> {
            UserLogin userLogin = new UserLogin(edt_phone_number.getText().toString(), edt_password.getText().toString());
            //loginServiceImpl.login(userLogin);
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
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Log.d("TAG", e.getLocalizedMessage());
                }));

        btn_fingerprint.setOnClickListener(v -> {
            // Kiểm tra lại lần nữa cho chắc chắn
            if (savedRefreshTokenForBiometric != null) {
                showBiometricPrompt(savedRefreshTokenForBiometric);
            }
        });
    }


    public void Login(UserLogin userLogin) {
        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> request = loginAPI.login(userLogin);
        request.enqueue(new Callback<Token>() {

            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {

                    //save jwt token on client
                    Token token = response.body();
                    EncryptedSharedPrefManager.saveToken(LoginActivity.this, token);


                    // GỌI API LẤY USER ĐỂ BIẾT ROLE
                    UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
                    Call<UserDTO> callUser = userAPI.getUser();

                    callUser.enqueue(new Callback<UserDTO>() {
                        @Override
                        public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {

                                UserDTO user = response.body();
                                // (Tuỳ chọn) Lưu user lại
                                EncryptedSharedPrefManager.saveUser(LoginActivity.this, user);


                                // ✅ Rẽ nhánh theo role
                                if ("admin".equalsIgnoreCase(user.getRole())) {
                                    // ADMIN → chỉ vào dashboard
                                    startActivity(new Intent(LoginActivity.this, AdminProductActivity.class));
                                } else {
                                    // USER → vào app mua hàng như cũ
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                }

                                finish(); // đóng màn login

                            } else {
                                // fallback: nếu có lỗi, vẫn vào MainActivity như cũ
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserDTO> call, Throwable t) {
                            // fallback nếu lỗi mạng
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    });


                } else if (response.code() / 100 == 4) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
                    builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                            .setTitle("Sai thông tin đăng nhập")
                            .setMessage("Vui lòng kiểm tra lại thông tin và đăng nhập lại !")
//                            .setCancelable(false) // Bấm ra ngoài không mất dialog
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Xử lý khi nhấn nút OK

                                }
                            })
                            .show();
                } else {
                    Toast.makeText(mContext, "Somethings was wrong!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(mContext, "Somethings was wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void LoginWithGoogle(GoogleToken googleToken){
        LoginAPI loginAPI = ServiceBuilder.buildService(LoginAPI.class);
        Call<Token> request = loginAPI.loginWithGoogle(googleToken);
        request.enqueue(new Callback<Token>() { // chạy bất đồng bộ

            //Xử lý khi server thành công
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {

                    //save jwt token on client
                    Token token = response.body();
                    EncryptedSharedPrefManager.saveToken(LoginActivity.this, token);

                    //navigate
                    Intent intent = new Intent(mContext, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
               else {
                    Toast.makeText(mContext, "Đăng nhập thất bại, đã có lỗi xảy ra !", Toast.LENGTH_LONG).show();
                }
            }

            //Xử lý khi server thất bại
            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // --- [SỬA ĐỔI] LOGIC VÂN TAY ---

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
        // Tạo request body (nhớ import class RefreshTokenRequest bạn đã tạo)
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
