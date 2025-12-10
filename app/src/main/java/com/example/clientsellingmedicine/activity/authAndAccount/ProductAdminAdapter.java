package com.example.clientsellingmedicine.activity.authAndAccount;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clientsellingmedicine.DTO.Product;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.productAndPayment.AddProductActivity;
import com.example.clientsellingmedicine.api.ProductAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ViewHolder> {


    Context context;
    List<Product> list;


    public ProductAdminAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = list.get(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(p.getPrice() + " đ");
        holder.tvQuantity.setText("SL: " + p.getQuantity());


        Glide.with(context).load(p.getImage()).into(holder.imgProduct);


        holder.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Bạn có chắc muốn xóa \"" + p.getName() + "\" không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        ProductAPI api = ServiceBuilder.buildService(ProductAPI.class);
                        api.deleteProduct(p.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        list.remove(pos);
                                        notifyItemRemoved(pos);
                                    }
                                    Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddProductActivity.class);

            intent.putExtra("product_id", p.getId());
            intent.putExtra("product_name", p.getName());
            intent.putExtra("product_price", p.getPrice());
            intent.putExtra("product_quantity", p.getQuantity());
            intent.putExtra("product_discount", p.getDiscountPercent());
            intent.putExtra("product_description", p.getDescription());
            intent.putExtra("product_image", p.getImage());

            // Lấy từ object category & unit
            if (p.getCategory() != null) {
                intent.putExtra("product_category_id", p.getCategory().getId());
            } else {
                intent.putExtra("product_category_id", 1);
            }

            if (p.getUnit() != null) {
                intent.putExtra("product_unit_id", p.getUnit().getId());
            } else {
                intent.putExtra("product_unit_id", 1);
            }

            context.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnEdit, btnDelete;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}