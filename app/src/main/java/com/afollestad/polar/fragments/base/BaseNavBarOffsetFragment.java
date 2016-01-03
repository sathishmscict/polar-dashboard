package com.afollestad.polar.fragments.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.polar.R;
import com.afollestad.polar.ui.MainActivity;

import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class BaseNavBarOffsetFragment extends Fragment {

    private RecyclerView list;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = ButterKnife.findById(view, android.R.id.list);
        if (list != null) {
            list.setClipToPadding(false);
            list.setPadding(list.getPaddingLeft(),
                    list.getPaddingTop(),
                    list.getPaddingRight(),
                    list.getPaddingBottom() + getResources().getDimensionPixelOffset(R.dimen.nav_bar_offset));
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getActivity() instanceof MainActivity && isVisibleToUser)
            ((MainActivity) getActivity()).mRecyclerView = list;
    }
}