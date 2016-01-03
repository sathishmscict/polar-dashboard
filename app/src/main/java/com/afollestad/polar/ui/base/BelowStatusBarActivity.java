package com.afollestad.polar.ui.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.polar.R;
import com.afollestad.polar.util.Utils;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BelowStatusBarActivity extends BaseThemedActivity {

    @LayoutRes
    public abstract int getLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        final View root = findViewById(R.id.root);
        final int paddingTop = getResources().getBoolean(R.bool.translucent_nav) ? Utils.getStatusBarHeight(this) : 0;
        root.setPadding(root.getPaddingLeft(),
                paddingTop,
                root.getPaddingRight(),
                root.getPaddingBottom());
    }
}
