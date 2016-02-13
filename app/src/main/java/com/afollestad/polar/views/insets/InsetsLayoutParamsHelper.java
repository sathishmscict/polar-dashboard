package com.afollestad.polar.views.insets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.afollestad.polar.R;

public class InsetsLayoutParamsHelper {

    public boolean insetsTop;
    public boolean insetsBottom;
    public boolean insetsUseMargin;

    public InsetsLayoutParamsHelper(Context c, AttributeSet attrs) {
        if (c == null) {
            return;
        }

        TypedArray a = c.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WindowInsetsLayout_Layout,
                0, 0);

        int insetsFlags = a.getInt(R.styleable.WindowInsetsLayout_Layout_layout_windowInsets, 0);
        insetsTop = (insetsFlags & WindowInsetsHelper.FLAG_INSETS_TOP) == WindowInsetsHelper.FLAG_INSETS_TOP;
        insetsBottom = (insetsFlags & WindowInsetsHelper.FLAG_INSETS_BOTTOM) == WindowInsetsHelper.FLAG_INSETS_BOTTOM;
        insetsUseMargin = a.getBoolean(R.styleable.WindowInsetsLayout_Layout_layout_windowInsetsUseMargin, false);

        a.recycle();
    }
}
