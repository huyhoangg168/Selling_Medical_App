package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.R;

public class HealthyNewsDetailActivity extends AppCompatActivity {
    private Context mContext;
    WebView wvHealthyNews;

    ProgressBar progressBar;
    ImageView ivBack;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.healthy_news_detail);

        addControl();
        addEvents();

    }

    private void addControl() {
        wvHealthyNews = findViewById(R.id.wvHealthyNews);
        ivBack = findViewById(R.id.ivBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void addEvents() {
        ivBack.setOnClickListener(v -> {
            finish();
        });


        wvHealthyNews.loadUrl(getIntent().getStringExtra("url"));
        wvHealthyNews.getSettings().setJavaScriptEnabled(true);
        wvHealthyNews.setWebViewClient(new WebViewClient() {
           @Override
           public void onPageStarted(WebView view, String url, Bitmap favicon) {
               // Trang web bắt đầu tải
               progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar
              }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                // Trang web đã sẵn sàng để hiển thị
                progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
            }



        });
    }

}
