package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsellingmedicine.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Context mContext;
    private EditText edtNumber1,edtNumber2,edtNumber3,edtNumber4,
    edtNumber5,edtNumber6,edtNumber7,edtNumber8;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.otp_confirm_screen);

        addControl();
        addEvents();

    }

    private void addEvents() {
    }

    private void addControl() {
        edtNumber1 = findViewById(R.id.edtConfimNumber1);
        edtNumber2 = findViewById(R.id.edtConfimNumber2);
        edtNumber3 = findViewById(R.id.edtConfimNumber3);
        edtNumber4 = findViewById(R.id.edtConfimNumber4);
        edtNumber5 = findViewById(R.id.edtConfimNumber5);
        edtNumber6 = findViewById(R.id.edtConfimNumber6);

    }




}
