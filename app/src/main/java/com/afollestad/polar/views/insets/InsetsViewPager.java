package com.afollestad.polar.views.insets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowInsets;

public class InsetsViewPager extends ViewPager implements InsetsViewGroup {

    private WindowInsetsHelper mHelper;

    public InsetsViewPager(Context context) {
        this(context, null);
    }

    public InsetsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHelper = new WindowInsetsHelper(context, attrs, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            requestApplyInsets();
        } else {
            requestFitSystemWindows();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return mHelper.onApplyWindowInsets(insets);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mHelper.fitSystemWindows(insets);
        return false;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public void dispatchFitSystemWindows(Rect insets) {
        mHelper.fitSystemWindows(insets);
    }

    public class LayoutParams extends ViewPager.LayoutParams implements InsetsLayoutParams {

        private InsetsLayoutParamsHelper mHelper;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            mHelper = new InsetsLayoutParamsHelper(c, attrs);
        }

        public LayoutParams() {
            super();
        }

        @Override
        public InsetsLayoutParamsHelper getHelper() {
            return mHelper;
        }
    }
}
