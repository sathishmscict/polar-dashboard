package com.afollestad.polar.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.AboutFragment;
import com.afollestad.polar.fragments.ApplyFragment;
import com.afollestad.polar.fragments.HomeFragment;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.fragments.RequestsFragment;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.fragments.ZooperFragment;
import com.afollestad.polar.util.PagesBuilder;
import com.afollestad.polar.viewer.FragmentStatePagerAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

    private final PagesBuilder mPages;

    public MainPagerAdapter(FragmentManager fm, PagesBuilder pages) {
        super(fm);
        mPages = pages;
    }

    @Override
    protected Fragment getItem(int position) {
        return mPages.get(position).fragment;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }
}