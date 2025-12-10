package com.example.clientsellingmedicine.activity.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clientsellingmedicine.Adapter.orderAdapter;
import com.example.clientsellingmedicine.R;
import com.example.clientsellingmedicine.activity.MainActivity;
import com.example.clientsellingmedicine.activity.authAndAccount.LoginActivity;
import com.example.clientsellingmedicine.interfaces.IOnOrderItemClickListener;
import com.example.clientsellingmedicine.DTO.OrderDTO;

import com.example.clientsellingmedicine.api.OrderAPI;
import com.example.clientsellingmedicine.api.ServiceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFragment extends Fragment implements IOnOrderItemClickListener {
    private Context mContext;

    LinearLayout layout_allOrder, layout_processingStatus, layout_inTransitStatus, layout_deliveredStatus, layout_cancelledStatus, layout_empty_order, layout_statusOrder;

    TextView processingStatus, inTransitStatus, deliveredStatus, cancelledStatus, allOrder, tv_empty_order;

    Button btn_shopping_now;

    ScrollView scrollview_content;

    RecyclerView rcvOrder;

    orderAdapter orderAdapter;

    HorizontalScrollView horizontal_statusOrder;

    private static List<OrderDTO> listOrder; //list order for search status

    public OrderFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_screen, container, false);
        mContext = view.getContext();
        // Thực hiện các thao tác cần thiết trên giao diện view của fragment
        addControl(view);
        addEvents();
        return view;


    }


    private void addControl(View view) {
        layout_statusOrder = view.findViewById(R.id.layout_statusOrder);
        layout_processingStatus = view.findViewById(R.id.layout_processingStatus);
        layout_inTransitStatus = view.findViewById(R.id.layout_inTransitStatus);
        layout_deliveredStatus = view.findViewById(R.id.layout_deliveredStatus);
        layout_cancelledStatus = view.findViewById(R.id.layout_cancelledStatus);
        layout_allOrder = view.findViewById(R.id.layout_allOrder);
        layout_empty_order = view.findViewById(R.id.layout_empty_order);

        processingStatus = view.findViewById(R.id.processingStatus);
        inTransitStatus = view.findViewById(R.id.inTransitStatus);
        deliveredStatus = view.findViewById(R.id.deliveredStatus);
        cancelledStatus = view.findViewById(R.id.cancelledStatus);
        allOrder = view.findViewById(R.id.allOrder);
        tv_empty_order = view.findViewById(R.id.tv_empty_order);

        horizontal_statusOrder = view.findViewById(R.id.horizontal_statusOrder);
        scrollview_content = view.findViewById(R.id.scrollview_content);

        rcvOrder = view.findViewById(R.id.rcvOrder);

        btn_shopping_now = view.findViewById(R.id.btn_shopping_now);



    }

    private void addEvents() {
        // Set the horizontal scroll position to the first position
        horizontal_statusOrder.post(new Runnable() {
            @Override
            public void run() {
                horizontal_statusOrder.scrollTo(0, 0);
            }
        });


//        // click on the shopping now button or login now button
//        View.OnClickListener shoppingNowClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (view == btn_shopping_now) {
//                    //go to home fragment
//                    ((MainActivity) getActivity()).goToHomeFragment();
//                }
//            }
//        };
//        btn_shopping_now.setOnClickListener(shoppingNowClickListener);


        //get order status
        View.OnClickListener statusClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_allOrder.setBackgroundResource(R.color.white);
                layout_processingStatus.setBackgroundResource(R.color.white);
                layout_inTransitStatus.setBackgroundResource(R.color.white);
                layout_deliveredStatus.setBackgroundResource(R.color.white);
                layout_cancelledStatus.setBackgroundResource(R.color.white);

                if (view == processingStatus) {
                    layout_processingStatus.setBackgroundResource(R.drawable.order_selection_background);
                    if (listOrder != null) {
                        //get orders delivered
                        List<OrderDTO> ordersDelivered = listOrder.stream()
                                .filter(order -> order.getStatus() == 2)
                                .sorted(Comparator.comparing(OrderDTO::getOrderTime).reversed())
                                .collect(Collectors.toList());

                        orderAdapter.setListOrder(ordersDelivered);
                    }

                } else if (view == inTransitStatus) {
                    layout_inTransitStatus.setBackgroundResource(R.drawable.order_selection_background);
                    if (listOrder != null) {
                        //get orders delivered
                        List<OrderDTO> ordersDelivered = listOrder.stream()
                                .filter(order -> order.getStatus() == 3)
                                .sorted(Comparator.comparing(OrderDTO::getOrderTime).reversed())
                                .collect(Collectors.toList());

                        orderAdapter.setListOrder(ordersDelivered);
                    }

                } else if (view == deliveredStatus) {
                    //set background color delivered status
                    layout_deliveredStatus.setBackgroundResource(R.drawable.order_selection_background);
                    if (listOrder != null) {
                        //get orders delivered
                        List<OrderDTO> ordersDelivered = listOrder.stream()
                                .filter(order -> order.getStatus() == 1)
                                .sorted(Comparator.comparing(OrderDTO::getOrderTime).reversed())
                                .collect(Collectors.toList());

                        orderAdapter.setListOrder(ordersDelivered);
                    }

                } else if (view == cancelledStatus) {
                    //set background color cancelled status
                    layout_cancelledStatus.setBackgroundResource(R.drawable.order_selection_background);
                    if (listOrder != null) {
                        //get orders cancelled
                        List<OrderDTO> ordersCancelled = listOrder.stream()
                                .filter(order -> order.getStatus() == 0)
                                .sorted(Comparator.comparing(OrderDTO::getOrderTime).reversed())
                                .collect(Collectors.toList());

                        orderAdapter.setListOrder(ordersCancelled);
                    }
                } else if (view == allOrder) {
                    layout_allOrder.setBackgroundResource(R.drawable.order_selection_background);
                    if (listOrder != null) {
                        orderAdapter.setListOrder(listOrder);
                    }

                }
            }
        };

        //set color background for title order filter
        processingStatus.setOnClickListener(statusClickListener);
        inTransitStatus.setOnClickListener(statusClickListener);
        deliveredStatus.setOnClickListener(statusClickListener);
        cancelledStatus.setOnClickListener(statusClickListener);
        allOrder.setOnClickListener(statusClickListener);


        getOrders();
    }

    // Lấy dữ liệu lịch sử đặt hàng
    public void getOrders() {
        OrderAPI orderAPI = ServiceBuilder.buildService(OrderAPI.class);
        Call<List<OrderDTO>> request = orderAPI.getOrders();

        request.enqueue(new Callback<List<OrderDTO>>() {
            @Override
            public void onResponse(Call<List<OrderDTO>> call, Response<List<OrderDTO>> response) {
                if (response.isSuccessful()) {
                    listOrder = new ArrayList<>();
                    listOrder = response.body().stream() //get list order
                            .sorted(Comparator.comparing(OrderDTO::getId).reversed())  // sort by id
                            .collect(Collectors.toList());
                    if(listOrder != null && listOrder.size() > 0) {
                        // add list order to recycle view
                        orderAdapter = new orderAdapter(listOrder, OrderFragment.this); // Hiển thị dữ liệu bằng orderAdapter
                        rcvOrder.setAdapter(orderAdapter);
                        LinearLayoutManager layoutManager
                                = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                        rcvOrder.setLayoutManager(layoutManager);
                    }
                    else {
                        showOrderHistoryWithEmptyOrder();
                    }

                } else if (response.code() == 401) {
                    showOrderHistoryWithoutLogin();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderDTO>> call, Throwable t) {
                if (t instanceof IOException) {
                    Toast.makeText(mContext, "A connection error occured", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "Failed to retrieve items", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void showOrderHistoryWithoutLogin() {
        tv_empty_order.setText("Bạn cần đăng nhập để xem lịch sử đơn hàng!");
        btn_shopping_now.setText("Đăng nhập ngay");
        layout_empty_order.setVisibility(View.VISIBLE);
        scrollview_content.setVisibility(View.GONE);
        horizontal_statusOrder.setVisibility(View.GONE);
        btn_shopping_now.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        });
    }

    public void showOrderHistoryWithEmptyOrder() {
        tv_empty_order.setText("Bạn chưa có đơn hàng nào!");
        btn_shopping_now.setText("Mua sắm ngay");
        layout_empty_order.setVisibility(View.VISIBLE);
        scrollview_content.setVisibility(View.GONE);
        horizontal_statusOrder.setVisibility(View.GONE);

        btn_shopping_now.setOnClickListener(view -> ((MainActivity) getActivity()).goToHomeFragment());
    }

    @Override
    public void onItemClick(OrderDTO order) {
//        Intent intent = new Intent(mContext, OrderDetailActivity.class);
//        intent.putExtra("order", order);
//        startActivity(intent);

        Intent intent = new Intent(mContext, OrderDetailActivity.class);
        intent.putExtra("code", order.getCode());
        startActivity(intent);
    }
}
