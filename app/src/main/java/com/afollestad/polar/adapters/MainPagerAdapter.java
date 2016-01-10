package com.afollestad.polar.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.AboutFragment;
import com.afollestad.polar.fragments.ApplyFragment;
import com.afollestad.polar.fragments.HomeFragment;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.fragments.RequestsFragment;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.viewer.FragmentStatePagerAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private final boolean mHomepageEnabled;

    public MainPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mHomepageEnabled = context.getResources().getBoolean(R.bool.homepage_enabled);
    }

    @Override
    protected Fragment getItem(int position) {
        if (!mHomepageEnabled)
            position++;
        switch (position) {
            default:
            case 0:
                return new HomeFragment();
            case 1:
                return new IconsFragment();
            case 2:
                return new WallpapersFragment();
            case 3:
                return new RequestsFragment();
            case 4:
                return new ApplyFragment();
            case 5:
                return new AboutFragment();
        }
    }

    @Override
    public int getCount() {
        int count = 5;
        if (mHomepageEnabled) count++;
        return count;
    }
}