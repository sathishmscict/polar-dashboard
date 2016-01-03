package com.afollestad.polar.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.afollestad.polar.fragments.AboutFragment;
import com.afollestad.polar.fragments.ApplyFragment;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.fragments.RequestsFragment;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.viewer.FragmentStatePagerAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected Fragment getItem(int position) {
        switch (position) {
            default:
                return new IconsFragment();
            case 1:
                return new WallpapersFragment();
            case 2:
                return new RequestsFragment();
            case 3:
                return new ApplyFragment();
            case 4:
                return new AboutFragment();
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}