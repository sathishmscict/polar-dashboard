package com.afollestad.polar.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.polar.R;
import com.afollestad.polar.adapters.AboutAdapter;
import com.afollestad.polar.fragments.base.BasePageFragment;

import butterknife.ButterKnife;


public class AboutFragment extends BasePageFragment {

    private RecyclerView mRecyclerView;

    public AboutFragment() {
    }

    @Override
    public int getTitle() {
        return R.string.about;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_recyclerview_bare, container, false);
        mRecyclerView = ButterKnife.findById(v, android.R.id.list);

        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        final AboutAdapter mAdapter = new AboutAdapter(getActivity());
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyInsetsToView(mRecyclerView);
    }
}