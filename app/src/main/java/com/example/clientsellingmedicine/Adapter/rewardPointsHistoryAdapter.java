package com.example.clientsellingmedicine.Adapter;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.clientsellingmedicine.activity.AccumulateFragment;
import com.example.clientsellingmedicine.activity.RedeemFragment;

public class rewardPointsHistoryAdapter extends FragmentStatePagerAdapter {

    public rewardPointsHistoryAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm,behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:
                return new RedeemFragment();
            default:
                return new AccumulateFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position){
            case 0:
                title = "Tích điểm";
                break;

            case 1:
                title = "Tiêu điểm";
                break;
        }
        return title;
    }
}
