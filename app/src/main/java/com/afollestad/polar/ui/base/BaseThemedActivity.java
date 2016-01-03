package com.afollestad.polar.ui.base;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.assent.AssentActivity;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseThemedActivity extends AssentActivity {

    private static final String CONFIG_DARK_THEME = "config_dark_theme";

    private boolean mLastDarkTheme = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mLastDarkTheme = darkTheme();
        setTheme(getCurrentTheme());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLastDarkTheme != darkTheme())
            recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        themeMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected boolean isTranslucent() {
        return false;
    }

    @StyleRes
    private int getCurrentTheme() {
        if (isTranslucent()) {
            if (!mLastDarkTheme) {
                if (ContextCompat.getColor(this, R.color.primary_1_light) ==
                        ContextCompat.getColor(this, R.color.default_primary_lighttheme)) {
                    return R.style.AppTheme_Light_White_Translucent;
                } else {
                    return R.style.AppTheme_Light_Translucent;
                }
            }
            return R.style.AppTheme_Dark_Translucent;
        } else {
            if (!mLastDarkTheme) {
                if (ContextCompat.getColor(this, R.color.primary_1_light) ==
                        ContextCompat.getColor(this, R.color.default_primary_lighttheme)) {
                    return R.style.AppTheme_Light_White;
                } else {
                    return R.style.AppTheme_Light;
                }
            }
            return R.style.AppTheme_Dark;
        }
    }

    public final void darkTheme(boolean newValue) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(CONFIG_DARK_THEME, newValue).commit();
    }

    public final boolean darkTheme() {
        if (!getResources().getBoolean(R.bool.allow_theme_switching))
            darkTheme(getResources().getBoolean(R.bool.dark_theme_default));
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(CONFIG_DARK_THEME, getResources().getBoolean(R.bool.dark_theme_default));
    }

    public static void themeMenu(Context context, Menu menu) {
        final int tintColor = DialogUtils.resolveColor(context, R.attr.tab_icon_color);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null)
                item.getIcon().setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
        }
    }
}