package com.example.clientsellingmedicine.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.activity.adress.AddAddressActivity;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.interfaces.IOnProductItemClickListener;
import com.example.clientsellingmedicine.DTO.AddressDto;
import com.example.clientsellingmedicine.DTO.ResponseDto;
import com.example.clientsellingmedicine.api.AddressAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class addressAdapter extends RecyclerView.Adapter <addressAdapter.ViewHolder> {

    private List<AddressDto> mAddress;
    private Context mContext;

    private IOnProductItemClickListener mListener;

    private ActivityResultLauncher<Intent> launcher;
    public addressAdapter(List<AddressDto> list, IOnProductItemClickListener listener, ActivityResultLauncher<Intent> launcher) {
        this.mAddress = list;
        this.mListener = listener;
        this.launcher = launcher;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_userName,tv_update,tv_delete,tv_phoneNumber,tv_address,tv_type_address;

        public LinearLayout layout_is_default_address;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            tv_userName = itemView.findViewById(R.id.tv_userName);
            tv_update = itemView.findViewById(R.id.tv_update);
            tv_delete = itemView.findViewById(R.id.tv_delete);
            tv_phoneNumber = itemView.findViewById(R.id.tv_phoneNumber);
            tv_address = itemView.findViewById(R.id.tv_address);
            tv_type_address = itemView.findViewById(R.id.tv_type_address);
            layout_is_default_address = itemView.findViewById(R.id.layout_is_default_address);

        }
    }
    @NonNull
    @Override
    public addressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView = inflater.inflate(R.layout.registered_address_item, parent, false);

        addressAdapter.ViewHolder viewHolder = new addressAdapter.ViewHolder(newsView,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull addressAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AddressDto address = (AddressDto) mAddress.get(position);
        if (address == null) {
            return;
        }
        holder.tv_userName.setText(address.getUser_name());
        holder.tv_phoneNumber.setText(address.getPhone());
        holder.tv_address.setText(address.getSpecific_address() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getProvince());
        holder.tv_type_address.setText(address.getType());



        if (address.getIs_default()) {
            holder.layout_is_default_address.setVisibility(View.VISIBLE);
        } else {
            holder.layout_is_default_address.setVisibility(View.GONE);
        }

        holder.tv_update.setOnClickListener(view -> {
            // go to update address screen
            Intent intent = new Intent(holder.itemView.getContext(), AddAddressActivity.class);
            intent.putExtra("address", (Serializable) address);
            launcher.launch(intent);;

        });

        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(address.getIs_default()){
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.itemView.getContext());
                    builder.setIcon(R.drawable.ic_warning) // Đặt icon của Dialog
                            .setTitle("Thông Báo")
                            .setMessage("Vui lòng chọn một địa chỉ khác làm mặc định trước xóa địa chỉ !")
                            .setCancelable(false) // Bấm ra ngoài không mất dialog
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Xử lý khi nhấn nút OK

                                }
                            })
                            .show();
                }
                else {
                    deleteAddress(address.getId(), position);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mAddress.size();
    }


    public void deleteAddress(Integer addressID, int position) {
        AddressAPI addressAPI = ServiceBuilder.buildService(AddressAPI.class);
        Call<ResponseDto> request = addressAPI.deleteAddress(addressID);
        request.enqueue(new Callback<ResponseDto>() {
            @Override
            public void onResponse(Call<ResponseDto> call, Response<ResponseDto> response) {
                if (response.isSuccessful()) {
                    // remove item from recyclerview
                    mAddress.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                } else if (response.code() == 401) {
                    Toast.makeText(mContext, "Your session has expired", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items (response)", Toast.LENGTH_LONG).show();
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
}
