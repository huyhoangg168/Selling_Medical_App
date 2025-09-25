package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.clientsellingmedicine.Adapter.rewardPointsHistoryAdapter;
import com.example.clientsellingmedicine.R;
import com.google.android.material.tabs.TabLayout;

public class RewardPointsHistoryActvity extends AppCompatActivity {
    private Context mContext;
    private TabLayout mTabLayout;
    private ViewPager mViewPaper;
    private TextView tvPoints;
    private Button btn_ExchangePoints;
    private ImageView ivBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.accumulate_points_history_screen);

        addControl();
        addEvents();

    }

    private void addControl() {
        ivBack = findViewById(R.id.ivBack);
        tvPoints = findViewById(R.id.tvPoints);
        btn_ExchangePoints = findViewById(R.id.btn_ExchangePoints);
        mTabLayout = findViewById(R.id.tabRewardPointsHistory);
        mViewPaper = findViewById(R.id.viewPayPer_RewardPointsHistory);

        rewardPointsHistoryAdapter historyAdapter = new rewardPointsHistoryAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPaper.setAdapter(historyAdapter);
        mTabLayout.setupWithViewPager(mViewPaper);
    }
    private void addEvents() {
        ivBack.setOnClickListener(v -> {
            finish();
        });

        btn_ExchangePoints.setOnClickListener(v -> {
            finish();
        });

        getPoints();
    }
    private void getPoints(){
        Intent intent = getIntent();
        int points = intent.getIntExtra("points",0);
        tvPoints.setText(points+"");
    }
}
