package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.R;

public class CouponActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.coupon_screen);

        addControl();
        addEvents();

    }

    private void addControl() {}
    private void addEvents() {}
}
