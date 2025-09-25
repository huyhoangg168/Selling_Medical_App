package com.example.clientsellingmedicine.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.interfaces.IOnVoucherItemClickListener;
import com.example.clientsellingmedicine.DTO.RedeemedCouponDTO;
import com.example.clientsellingmedicine.utils.Convert;

import java.util.List;

public class couponCheckboxAdapter extends RecyclerView.Adapter<couponCheckboxAdapter.ViewHolder> {
    private List<RedeemedCouponDTO> mCoupon;
    private Context mContext;

    private int lastCheckedPosition;
    private int previousLastCheckedPosition = -1;
    private Handler handler = new Handler(Looper.getMainLooper());

    IOnVoucherItemClickListener listener;

    public couponCheckboxAdapter(List<RedeemedCouponDTO> list, IOnVoucherItemClickListener listener, int lastCheckedPosition) {
        this.mCoupon = list;
        this.listener = listener;
        this.lastCheckedPosition = lastCheckedPosition;
    }


    public Integer getPositionVoucherSelected() {
        return lastCheckedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView = inflater.inflate(R.layout.coupon_item_checkbox, parent, false);

        couponCheckboxAdapter.ViewHolder viewHolder = new couponCheckboxAdapter.ViewHolder(newsView, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        RedeemedCouponDTO redeemedCoupon = mCoupon.get(position);
        if (redeemedCoupon == null) {
            return;
        }

        holder.tvNameDiscountItem.setText(redeemedCoupon.getCoupon().getDescription());
        String date_expiration = Convert.convertToDate(redeemedCoupon.getExpiryDate().toString());
        holder.tvExpireDiscountItem.setText(date_expiration);

        listener.onVoucherItemClick(lastCheckedPosition); // get position of voucher selected for apply button in first time

        // Set the state of the CheckBox based on the lastCheckedPosition
        holder.cbSelectCoupon.setChecked(position == lastCheckedPosition);

        holder.cbSelectCoupon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Update lastCheckedPosition and previousLastCheckedPosition
                if (lastCheckedPosition != position) {
                    previousLastCheckedPosition = lastCheckedPosition;
                    lastCheckedPosition = position;
                    listener.onVoucherItemClick(position); // get position of voucher selected

                    notifyItemChanged(previousLastCheckedPosition); // Hủy chọn CheckBox trước đó
                    notifyItemChanged(lastCheckedPosition); // Chọn CheckBox mới

                }
            } else {
                if (lastCheckedPosition == position) {
                    lastCheckedPosition = -1;
                    previousLastCheckedPosition = -1;
                    listener.onVoucherItemClick(lastCheckedPosition);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCoupon.size();
    }

    public RedeemedCouponDTO getCouponSelected() {
        if (lastCheckedPosition == -1)
            return null;
        return mCoupon.get(lastCheckedPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNameDiscountItem, tvExpireDiscountItem, tvDetailDiscountItem;
        public CheckBox cbSelectCoupon;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            tvNameDiscountItem = itemView.findViewById(R.id.tvNameDiscountItem);
            tvExpireDiscountItem = itemView.findViewById(R.id.tvExpireDiscountItem);
            tvDetailDiscountItem = itemView.findViewById(R.id.tvDetailDiscountItem);
            cbSelectCoupon = itemView.findViewById(R.id.cbSelectCoupon);

        }
    }


//public class couponCheckboxAdapter extends RecyclerView.Adapter<couponCheckboxAdapter.ViewHolder> {
//    private List<RedeemedCouponDTO> mCoupon;
//    private SparseBooleanArray itemStateArray = new SparseBooleanArray();
//    IOnVoucherItemClickListener listener;
//
//    public couponCheckboxAdapter(List<RedeemedCouponDTO> list, IOnVoucherItemClickListener listener) {
//        this.mCoupon = list;
//        this.listener = listener;
//    }
//
//    public Integer getPositionVoucherSelected() {
//        for (int i = 0; i < itemStateArray.size(); i++) {
//            int key = itemStateArray.keyAt(i);
//            if (itemStateArray.get(key)) {
//                return key;
//            }
//        }
//        return -1;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Context context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View newsView = inflater.inflate(R.layout.coupon_item_checkbox, parent, false);
//        return new ViewHolder(newsView, context);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        RedeemedCouponDTO redeemedCoupon = mCoupon.get(position);
//        if (redeemedCoupon == null) {
//            return;
//        }
//
//        holder.tvNameDiscountItem.setText(redeemedCoupon.getCoupon().getDescription());
//        String date_expiration = Convert.convertToDate(redeemedCoupon.getExpiryDate().toString());
//        holder.tvExpireDiscountItem.setText(date_expiration);
//
//        // Set the state of the CheckBox based on SparseBooleanArray
//        holder.cbSelectCoupon.setChecked(itemStateArray.get(position, false));
//
//        holder.cbSelectCoupon.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                clearAllSelections();
//                itemStateArray.put(position, true);
//                listener.onVoucherItemClick(position);
//                notifyItemChanged(position); // Thay đổi chỉ mục cụ thể
//            } else {
//                itemStateArray.put(position, false);
//                listener.onVoucherItemClick(-1);
//                notifyItemChanged(position); // Thay đổi chỉ mục cụ thể
//            }
//        });
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return mCoupon.size();
//    }
//
//    public RedeemedCouponDTO getCouponSelected() {
//        int position = getPositionVoucherSelected();
//        if (position == -1)
//            return null;
//        return mCoupon.get(position);
//    }
//
//    private void clearAllSelections() {
//        for (int i = 0; i < itemStateArray.size(); i++) {
//            int key = itemStateArray.keyAt(i);
//            itemStateArray.put(key, false);
//        }
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        public TextView tvNameDiscountItem, tvExpireDiscountItem, tvDetailDiscountItem;
//        public CheckBox cbSelectCoupon;
//
//        public ViewHolder(View itemView, Context context) {
//            super(itemView);
//            tvNameDiscountItem = itemView.findViewById(R.id.tvNameDiscountItem);
//            tvExpireDiscountItem = itemView.findViewById(R.id.tvExpireDiscountItem);
//            tvDetailDiscountItem = itemView.findViewById(R.id.tvDetailDiscountItem);
//            cbSelectCoupon = itemView.findViewById(R.id.cbSelectCoupon);
//        }
//    }
}
