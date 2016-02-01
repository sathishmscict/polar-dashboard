package com.afollestad.polar.fragments.base;

import android.app.Fragment;
import android.support.annotation.StringRes;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

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

    static final Object LOCK = new Object();

    /**
     * Applies window insets apart from the top inset to a ViewGroup's direct children, if they have
     * fitsSystemWindows set.
     * <p/>
     * Must be called in/after onViewCreated
     */
    protected void applyInsets(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child.getFitsSystemWindows()) {
                applyInsetsToView(child);
            }
        }
    }

    protected void applyInsetsToViewMargin(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            private int mInitialBottom = -1;

            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                synchronized (LOCK) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    if (mInitialBottom == -1)
                        mInitialBottom = layoutParams.bottomMargin;
                    layoutParams.bottomMargin = mInitialBottom + insets.getSystemWindowInsetBottom();
                    v.setLayoutParams(layoutParams);
                    return insets;
                }
            }
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Applies any window insets apart from the top inset to the view.
     * <p/>
     * Must be called in/after onViewCreated
     */
    protected void applyInsetsToView(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            private int mInitialBottom = -1;

            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                synchronized (LOCK) {
                    if (mInitialBottom == -1)
                        mInitialBottom = v.getPaddingBottom();
                    v.setPaddingRelative(v.getPaddingStart(), v.getPaddingTop(),
                            v.getPaddingEnd(), mInitialBottom + insets.getSystemWindowInsetBottom());
                    return insets;
                }
            }
        });
        ViewCompat.requestApplyInsets(view);
    }
}