package com.example.clientsellingmedicine.activity.authAndAccount;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.activity.adress.RegisteredAddressActivity;
import com.example.clientsellingmedicine.api.LogoutAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.BiometricHelper;
import com.example.clientsellingmedicine.utils.Constants;
import com.example.clientsellingmedicine.utils.EncryptedSharedPrefManager;
import com.example.clientsellingmedicine.utils.SharedPref;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private Context mContext;
    private ImageView iv_Avatar;
    private TextView tv_UserName,tv_Rank,tv_Logout;

    private ProgressBar progress_Point;

    private LinearLayout ll_AddressBook,ll_EditProfile;

    private SwitchCompat sw_biometric;


    private static UserDTO user = new UserDTO();
    public ProfileFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_login_screen, container, false);
        mContext = view.getContext();
        // Thực hiện các thao tác cần thiết trên giao diện view của fragment
        addControl(view);
        addEvents();
        return view;


    }



    private void addControl(View view){
        iv_Avatar = view.findViewById(R.id.iv_Avatar);
        tv_UserName = view.findViewById(R.id.tv_UserName);
        tv_Rank = view.findViewById(R.id.tv_Rank);
        progress_Point = view.findViewById(R.id.progress_Point);
        tv_Logout = view.findViewById(R.id.tv_Logout);

        ll_AddressBook = view.findViewById(R.id.ll_AddressBook);
        ll_EditProfile = view.findViewById(R.id.ll_EditProfile);
        sw_biometric = view.findViewById(R.id.sw_biometric);
    }
    private void addEvents(){
        // this layout is used to show the list of registered addresses
        ll_AddressBook.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RegisteredAddressActivity.class);
            startActivity(intent);
        });

        // this layout is used to edit the user's profile
        ll_EditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, IndividualActivity.class);
            startActivity(intent);
        });

        // logout button
        tv_Logout.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setIcon(R.drawable.drug) // Đặt icon của Dialog
                    .setTitle("Xác Nhận Đăng Xuất")
                    .setMessage("Bạn có muốn đăng xuất khỏi ứng dụng không?")
                    .setCancelable(false) // Bấm ra ngoài không mất dialog

                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Xử lý khi nhấn nút OK
                        Logout();
                    })

                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // Xử lý khi nhấn nút Cancel
                    })
                    .show();
        });

        // --- SỰ KIỆN MỚI CHO VÂN TAY ---
        // 1. Set trạng thái ban đầu cho Switch dựa trên SharedPref
        boolean isEnabled = SharedPref.getBoolean(mContext, Constants.BIOMETRIC_PREFS_NAME, Constants.KEY_BIOMETRIC_ENABLED, false);
        sw_biometric.setChecked(isEnabled);

        // 2. Xử lý khi click vào Switch
        sw_biometric.setOnClickListener(view -> {
            boolean isChecked = sw_biometric.isChecked();

            if (isChecked) {
                // Nếu người dùng muốn BẬT: Cần xác thực vân tay trước để đảm bảo chính chủ
                BiometricHelper.authenticate(ProfileFragment.this, new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                        // Xác thực thành công -> Lưu trạng thái BẬT
                        SharedPref.saveBoolean(mContext, Constants.BIOMETRIC_PREFS_NAME, Constants.KEY_BIOMETRIC_ENABLED, true);
                        Toast.makeText(mContext, "Đã bật đăng nhập bằng vân tay", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        // Thất bại -> Trả Switch về trạng thái tắt
                        sw_biometric.setChecked(false);
                    }
                });
            } else {
                // Nếu người dùng muốn TẮT: Tắt luôn không cần hỏi
                SharedPref.saveBoolean(mContext, Constants.BIOMETRIC_PREFS_NAME, Constants.KEY_BIOMETRIC_ENABLED, false);
                Toast.makeText(mContext, "Đã tắt đăng nhập bằng vân tay", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUserLogin() {
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> request = userAPI.getUser();
        request.enqueue(new Callback<UserDTO>() {

            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    user = response.body();
                    if(user != null){
                        if(user.getUsername() != null ){
                            tv_UserName.setText(user.getUsername());
                        }else if(user.getPhone() != null) {
                            tv_UserName.setText(user.getPhone());
                        }else {
                            tv_UserName.setText(user.getEmail());
                        }

                        progress_Point.setProgress(user.getPoint());
                        tv_Rank.setText(user.getRank());
                        Glide.with(mContext)
                                .load(user.getImage())
                                .placeholder(R.drawable.ic_profile_user) // Hình ảnh thay thế khi đang tải
                                .error(R.drawable.ic_profile_user) // Hình ảnh thay thế khi có lỗi
                                .circleCrop()
                                .into(iv_Avatar);

                        // 2. Lưu user mã hóa (trong đó có role) để chỗ khác dùng nếu cần
                        EncryptedSharedPrefManager.saveUser(mContext, user);

                    }
                } else if (response.code() == 401) {
                    navigateToLogin();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items (response)", Toast.LENGTH_LONG).show();
                }
            }

            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Log.d("TAG", "onFailure: " + t.getMessage());
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void Logout() {
        // 1. Kiểm tra xem có đang bật vân tay không
        boolean isBiometricEnabled = SharedPref.getBoolean(mContext, Constants.BIOMETRIC_PREFS_NAME, Constants.KEY_BIOMETRIC_ENABLED, false);

        if (isBiometricEnabled) {
            // === TRƯỜNG HỢP 1: CÓ VÂN TAY (Soft Logout) ===
            // KHÔNG GỌI API LOGOUT (Để Refresh Token trên server vẫn sống)

            // a. Xóa User Info (Để app biết là đã thoát)
            EncryptedSharedPrefManager.removeUser(mContext);

            // b. Xóa checkbox giỏ hàng
            SharedPref.removeData(mContext, Constants.CART_PREFS_NAME, Constants.KEY_CART_ITEMS_CHECKED);

            // c. Quan trọng: Làm hỏng Access Token hiện tại, nhưng giữ Refresh Token
            // Để lần sau vào LoginActivity bắt buộc phải refresh lại
            com.example.clientsellingmedicine.DTO.Token token = EncryptedSharedPrefManager.loadToken(mContext);
            if (token != null) {
                token.setToken(null); // Xóa Access Token
                // token.getRefreshToken() vẫn còn nguyên
                EncryptedSharedPrefManager.saveToken(mContext, token);
            }

            // d. Chuyển màn hình
            navigateToLogin();

        } else {
            // === TRƯỜNG HỢP 2: KHÔNG VÂN TAY (Hard Logout) ===
            // Gọi API để server xóa token trong DB
            LogoutAPI logoutAPI = ServiceBuilder.buildService(LogoutAPI.class);
            Call<ResponseDto> request = logoutAPI.logout();
            request.enqueue(new Callback<ResponseDto>() {
                @Override
                public void onResponse(Call<ResponseDto> call, Response<ResponseDto> response) {
                    // Dù thành công hay thất bại cũng xóa sạch
                    performFullLogout();
                }

                @Override
                public void onFailure(Call<ResponseDto> call, Throwable t) {
                    performFullLogout();
                }
            });
        }
    }

    // Hàm phụ xóa sạch sẽ
    private void performFullLogout() {
        EncryptedSharedPrefManager.clearAll(mContext);
        SharedPref.removeData(mContext, Constants.CART_PREFS_NAME, Constants.KEY_CART_ITEMS_CHECKED);
        navigateToLogin();
    }
    public void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume () {
        super.onResume();
        getUserLogin();
    }

}
