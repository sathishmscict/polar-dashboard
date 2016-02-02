package com.afollestad.polar.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.afollestad.polar.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Config implements IConfig {

    private Config(@Nullable Context context) {
        mR = null;
        mContext = context;
        if (context != null)
            mR = context.getResources();
    }

    private static Config mConfig;
    private Context mContext;
    private Resources mR;

    public static void init(@NonNull Context context) {
        mConfig = new Config(context);
    }

    private void destroy() {
        mR = null;
    }

    public static void deinit() {
        mConfig.destroy();
        mConfig = null;
    }

    @NonNull
    public static IConfig get() {
        if (mConfig == null)
            return new Config(null); // shouldn't ever happen, but avoid crashes
        return mConfig;
    }

    // Getters

    private SharedPreferences prefs() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public boolean allowThemeSwitching() {
        return mR != null && mR.getBoolean(R.bool.allow_theme_switching);
    }

    @Override
    public boolean darkTheme() {
        if (!Config.get().allowThemeSwitching())
            darkTheme(darkThemeDefault());
        return prefs().getBoolean("config_dark_theme", darkThemeDefault());
    }

    @Override
    public void darkTheme(boolean enabled) {
        prefs().edit().putBoolean("config_dark_theme", enabled).commit();
    }

    @Override
    public boolean darkThemeDefault() {
        return mR != null && mR.getBoolean(R.bool.dark_theme_default);
    }

    @Override
    public boolean navDrawerModeEnabled() {
        return !(mR == null || mContext == null) &&
                prefs().getBoolean("nav_drawer_mode", mR.getBoolean(R.bool.nav_drawer_mode_default));
    }

    @Override
    public void navDrawerModeEnabled(boolean enabled) {
        if (mR == null || mContext == null) return;
        prefs().edit().putBoolean("nav_drawer_mode", enabled).commit();
    }

    @Override
    public boolean navDrawerModeAllowSwitch() {
        return mR != null && mR.getBoolean(R.bool.allow_nav_drawer_mode_switch);
    }

    @Override
    public boolean homepageEnabled() {
        return mR == null || mR.getBoolean(R.bool.homepage_enabled);
    }

    @Nullable
    @Override
    public String homepageDescription() {
        if (mR == null) return null;
        return mR.getString(R.string.homepage_description);
    }

    @DrawableRes
    @Override
    public Drawable homepageLandingIcon() {
        if (mR == null) return null;
        return ContextCompat.getDrawable(mContext, R.drawable.homepage_landing_icon);
    }

    @Nullable
    @Override
    public String wallpapersJsonUrl() {
        if (mR == null) return null;
        return mR.getString(R.string.wallpapers_json_url);
    }

    @Override
    public boolean zooperEnabled() {
        return mR != null && mR.getBoolean(R.bool.zooper_enabled);
    }

    @Nullable
    @Override
    public String iconRequestEmail() {
        if (mR == null) return null;
        return mR.getString(R.string.icon_request_email);
    }

    @Override
    public boolean iconRequestEnabled() {
        final String requestEmail = iconRequestEmail();
        return requestEmail != null && !requestEmail.trim().isEmpty();
    }

    @Nullable
    @Override
    public String licensingPublicKey() {
        if (mR == null) return null;
        return mR.getString(R.string.licensing_public_key);
    }

    @Override
    public boolean persistSelectedPage() {
        return mR == null || mR.getBoolean(R.bool.persist_selected_page);
    }

    @Override
    public boolean changelogEnabled() {
        return mR != null && mR.getBoolean(R.bool.changelog_enabled);
    }

    @Override
    public int gridWidthWallpaper() {
        if (mR == null) return 2;
        return mR.getInteger(R.integer.wallpaper_grid_width);
    }

    @Override
    public int gridWidthApply() {
        if (mR == null) return 3;
        return mR.getInteger(R.integer.apply_grid_width);
    }

    @Override
    public int gridWidthIcons() {
        if (mR == null) return 4;
        return mR.getInteger(R.integer.icon_grid_width);
    }

    @Override
    public int gridWidthRequests() {
        if (mR == null) return 3;
        return mR.getInteger(R.integer.requests_grid_width);
    }
}
