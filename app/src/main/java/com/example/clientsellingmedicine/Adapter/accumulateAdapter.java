package com.example.clientsellingmedicine.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.DTO.OrderDTO;
import com.example.clientsellingmedicine.utils.Convert;

import java.util.List;


public class accumulateAdapter extends RecyclerView.Adapter<accumulateAdapter.ViewHolder> {
    private List<OrderDTO> mOrders;
    private Context mContext;


    public accumulateAdapter(List<OrderDTO> list) {
        this.mOrders = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View newsView = inflater.inflate(R.layout.accumulate_points_history_item, parent, false);

        accumulateAdapter.ViewHolder viewHolder = new accumulateAdapter.ViewHolder(newsView, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDTO order = mOrders.get(position);
        if (order == null) {
            return;
        }


        holder.tv_label_code.setText("Mã đơn hàng:");
        holder.tv_Code.setText(order.getCode());
        // Expire date = current date + expire date
        String time = order.getOrderTime().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = Convert.convertToDate(time);
        }
        holder.tv_Time.setText(time);
        holder.tv_label_point.setText("Điểm tích lũy:");
        holder.tv_Point.setText("  +" + order.getPoint());
        holder.tv_Point.setTextColor(Color.parseColor("#FFC700"));


    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_label_code, tv_Code, tv_Time, tv_Point,tv_label_point;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            tv_label_point = itemView.findViewById(R.id.tv_label_point);
            tv_label_code = itemView.findViewById(R.id.tv_label_code);
            tv_Code = itemView.findViewById(R.id.tv_Code);
            tv_Time = itemView.findViewById(R.id.tv_Time);
            tv_Point = itemView.findViewById(R.id.tv_Point);

        }
    }
}
