package com.afollestad.polar.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.base.BasePageFragment;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ZooperFragment extends BasePageFragment {

    @Override
    public int getTitle() {
        return R.string.zooper;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zooper_placeholder, container, false);
    }
}