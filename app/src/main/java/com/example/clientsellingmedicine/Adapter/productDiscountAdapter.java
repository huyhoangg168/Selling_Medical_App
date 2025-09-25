package com.example.clientsellingmedicine.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.Product;

import java.util.List;

public class productDiscountAdapter extends RecyclerView.Adapter <productDiscountAdapter.ViewHolder>  {

    private List<Product> mProducts;
    private Context mContext;

    public productDiscountAdapter(List<Product> list) {
        this.mProducts = list;
//        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView =
                inflater.inflate(R.layout.product_item, parent, false);

        productDiscountAdapter.ViewHolder viewHolder = new productDiscountAdapter.ViewHolder(newsView,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        Product product = (Product) mProducts.get(position);

        holder.tvNameProductItem.setText(product.getName());
        String unit = product.getUnit().getName();
        String price = convertPrice(product.getPrice());
        holder.tvProductPrice.setText(price+" đ/"+unit);
        Glide.with(holder.itemView.getContext())
                .load(product.getImage())
                .placeholder(R.drawable.loading_icon) // Hình ảnh thay thế khi đang tải
                .error(R.drawable.error_image) // Hình ảnh thay thế khi có lỗi
                .into(holder.ivProductItem);
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNameProductItem,tvProductPrice;
        public ImageView ivProductItem;
        public LinearLayout layout_Discount;



        public Button btnAddtoCartProduct;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            tvNameProductItem = itemView.findViewById(R.id.tvNameProductItem);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            ivProductItem = itemView.findViewById(R.id.ivProductItem);
            btnAddtoCartProduct = itemView.findViewById(R.id.btnAddtoCartProduct);
            ivProductItem = itemView.findViewById(R.id.ivProductItem);
            layout_Discount = itemView.findViewById(R.id.layout_Discount);
            //xử lý sự kiện khi click nút view
            btnAddtoCartProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                }
            });


        }
    }
    public String convertPrice(double number) {
        long integerPart = (long) number;
        int decimalPart = (int) ((number - integerPart) * 1000);

        String formattedIntegerPart = String.format("%,d", integerPart).replace(",", ".");
        String formattedDecimalPart = String.format("%03d", decimalPart);

        return formattedIntegerPart + "." + formattedDecimalPart;
    }
}
