package com.example.clientsellingmedicine.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.OrderDetailDTO;
import com.example.clientsellingmedicine.utils.Convert;

import java.util.List;



public class orderDetailAdapter extends RecyclerView.Adapter <orderDetailAdapter.ViewHolder> {
    private List<OrderDetailDTO> mProducts;
    private Context mContext;


    public orderDetailAdapter(List<OrderDetailDTO> list) {
        this.mProducts = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView = inflater.inflate(R.layout.payment_item, parent, false);

        orderDetailAdapter.ViewHolder viewHolder = new orderDetailAdapter.ViewHolder(newsView, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetailDTO orderItem = mProducts.get(position);
        if (orderItem == null) {
            return;
        }
        Glide.with(holder.itemView.getContext())
                .load(orderItem.getProduct().getImage())
                .placeholder(R.drawable.loading_icon) // Hình ảnh thay thế khi đang tải
                .error(R.drawable.error_image) // Hình ảnh thay thế khi có lỗi
                .into(holder.ivCartItem);

        holder.tvNameCartItem.setText(orderItem.getProduct().getName());
        holder.tvUnit.setText(orderItem.getProduct().getUnit().getName());
        holder.tvPriceCartItem.setText(Convert.convertPrice(orderItem.getProductPrice()));
        holder.tvQuantity.setText(String.valueOf(orderItem.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivCartItem;

        public TextView tvNameCartItem, tvUnit, tvPriceCartItem, tvQuantity;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            ivCartItem = itemView.findViewById(R.id.ivCartItem);
            tvNameCartItem = itemView.findViewById(R.id.tvNameCartItem);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvPriceCartItem = itemView.findViewById(R.id.tvPriceCartItem);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
