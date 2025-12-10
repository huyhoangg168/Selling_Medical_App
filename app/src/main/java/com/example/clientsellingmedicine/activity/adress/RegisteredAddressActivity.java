package com.example.clientsellingmedicine.activity.adress;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.Adapter.addressAdapter;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.interfaces.IOnProductItemClickListener;
import com.example.clientsellingmedicine.DTO.AddressDto;
import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.api.AddressAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisteredAddressActivity extends AppCompatActivity implements IOnProductItemClickListener {

    private Context mContext;

    private RecyclerView rcv_address;
    private ImageView iv_back;

    private Button btn_add_address;

    private addressAdapter addressAdapter;
    private ActivityResultLauncher<Intent> launcher;
    IOnProductItemClickListener listener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.registered_address_screen);

        // get result status from addAddress activity
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
//                Intent data = result.getData();
                // Xử lý dữ liệu trả về từ màn hình B và cập nhật UI ở đây
                getRegisteredAddress();
            }
        });
        addControl();
        addEvents();
    }

    public void addControl() {
        rcv_address = findViewById(R.id.rcv_address);
        iv_back = findViewById(R.id.iv_back);
        btn_add_address = findViewById(R.id.btn_add_address);
    }
    public void addEvents() {

        // divider item on recyclerview
        rcv_address.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        // back to previous screen
        iv_back.setOnClickListener(v -> {
            finish();
        });

        // go to add address screen
        btn_add_address.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, AddAddressActivity.class);
            launcher.launch(intent);
        });

        // get registered address
        getRegisteredAddress();
    }

    public void getRegisteredAddress() {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<List<AddressDto>> request = addressAPI.getAddress();

        request.enqueue(new Callback<List<AddressDto>>() {
            @Override
            public void onResponse(Call<List<AddressDto>> call, Response<List<AddressDto>> response) {
                if(response.isSuccessful()){
                    addressAdapter = new addressAdapter(response.body(), RegisteredAddressActivity.this, launcher);
                    rcv_address.setAdapter(addressAdapter);
                    rcv_address.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

                } else if(response.code() == 401) {
                    Toast.makeText(mContext, "Your session has expired", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AddressDto>> call, Throwable t) {
                if (t instanceof IOException){
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    @Override
    public void onItemClick(Product product) {

    }
}
