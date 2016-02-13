package com.afollestad.polar.views.insets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;

public class WindowInsetsLinearLayout extends LinearLayout implements InsetsViewGroup {

    private Rect mWindowInsets = new Rect();
    private Rect mTempInsets = new Rect();

    private WindowInsetsHelper mHelper;


    public WindowInsetsLinearLayout(Context context) {
        this(context, null, 0);
    }

    public WindowInsetsLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WindowInsetsLinearLayout(Context context, AttributeSet attrs, int defStyle) {
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
        //super.fitSystemWindows(insets);
        mWindowInsets.set(insets);

        // setWillNotDraw(false);
        return false;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

    }


    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public class LayoutParams extends LinearLayout.LayoutParams implements InsetsLayoutParams {

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