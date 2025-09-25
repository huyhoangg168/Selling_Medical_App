package com.example.clientsellingmedicine.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.clientsellingmedicine.DTO.District;
import com.example.clientsellingmedicine.DTO.Province;
import com.example.clientsellingmedicine.DTO.Ward;

import java.util.List;

public class SubdivisionsAdapter extends ArrayAdapter<Object> {

    private static final int VIEW_TYPE_PROVINCE = 0;
    private static final int VIEW_TYPE_DISTRICT = 1;
    private static final int VIEW_TYPE_WARD = 2;

    public SubdivisionsAdapter(Context context, List<Object> list) {
        super(context,0,list);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof Province) {
            return VIEW_TYPE_PROVINCE;
        } else if (item instanceof District) {
            return VIEW_TYPE_DISTRICT;
        } else if (item instanceof Ward) {
            return VIEW_TYPE_WARD;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        int viewType = getItemViewType(position);
        Object item = getItem(position);

        switch (viewType) {
            case VIEW_TYPE_PROVINCE:
                Province province = (Province) item;
                TextView provinceTextView = convertView.findViewById(android.R.id.text1);
                provinceTextView.setText(province.getName());
                break;
            case VIEW_TYPE_DISTRICT:
                District district = (District) item;
                TextView districtTextView = convertView.findViewById(android.R.id.text1);
                districtTextView.setText(district.getName());

                break;
            case VIEW_TYPE_WARD:
                Ward ward = (Ward) item;
                TextView wardTextView = convertView.findViewById(android.R.id.text1);
                wardTextView.setText(ward.getName());
                break;
        }

        return convertView;

    }

}




