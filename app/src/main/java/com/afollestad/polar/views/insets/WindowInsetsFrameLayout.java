package com.afollestad.polar.views.insets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

public class WindowInsetsFrameLayout extends FrameLayout implements InsetsViewGroup {

    private WindowInsetsHelper mHelper;

    public WindowInsetsFrameLayout(Context context) {
        this(context, null, 0);
    }

    public WindowInsetsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WindowInsetsFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

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
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public void dispatchFitSystemWindows(Rect insets) {
        mHelper.fitSystemWindows(insets);
    }

    public class LayoutParams extends FrameLayout.LayoutParams implements InsetsLayoutParams {

        private InsetsLayoutParamsHelper mHelper;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            mHelper = new InsetsLayoutParamsHelper(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @Override
        public InsetsLayoutParamsHelper getHelper() {
            return mHelper;
        }
    }
}