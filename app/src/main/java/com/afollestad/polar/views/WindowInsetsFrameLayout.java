package com.afollestad.polar.views;

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
import android.widget.FrameLayout;

import com.afollestad.polar.R;

public class WindowInsetsFrameLayout extends FrameLayout {

    private Rect mWindowInsets = new Rect();
    private Rect mTempInsets = new Rect();
    private WindowInsets mWindowInsetsLollipop;

    public WindowInsetsFrameLayout(Context context) {
        this(context, null, 0);
    }

    public WindowInsetsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WindowInsetsFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mWindowInsetsLollipop = insets;

        setWillNotDraw(false);

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (lp.insetsBottom) {
                child.setPadding(child.getPaddingLeft(), child.getPaddingTop(),
                        child.getPaddingRight(), child.getPaddingBottom() + mWindowInsetsLollipop.getSystemWindowInsetBottom());
            }
        }

        ViewCompat.postInvalidateOnAnimation(this);

        return insets;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        mWindowInsets.set(insets);

        setWillNotDraw(false);
        return true;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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

    public class LayoutParams extends FrameLayout.LayoutParams {

        public static final int FLAG_INSETS_TOP = 0x1;
        private static final int FLAG_INSETS_BOTTOM = 0x2;

        public boolean insetsTop;
        public boolean insetsBottom;
        public boolean insetsUseMargin;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            init(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        private void init(Context c, AttributeSet attrs) {
            TypedArray a = c.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.WindowInsetsFrameLayout_Layout,
                    0, 0);

            int insetsFlags = a.getInt(R.styleable.WindowInsetsFrameLayout_Layout_layout_windowInsets, 0);
            insetsTop = (insetsFlags & FLAG_INSETS_TOP) == FLAG_INSETS_TOP;
            insetsBottom = (insetsFlags & FLAG_INSETS_BOTTOM) == FLAG_INSETS_BOTTOM;
            insetsUseMargin = a.getBoolean(R.styleable.WindowInsetsFrameLayout_Layout_layout_windowInsetsUseMargin, false);

            a.recycle();
        }
    }
}