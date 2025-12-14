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
import com.example.clientsellingmedicine.interfaces.IOnButtonAddToCartClickListener;
import com.example.clientsellingmedicine.interfaces.IOnProductItemClickListener;
import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.utils.Convert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class productAdapter extends RecyclerView.Adapter <productAdapter.ViewHolder> {

    private List<Product> mProducts;
    private Context mContext;

    private IOnProductItemClickListener mListener;
    private IOnButtonAddToCartClickListener addToCartClickListener;

    public productAdapter(List<Product> list, IOnProductItemClickListener listener, IOnButtonAddToCartClickListener addToCartClickListener) {
        this.mProducts = list;
        this.mListener = listener;
        this.addToCartClickListener = addToCartClickListener;
    }

    public void setList(List<Product> list){
        mProducts = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNameProductItem,tvProductPrice,tv_Discount, tvProductStock;
        public ImageView ivProductItem;

        public LinearLayout layout_Discount,layoutProductItem;



        public Button btnAddtoCartProduct;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            tvNameProductItem = itemView.findViewById(R.id.tvNameProductItem);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            ivProductItem = itemView.findViewById(R.id.ivProductItem);
            btnAddtoCartProduct = itemView.findViewById(R.id.btnAddtoCartProduct);
            ivProductItem = itemView.findViewById(R.id.ivProductItem);
            layout_Discount = itemView.findViewById(R.id.layout_Discount);
            layoutProductItem = itemView.findViewById(R.id.layoutProductItem);
            tv_Discount = itemView.findViewById(R.id.tv_Discount);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            //xử lý sự kiện khi click nút view
            btnAddtoCartProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                }
            });

        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView = inflater.inflate(R.layout.product_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(newsView,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = (Product) mProducts.get(position);
        if (product == null) {
            return;
        }
        if(product.getDiscountPercent() == 0){
            holder.layout_Discount.setVisibility(View.GONE);
        }
        holder.tv_Discount.setText("-"+product.getDiscountPercent()+"%");
        holder.tvNameProductItem.setText(product.getName());
        String unit = product.getUnit().getName();
        String price = Convert.convertPrice(product.getPrice());
        holder.tvProductPrice.setText(price+"/"+unit);
        Integer qty = product.getQuantity();
        int stock = (qty == null) ? 0 : qty;

        if (stock <= 0) {
            holder.tvProductStock.setText("Hết hàng");
            holder.btnAddtoCartProduct.setEnabled(false);
            holder.btnAddtoCartProduct.setAlpha(0.6f);
            holder.btnAddtoCartProduct.setText("Hết hàng");
        } else {
            holder.tvProductStock.setText("Còn: " + stock);
            holder.btnAddtoCartProduct.setEnabled(true);
            holder.btnAddtoCartProduct.setAlpha(1f);
            holder.btnAddtoCartProduct.setText("Chọn mua");
        }
        Glide.with(holder.itemView.getContext())
                .load(product.getImage())
                .placeholder(R.drawable.loading_icon) // Hình ảnh thay thế khi đang tải
                .error(R.drawable.error_image) // Hình ảnh thay thế khi có lỗi
                .into(holder.ivProductItem);

        holder.layoutProductItem.setOnClickListener(view -> mListener.onItemClick(product));

        holder.btnAddtoCartProduct.setOnClickListener(view -> addToCartClickListener.onButtonAddToCartClick(product));
    }


    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public void sortProducts(Comparator<Product> comparator) {
        Collections.sort(mProducts, comparator);
        notifyDataSetChanged();
    }

}