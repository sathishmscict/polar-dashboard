package com.afollestad.polar.fragments.base;

import android.app.Fragment;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;

import com.afollestad.polar.ui.base.BaseThemedActivity;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BasePageFragment extends Fragment {

    @StringRes
    public abstract int getTitle();

    public void updateTitle() {
        if (getActivity() != null)
            getActivity().setTitle(getTitle());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            updateTitle();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() != null)
            BaseThemedActivity.themeMenu(getActivity(), menu);
    }
}