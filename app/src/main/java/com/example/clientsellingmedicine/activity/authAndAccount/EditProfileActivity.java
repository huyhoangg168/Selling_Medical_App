package com.example.clientsellingmedicine.activity.authAndAccount;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.example.clientsellingmedicine.utils.Convert;
import com.example.clientsellingmedicine.utils.LoadingManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private Context mContext;
    private ImageView ivAvatar, ivCalendar;
    private TextView tvChangeAvatar, tvDate;
    private EditText edtUserName, edtPhoneNumber;
    private RadioButton rdbMale, rdbFeMale;

    private Button btnUpdateInfo;

    private static int IMAGE_REQ = 1;
    private Uri imagePath;

    private String imageLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.individual_update_screen);

        addControl();
        addEvents();
    }

    private void addControl() {
        ivAvatar = findViewById(R.id.ivAvatar);
        ivCalendar = findViewById(R.id.ivCalendar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);
        tvDate = findViewById(R.id.tvDate);
        edtUserName = findViewById(R.id.edtUserName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        rdbMale = findViewById(R.id.rdbMale);
        rdbFeMale = findViewById(R.id.rdbFeMale);
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo);
    }

    private void addEvents() {
        tvChangeAvatar.setOnClickListener(v -> pickImage());
        ivCalendar.setOnClickListener(v -> handleSelectBirthday());
        btnUpdateInfo.setOnClickListener(v -> upLoadImage());
        loadInfo();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, IMAGE_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQ && resultCode == RESULT_OK && data != null) {
            imagePath = data.getData();
            // TODO: Xử lý ảnh đã chọn ở đây
            Glide.with(mContext)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_avartar) // Hình ảnh thay thế khi đang tải
                    .error(R.drawable.ic_avartar) // Hình ảnh thay thế khi có lỗi
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void loadInfo() {
        Intent intent = getIntent();
        UserDTO user = (UserDTO) intent.getSerializableExtra("user");
        if (user != null) {
            edtUserName.setText(user.getUsername() == null ? "" : user.getUsername());
            edtPhoneNumber.setText(user.getPhone());
            if (user.getGender() == 1) {
                rdbMale.setChecked(true);
            } else {
                rdbFeMale.setChecked(true);
            }
            String birthday = (user.getBirthday() != null ? Convert.convertToDate(user.getBirthday().toString()): null);
            tvDate.setText(birthday != null ? birthday : "");
            Glide.with(mContext)
                    .load(user.getImage())
                    .placeholder(R.drawable.ic_avartar) // Hình ảnh thay thế khi đang tải
                    .error(R.drawable.ic_avartar) // Hình ảnh thay thế khi có lỗi
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void handleSelectBirthday() {
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo một DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                (view, year1, month1, dayOfMonth1) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth1);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selectedDate.getTime());
                    tvDate.setText(formattedDate);
                }, year, month, dayOfMonth);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();
    }

    private void handleUpdateInfo() {
        // Xử lý cập nhật thông tin cá nhân
        UserDTO userUpdate = new UserDTO();
        userUpdate.setUsername(edtUserName.getText().toString());
        if(edtPhoneNumber.getText() != null && !edtPhoneNumber.getText().toString().isEmpty()){
            userUpdate.setPhone(edtPhoneNumber.getText().toString());
        }
        userUpdate.setGender(rdbMale.isChecked() ? 1 : 0);
        String date = tvDate.getText().toString();
        if( !date.isEmpty() && date != null){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date birthday = sdf.parse(date);
                userUpdate.setBirthday(birthday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(imageLink != null){
            userUpdate.setImage(imageLink);
        }
        // Gọi API cập nhật thông tin cá nhân
        updateUser(userUpdate);
    }

    //Gọi API update user lên DB
    private void updateUser(UserDTO userUpdate) {
        UserAPI userAPI = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> request = userAPI.updateUser(userUpdate);
        request.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    finish();
                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Cập nhật thông tin cá nhân thất bại
                    // Hiển thị thông báo lỗi
                    Toast.makeText(mContext, "Cập nhật thông tin cá nhân thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                // Cập nhật thông tin cá nhân thất bại
                // Hiển thị thông báo lỗi
                Toast.makeText(mContext, "Cập nhật thông tin cá nhân thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void upLoadImage(){
        LoadingManager.showLoading(this);
        if(imagePath == null){
            handleUpdateInfo();
            return;
        }
        MediaManager.get().upload(imagePath).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d("tag", "onStart: " + "onStart");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Log.d("tag", "onProgress: " + "onProgress");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d("tag", "onSuccess: " +  resultData.get("url"));
                // Upload image to Cloudinary successfully
                imageLink = (String) resultData.get("url");
                // update to Database
                handleUpdateInfo();
                LoadingManager.hideLoading();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.d("tag", "onError: " + error);
                LoadingManager.hideLoading();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.d("tag", "onReschedule: " + error);
            }
        }).dispatch();
    }

//    public  String extractPublicIdFromUrl(String imageUrl) {
//        int startIndex = imageUrl.lastIndexOf("/") + 1;
//        int endIndex = imageUrl.lastIndexOf(".");
//        return imageUrl.substring(startIndex, endIndex);
//    }
//
//    private void deleteImage(String imageUrl)  {
//        Cloudinary cloudinary = new Cloudinary("cloudinary://716447925773513:JD584oxI3Qb9VTy6ZiQJYqSO6YY@dwrd1yxgh");
//        String publicId = extractPublicIdFromUrl(imageUrl);
//        try {
//            ApiResponse apiResponse = cloudinary.api().deleteResources(Arrays.asList(publicId),
//                    ObjectUtils.asMap("type", "upload", "resource_type", "image"));
//            System.out.println(apiResponse);
//        } catch (Exception exception) {
//            System.out.println(exception.getMessage());
//        }
//    }
}
