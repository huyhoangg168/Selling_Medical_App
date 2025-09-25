package com.example.clientsellingmedicine.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.Adapter.SubdivisionsAdapter;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.AddressDto;
import com.example.clientsellingmedicine.DTO.District;
import com.example.clientsellingmedicine.DTO.Province;
import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.DTO.Ward;
import com.example.clientsellingmedicine.api.AddressAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.example.clientsellingmedicine.api.UserAPI;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddressActivity extends AppCompatActivity {

    TextInputLayout txt_input_name_layout, txt_input_phone_layout, txt_input_province_layout, txt_input_district_layout, txt_input_ward_layout, txt_input_specificAddress_layout;
    TextInputEditText txt_input_name, txt_input_phone, txt_input_province, txt_input_district, txt_input_ward, txt_input_specificAddress;
    Button btn_add_address;

    TextView tvTitleHeader;

    Switch swt_default_address;
    RadioButton rb_home, rb_company;

    ImageView iv_back;

    SubdivisionsAdapter adapter;

    private Context mContext;

    private Integer provinceID = 0;
    private Integer districtID = 0;

    private Boolean isDefaultAddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_address_screen);
        mContext = this;
        addControl();
        addEvents();
    }

    public void addControl() {
        // layout input
        txt_input_name_layout = findViewById(R.id.txt_input_name_layout);
        txt_input_phone_layout = findViewById(R.id.txt_input_phone_layout);
        txt_input_province_layout = findViewById(R.id.txt_input_province_layout);
        txt_input_district_layout = findViewById(R.id.txt_input_district_layout);
        txt_input_ward_layout = findViewById(R.id.txt_input_ward_layout);
        txt_input_specificAddress_layout = findViewById(R.id.txt_input_specificAddress_layout);

        // text input
        txt_input_name = findViewById(R.id.txt_input_name);
        txt_input_phone = findViewById(R.id.txt_input_phone);
        txt_input_province = findViewById(R.id.txt_input_province);
        txt_input_district = findViewById(R.id.txt_input_district);
        txt_input_ward = findViewById(R.id.txt_input_ward);
        txt_input_specificAddress = findViewById(R.id.txt_input_specificAddress);

        // button
        btn_add_address = findViewById(R.id.btn_add_address);
        rb_home = findViewById(R.id.rb_home);
        rb_company = findViewById(R.id.rb_company);

        // switch
        swt_default_address = findViewById(R.id.swt_default_address);

        // text view
        tvTitleHeader = findViewById(R.id.tvTitleHeader);

        iv_back = findViewById(R.id.iv_back);


    }

    public void addEvents() {
        iv_back.setOnClickListener(view -> finish());


        // get address update
        AddressDto address = getAddressUpdate();
        // set data for address update screen
        if (address != null) {
            // set title header
            tvTitleHeader.setText("Cập nhật địa chỉ");
            // set text for button
            btn_add_address.setText("Cập nhật");
            // set data for address update screen
            txt_input_name.setText(address.getUser_name().trim());
            txt_input_phone.setText(address.getPhone().trim());
            txt_input_province.setText(address.getProvince());
            txt_input_district.setEnabled(true);
            txt_input_district.setText(address.getDistrict());
            txt_input_ward.setEnabled(true);
            txt_input_ward.setText(address.getWard());
            txt_input_specificAddress.setText(address.getSpecific_address().trim());
            String type = address.getType();
            if (type.equals("Nhà riêng")) {
                rb_home.setChecked(true);
            } else {
                rb_company.setChecked(true);
            }

            if (address.getIs_default()) {
                swt_default_address.setChecked(true);
                isDefaultAddress = true;
            } else {
                swt_default_address.setChecked(false);
                isDefaultAddress = false;
            }
        }

        // add event for button add address
        btn_add_address.setOnClickListener(v -> {
            if (txt_input_name_layout.getEditText().getText().toString().trim().isEmpty()) {
                txt_input_name_layout.setError("Vui lòng nhập họ tên");
                return;
            }
            if (txt_input_phone_layout.getEditText().getText().toString().trim().isEmpty()) {
                txt_input_phone_layout.setError("Vui lòng nhập số điện thoại");
                return;
            }
            // validate phone number
            String phoneNumber = txt_input_phone_layout.getEditText().getText().toString().trim();
            String phoneRegex = "^(0[3|5|7|8|9])+([0-9]{8})$";
            Pattern pattern = Pattern.compile(phoneRegex);
            Matcher matcher = pattern.matcher(phoneNumber);
            if (!matcher.matches()) {
                txt_input_phone_layout.setError("Số điện thoại không hợp lệ");
                return;
            }
            if (txt_input_province_layout.getEditText().getText().toString().trim().isEmpty()) {
                txt_input_province_layout.setError("Vui lòng chọn tỉnh/thành phố");
                return;
            }
            if (txt_input_district_layout.getEditText().getText().toString().trim().isEmpty()) {
                txt_input_district_layout.setError("Vui lòng chọn quận/huyện");
                return;
            }
            if (txt_input_ward_layout.getEditText().getText().toString().trim().isEmpty()) {
                txt_input_ward_layout.setError("Vui lòng chọn phường/xã");
                return;
            }
            if (txt_input_specificAddress_layout.getEditText().getText().toString().trim().isEmpty()) {
                txt_input_specificAddress_layout.setError("Vui lòng nhập số nhà, tên đường");
                return;
            }

            if (address != null) {
                // update address
                address.setId(address.getId());
                address.setId_user(address.getId_user());
                address.setUser_name(txt_input_name.getText().toString());
                address.setPhone(txt_input_phone.getText().toString());
                address.setProvince(txt_input_province.getText().toString());
                address.setDistrict(txt_input_district.getText().toString());
                address.setWard(txt_input_ward.getText().toString());
                address.setSpecific_address(txt_input_specificAddress.getText().toString());
                address.setType(rb_home.isChecked() ? "Nhà riêng" : "Công ty");
                address.setIs_default(swt_default_address.isChecked());
                updateAddress(address);
            } else {
                // get id user
                UserDTO user = getUser();
                // add new address
                AddressDto addressNew = new AddressDto();
                addressNew.setId_user(user.getId());
                addressNew.setUser_name(txt_input_name.getText().toString());
                addressNew.setPhone(txt_input_phone.getText().toString());
                addressNew.setProvince(txt_input_province.getText().toString());
                addressNew.setDistrict(txt_input_district.getText().toString());
                addressNew.setWard(txt_input_ward.getText().toString());
                addressNew.setSpecific_address(txt_input_specificAddress.getText().toString());
                addressNew.setType(rb_home.isChecked() ? "Nhà riêng" : "Công ty");
                addressNew.setIs_default(swt_default_address.isChecked());
                addNewAddress(addressNew);
            }
        });


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!txt_input_name.getText().toString().trim().isEmpty()) {
                    txt_input_name_layout.setError(null);
                }
                if (!txt_input_phone.getText().toString().trim().isEmpty()) {
                    txt_input_phone_layout.setError(null);
                }
                if (!txt_input_province.getText().toString().trim().isEmpty()) {
                    txt_input_province_layout.setError(null);
                    txt_input_district.setEnabled(true);
                }
                if (!txt_input_district.getText().toString().trim().isEmpty()) {
                    txt_input_district_layout.setError(null);
                    txt_input_ward.setEnabled(true);
                }
                if (!txt_input_ward.getText().toString().trim().isEmpty()) {
                    txt_input_ward_layout.setError(null);
                }
                if (!txt_input_specificAddress.getText().toString().trim().isEmpty()) {
                    txt_input_specificAddress_layout.setError(null);
                }
            }
        };
        txt_input_name.addTextChangedListener(textWatcher);
        txt_input_phone.addTextChangedListener(textWatcher);
        txt_input_province.addTextChangedListener(textWatcher);
        txt_input_district.addTextChangedListener(textWatcher);
        txt_input_ward.addTextChangedListener(textWatcher);
        txt_input_specificAddress.addTextChangedListener(textWatcher);


        txt_input_province.setOnClickListener(v -> {
            showSelectSubdivisionsDialog("Thành phố/Tỉnh", getProvinces());
        });

        txt_input_district.setOnClickListener(view -> {
            showSelectSubdivisionsDialog("Quận/Huyện", getDistricts(provinceID));
        });

        txt_input_ward.setOnClickListener(view -> {
            showSelectSubdivisionsDialog("Phường/Xã", getWards(districtID));
        });


        swt_default_address.setOnClickListener(view -> {
            Log.d("TAG", "onClick: " + swt_default_address.isChecked());
            if (isDefaultAddress) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
                builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                        .setTitle("Thông Báo")
                        .setMessage("Bạn vui lòng chọn một địa chỉ khác làm mặc định !")
                        .setCancelable(false) // Bấm ra ngoài không mất dialog
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Xử lý khi nhấn nút OK

                        })
                        .show();
                swt_default_address.setChecked(true);
            }
        });
    }

    public AddressDto getAddressUpdate() {
        // get address update
        Intent intent = getIntent();
        AddressDto address = (AddressDto) intent.getSerializableExtra("address");
        return address;
    }

    public void addNewAddress(AddressDto addressDto) {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<AddressDto> request = addressAPI.addAddress(addressDto);
        request.enqueue(new Callback<AddressDto>() {

            @Override
            public void onResponse(Call<AddressDto> call, Response<AddressDto> response) {
                if (response.isSuccessful()) {
                    Intent resultIntent = new Intent();
                    // Đặt dữ liệu trả về vào intent nếu cần
                    setResult(RESULT_OK, resultIntent);
                    finish();

                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items (response)", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<AddressDto> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void updateAddress(AddressDto addressDto) {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<ResponseDto> request = addressAPI.updateAddress(addressDto);
        request.enqueue(new Callback<ResponseDto>() {

            @Override
            public void onResponse(Call<ResponseDto> call, Response<ResponseDto> response) {
                if (response.isSuccessful()) {
                    Intent resultIntent = new Intent();
                    // Đặt dữ liệu trả về vào intent nếu cần
                    setResult(RESULT_OK, resultIntent);
                    finish();

                } else if (response.code() == 401) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (response.code() == 400) {
                    Toast.makeText(mContext, "Invalid input data", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onFailure(Call<ResponseDto> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
            }
        });
    }


    public List<Province> getProvinces() {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<List<Province>> call = addressAPI.getProvinces();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Province>> future = executorService.submit(new Callable<List<Province>>() {
            @Override
            public List<Province> call() throws Exception {
                try {
                    Response<List<Province>> response = call.execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                        return null;
                    }
                } catch (IOException e) {
                    Log.d("Error", "Get Province Exception : " + e.getMessage());
                    return null;
                }
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }


    public List<District> getDistricts(Integer provinceId) {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<List<District>> call = addressAPI.getDistricts(provinceId);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<District>> future = executorService.submit(() -> {
            try {
                Response<List<District>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get District Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }


    public List<Ward> getWards(Integer districtId) {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<List<Ward>> call = addressAPI.getWards(districtId);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Ward>> future = executorService.submit(() -> {
            try {
                Response<List<Ward>> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Wards Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }


    public UserDTO getUser() {
        UserAPI addressService = ServiceBuilder.buildService(UserAPI.class);
        Call<UserDTO> call = addressService.getUser();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<UserDTO> future = executorService.submit((Callable<UserDTO>) () -> {
            try {
                Response<UserDTO> response = call.execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    Log.d("Error", "Status Code : " + response.code() + ", Message : " + response.message());
                    return null;
                }
            } catch (IOException e) {
                Log.d("Error", "Get Province Exception : " + e.getMessage());
                return null;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } finally {
            executorService.shutdown();
        }

    }


    private void showSelectSubdivisionsDialog(String title, List<?> list) {

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_address);

        // add control
        TextInputEditText edtSearchAddress = dialog.findViewById(R.id.edtSearchAddress);
        TextView tv_administrative_divisions = dialog.findViewById(R.id.tv_administrative_divisions);
        ImageView iv_back = dialog.findViewById(R.id.iv_back);
        ListView lv_administrative_divisions = dialog.findViewById(R.id.lv_administrative_divisions);

        // add event
        edtSearchAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = charSequence.toString().toLowerCase().trim();
                List<Object> filtered = search(searchText,list);
                adapter = new SubdivisionsAdapter(mContext, filtered);
                lv_administrative_divisions.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        tv_administrative_divisions.setText(title);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // add data to listview
        List<Object> convertedList = new ArrayList<>();
        convertedList.addAll(list);
        adapter = new SubdivisionsAdapter(this, convertedList);
        lv_administrative_divisions.setAdapter(adapter);
        lv_administrative_divisions.setOnItemClickListener((parent, view, position, id) -> {
            Object item = lv_administrative_divisions.getItemAtPosition(position);
            handleSelectedItem(item);
            dialog.dismiss();
        });

        // show dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public void handleSelectedItem(Object data) {
        if (data instanceof Province) {
            List<Integer> centralCityProvinceIDs = new ArrayList<>(Arrays.asList(1, 79, 31, 48, 92));
            provinceID = ((Province) data).getId();
            Log.d("TAG", "handleSelectedItem: " + provinceID);
            if (centralCityProvinceIDs.contains(provinceID)) {  //this province is central city
                txt_input_province.setText("Thành phố" + ((Province) data).getName());
            } else
                //this province is not central city
                txt_input_province.setText("Tỉnh" + ((Province) data).getName());

            // if changed province
            if (!txt_input_district.getText().toString().isEmpty()) {
                txt_input_district.setText("");
            }
            if (!txt_input_ward.getText().toString().isEmpty()) {
                txt_input_ward.setText("");
                txt_input_ward.setEnabled(false);
            }
            if (!txt_input_specificAddress.getText().toString().isEmpty()) {
                txt_input_specificAddress.setText("");
            }
        }

        if (data instanceof District) {
            txt_input_district.setText(((District) data).getName());
            districtID = ((District) data).getId();
        }

        if (data instanceof Ward)
            txt_input_ward.setText(((Ward) data).getName());
    }

    // Hàm tìm kiếm
    private List<Object> search(String searchText, List<?> list){
        List<Object> filtered = new ArrayList<>();
        String searchQuery = normalizeString(searchText);
        for (Object obj : list) {
            if(obj instanceof Province){
                if(normalizeString(((Province) obj).getName()).contains(searchQuery)){
                    filtered.add(obj);
                }

            }
            if(obj instanceof District){
                if(normalizeString(((District) obj).getName()).contains(searchQuery)){
                    filtered.add(obj);
                }

            }
            if(obj instanceof Ward){
                if(normalizeString(((Ward) obj).getName()).contains(searchQuery)){
                    filtered.add(obj);
                }

            }
        }
        return filtered;
    }

    // Hàm chuẩn hóa chuỗi không dấu (dùng cho tìm kiếm)
    public static String normalizeString(String input) {
        // Sử dụng Normalizer và regex để chuẩn hóa chuỗi
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizedString).replaceAll("").toLowerCase();
    }

}
