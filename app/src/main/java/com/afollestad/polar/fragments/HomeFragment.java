package com.afollestad.polar.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.base.BasePageFragment;

/**
 * @author Aidan Follestad (afollestad)
 */
public class HomeFragment extends BasePageFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView view = (TextView) inflater.inflate(R.layout.fragment_placeholder, container, false);
        view.setText(R.string.home);
        return view;
    }

    @Override
    public int getTitle() {
        return R.string.home;
    }
}