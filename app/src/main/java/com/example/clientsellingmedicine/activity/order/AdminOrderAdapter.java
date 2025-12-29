package com.example.clientsellingmedicine.activity.order;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.DTO.OrderDTO;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.example.clientsellingmedicine.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.example.clientsellingmedicine.utils.CryptoManager;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    public interface OnActionListener {
        void onApprove(OrderDTO order);
        void onReject(OrderDTO order);
    }

    private List<OrderDTO> orders;
    private OnActionListener listener;

    public AdminOrderAdapter(List<OrderDTO> orders, OnActionListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDTO order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    public void updateData(List<OrderDTO> newList) {
        this.orders = newList;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvCode, tvCustomerName, tvPhone, tvAddress, tvNote;
        TextView tvOrderDate, tvProducts, tvTotal, tvStatusChip;
        TextView btnReject, btnApprove;

        TextView tvIntegrityWarning;
        LinearLayout layoutSensitiveInfo;

        private CryptoManager cryptoManager;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // 2. Khởi tạo CryptoManager
            cryptoManager = new CryptoManager(itemView.getContext());

            // 3. Ánh xạ View
            tvCode = itemView.findViewById(R.id.tvCode);
            tvIntegrityWarning = itemView.findViewById(R.id.tvIntegrityWarning);
            layoutSensitiveInfo = itemView.findViewById(R.id.layoutSensitiveInfo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvNote = itemView.findViewById(R.id.tvNote);

            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvProducts = itemView.findViewById(R.id.tvProducts);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatusChip = itemView.findViewById(R.id.tvStatusChip);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }

        public void bind(OrderDTO order) {
            tvCode.setText("#" + order.getCode());

            // 1. Lấy dữ liệu thô để kiểm tra cờ cảnh báo
            String rawPhone = (order.getUser() != null) ? order.getUser().getPhone() : "";
            String rawAddress = order.getUserAddress();
            String rawNote = order.getNote();

            // Kiểm tra từ khóa cảnh báo (Backend gửi về "⚠️ TAMPERED" hoặc "⚠️")
            boolean isTampered = (rawPhone != null && rawPhone.contains("⚠️")) ||
                    (rawAddress != null && rawAddress.contains("⚠️")) ||
                    (rawNote != null && rawNote.contains("⚠️"));

            if (isTampered) {
                // TRƯỜNG HỢP BỊ CAN THIỆP: Hiện cảnh báo, Ẩn thông tin
                tvIntegrityWarning.setVisibility(View.VISIBLE);
                layoutSensitiveInfo.setVisibility(View.GONE);
            } else {
                // TRƯỜNG HỢP AN TOÀN: Ẩn cảnh báo, Hiện thông tin & Set dữ liệu bình thường
                tvIntegrityWarning.setVisibility(View.GONE);
                layoutSensitiveInfo.setVisibility(View.VISIBLE);
                String name = "Khách";
                if (order.getUser() != null && order.getUser().getUsername() != null) {
                    name = order.getUser().getUsername();
                }
                tvCustomerName.setText("Tên khách hàng: " + name);

                // Xử lý SĐT (Decrypt nếu cần)
                String phone = (rawPhone != null) ? rawPhone : "N/A";
                if(phone.startsWith("AES_ENCRYPTED:")) phone = cryptoManager.decrypt(phone);
                tvPhone.setText("SĐT: " + phone);

                // Xử lý Địa chỉ
                String address = (rawAddress != null) ? rawAddress : "N/A";
                if(address.startsWith("AES_ENCRYPTED:")) address = cryptoManager.decrypt(address);
                tvAddress.setText("Địa chỉ: " + address);

                // Xử lý Note
                String note = (rawNote != null) ? rawNote : "";
                if(note.startsWith("AES_ENCRYPTED:")) note = cryptoManager.decrypt(note);
                tvNote.setText("Ghi chú: " + note);
            }


            // Ngày đặt
            String dateStr = "";
            if (order.getOrderTime() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateStr = sdf.format(order.getOrderTime());
            }
            tvOrderDate.setText("Ngày đặt: " + dateStr);

            // ======= SẢN PHẨM =======
            String products = order.getProductSummary();
            if (products == null || products.isEmpty()) {
                tvProducts.setText("Sản phẩm: (đang tải...)");
            } else {
                tvProducts.setText("Sản phẩm: " + products);
            }

            // Tổng tiền
            int total = order.getTotal() != null ? order.getTotal() : 0;
            tvTotal.setText(String.format(Locale.getDefault(), "%,dđ", total));

            // Chip trạng thái
            switch (order.getStatus() != null ? order.getStatus() : 2) {
                case 2:
                    tvStatusChip.setText("Chờ duyệt");
                    tvStatusChip.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatusChip.setTextColor(Color.parseColor("#C98000"));
                    break;
                case 1:
                    tvStatusChip.setText("Thành công");
                    //tvStatusChip.setBackgroundResource(R.drawable.bg_status_success);
                    tvStatusChip.setTextColor(Color.parseColor("#2E7D32"));
                    break;
                case 0:
                default:
                    tvStatusChip.setText("Từ chối");
                    //tvStatusChip.setBackgroundResource(R.drawable.bg_status_reject);
                    tvStatusChip.setTextColor(Color.parseColor("#E53935"));
                    break;
            }

            btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(order);
            });

            btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(order);
            });
        }
    }
}
