package com.example.clientsellingmedicine.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.clientsellingmedicine.R;

public class CategoryFragment extends Fragment {
    private Context mContext;
    public CategoryFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_screen, container, false);
        // Thực hiện các thao tác cần thiết trên giao diện view của fragment
        addControl();
        addEvents();
        return view;


    }



    private void addControl(){

    }
    private void addEvents(){

    }
}
