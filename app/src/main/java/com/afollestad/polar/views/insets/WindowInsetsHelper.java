package com.afollestad.polar.views.insets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.afollestad.polar.R;

public class WindowInsetsHelper {

    public static final int FLAG_INSETS_TOP = 0x1;
    public static final int FLAG_INSETS_BOTTOM = 0x2;
    private static final String TAG_INSETS_APPLIED = "insets_applied";
    private boolean mInsetsTop;
    private boolean mInsetsBottom;
    private boolean mInsetsUseMargin;

    private ViewGroup mView;

    public WindowInsetsHelper(Context context, AttributeSet attrs, ViewGroup view) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WindowInsetsLayout,
                0, 0);

        int insetsFlags = a.getInt(R.styleable.WindowInsetsLayout_windowInsets, 0);
        mInsetsTop = (insetsFlags & FLAG_INSETS_TOP) == FLAG_INSETS_TOP;
        mInsetsBottom = (insetsFlags & FLAG_INSETS_BOTTOM) == FLAG_INSETS_BOTTOM;
        mInsetsUseMargin = a.getBoolean(R.styleable.WindowInsetsLayout_windowInsetsUseMargin, false);

        a.recycle();

        mView = view;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {

        Rect rect = new Rect(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());

        for (int i = 0; i < mView.getChildCount(); i++) {
            final View child = mView.getChildAt(i);

            if (child instanceof InsetsViewGroup) {
                child.dispatchApplyWindowInsets(insets);
            } else {
                InsetsLayoutParamsHelper helper = ((InsetsLayoutParams) child.getLayoutParams()).getHelper();
                applyInsets(rect, child, helper.insetsTop, helper.insetsBottom, helper.insetsUseMargin);
            }
        }

        applyInsets(rect, mView, mInsetsTop, mInsetsBottom, mInsetsUseMargin);

        ViewCompat.postInvalidateOnAnimation(mView);

        return insets;
    }

    public boolean fitSystemWindows(Rect insets) {
        for (int i = 0; i < mView.getChildCount(); i++) {
            final View child = mView.getChildAt(i);

            if (child instanceof InsetsViewGroup) {
                ((InsetsViewGroup) child).dispatchFitSystemWindows(insets);
            } else {
                InsetsLayoutParamsHelper helper = ((InsetsLayoutParams) child.getLayoutParams()).getHelper();
                applyInsets(insets, child, helper.insetsTop, helper.insetsBottom, helper.insetsUseMargin);
            }
        }

        applyInsets(insets, mView, mInsetsTop, mInsetsBottom, mInsetsUseMargin);

        ViewCompat.postInvalidateOnAnimation(mView);

        return false;
    }

    private void applyInsets(Rect insets, View view, boolean insetsTop, boolean insetsBottom, boolean useMargin) {
        if (view.getTag() != null && view.getTag().equals(TAG_INSETS_APPLIED)) {
            return;
        }

        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        ViewGroup.MarginLayoutParams marginLp = null;

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            marginLp = (ViewGroup.MarginLayoutParams) lp;
        } else if (useMargin) {
            return;
        }

        int paddingTop = useMargin ? marginLp.topMargin : view.getPaddingTop();
        int paddingBottom = useMargin ? marginLp.bottomMargin : view.getPaddingBottom();

        if (insetsTop) {
            paddingTop += insets.top;
        }

        if (insetsBottom) {
            paddingBottom += insets.bottom;
        }

        if (useMargin) {
            marginLp.topMargin = paddingTop;
            marginLp.bottomMargin = paddingBottom;
            view.setLayoutParams(lp);
        } else {
            view.setPadding(view.getPaddingLeft(), paddingTop, view.getPaddingRight(), paddingBottom);
        }

        view.setTag(TAG_INSETS_APPLIED);
    }
}
