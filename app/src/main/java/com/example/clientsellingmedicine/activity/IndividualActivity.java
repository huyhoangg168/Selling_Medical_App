package com.example.clientsellingmedicine.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.Convert;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndividualActivity extends AppCompatActivity {
    private Context mContext;
    private TextView tvName, tvPhone, tvGender, tvBirthday, tvUserID;
    private Button btnEditProfile;
    private ImageView ivAvatar;
    private static UserDTO user = new UserDTO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.individual_screen);

        addControl();
        addEvents();

    }

    private void addControl() {
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvGender = findViewById(R.id.tvGender);
        tvBirthday = findViewById(R.id.tvBirthday);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserID = findViewById(R.id.tvUserID);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void addEvents() {
        // Chuyển sang màn hình chỉnh sửa thông tin cá nhân
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, EditProfileActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });
        // Lấy thông tin cá nhân
        getUserLogin();
    }

    public void getUserLogin() {
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> request = userAPI.getUser();
        request.enqueue(new Callback<UserDTO>() {

            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    user = response.body();
                    if (user != null) {
                        tvUserID.setText("user#" + user.getId());
                        tvName.setText(user.getUsername() != null ? user.getUsername() : "Unknown");
                        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Unknown");
                        tvGender.setText(user.getGender() == 1 ? "Nam" : "Nữ");
                        String birthday = (user.getBirthday() != null ? Convert.convertToDate(user.getBirthday().toString()) : null);
                        tvBirthday.setText(birthday != null ? birthday : "Unknown");

                        Glide.with(mContext)
                                .load(user.getImage())
                                .placeholder(R.drawable.ic_avartar) // Hình ảnh thay thế khi đang tải
                                .error(R.drawable.ic_avartar) // Hình ảnh thay thế khi có lỗi
                                .circleCrop()
                                .into(ivAvatar);
                    }
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
    @Override
    protected void onResume () {
        super.onResume();
        getUserLogin();
    }
}
