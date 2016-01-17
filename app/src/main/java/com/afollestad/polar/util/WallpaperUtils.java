package com.afollestad.polar.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.afollestad.bridge.annotations.Body;
import com.afollestad.bridge.annotations.ContentType;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.inquiry.annotations.Column;
import com.afollestad.inquiry.callbacks.RunCallback;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.fragments.WallpapersFragment;

import java.io.Serializable;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperUtils {

    public static final String TABLE_NAME = "wallpapers";
    public static final String DATABASE_NAME = "data_cache";
    public static final int DATABASE_VERSION = 1;

    public interface WallpapersCallback {
        void onRetrievedWallpapers(WallpapersHolder wallpapers, Exception error, boolean cancelled);
    }

    @ContentType("application/json")
    public static class WallpapersHolder implements Serializable {

        public Wallpaper get(int index) {
            return wallpapers[index];
        }

        public int length() {
            return wallpapers != null ? wallpapers.length : 0;
        }

        @Body
        public Wallpaper[] wallpapers;

        public WallpapersHolder() {
        }

        public WallpapersHolder(Wallpaper[] wallpapers) {
            this.wallpapers = wallpapers;
        }
    }

    @ContentType("application/json")
    public static class Wallpaper implements Serializable {

        public Wallpaper() {
        }

        @Column(primaryKey = true, notNull = true, autoIncrement = true)
        public long _id;
        @Body
        @Column
        public String author;
        @Body
        @Column
        public String url;
        @Body
        @Column
        public String name;

        @Column
        private int paletteNameColor;
        private boolean setPaletteNameColor;
        @Column
        private int paletteAuthorColor;
        private boolean setPaletteAuthorColor;
        @Column
        private int paletteBgColor;
        private boolean setPaletteBgColor;

        public void setPaletteNameColor(@ColorInt int color) {
            this.paletteNameColor = color;
            this.setPaletteNameColor = true;
        }

        @ColorInt
        public int getPaletteNameColor() {
            return paletteNameColor;
        }

        public void setPaletteAuthorColor(@ColorInt int color) {
            this.paletteAuthorColor = color;
            this.setPaletteAuthorColor = true;
        }

        @ColorInt
        public int getPaletteAuthorColor() {
            return paletteAuthorColor;
        }

        public void setPaletteBgColor(@ColorInt int color) {
            this.paletteBgColor = color;
            this.setPaletteBgColor = true;
        }

        @ColorInt
        public int getPaletteBgColor() {
            return paletteBgColor;
        }

        public boolean isPaletteComplete() {
            return setPaletteBgColor && setPaletteAuthorColor && setPaletteNameColor;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static boolean didExpire(Context context) {
        final long NOW = System.currentTimeMillis();
        final String UPDATE_TIME_KEY = "wallpaper_last_update_time";
        final String LAST_UPDATE_VERSION_KEY = "wallpaper_last_update_version";
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final long LAST_UPDATE = prefs.getLong(UPDATE_TIME_KEY, -1);
        final long INTERVAL = 24 * 60 * 60 * 1000; // every 24 hours

        if (LAST_UPDATE == -1 || NOW >= (LAST_UPDATE + INTERVAL)) {
            Log.d("WallpaperUtils", "Cache invalid: never updated, or it's been 24 hours since last update.");
            // Never updated before, or it's been at least 24 hours
            prefs.edit().putLong(UPDATE_TIME_KEY, NOW).commit();
            return true;
        } else if (prefs.getInt(LAST_UPDATE_VERSION_KEY, -1) != BuildConfig.VERSION_CODE) {
            Log.d("WallpaperUtils", "App was updated, wallpapers cache is invalid.");
            prefs.edit().putInt(LAST_UPDATE_VERSION_KEY, BuildConfig.VERSION_CODE).commit();
            return true;
        }

        Log.d("WallpaperUtils", "Cache is still valid.");
        return false;
    }

    public static WallpapersHolder getAll(final Context context, boolean allowCached) throws Exception {
        Inquiry.init(context, DATABASE_NAME, DATABASE_VERSION);
        if (allowCached) {
            Wallpaper[] cache = Inquiry.get().selectFrom(TABLE_NAME, Wallpaper.class).all();
            if (cache != null && cache.length > 0) {
                Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from cache.", cache.length));
                return new WallpapersHolder(cache);
            }
        } else {
            Inquiry.get().deleteFrom(TABLE_NAME, Wallpaper.class).run();
        }

        try {
            WallpapersHolder holder = Bridge.get(context.getString(R.string.wallpapers_json_url))
                    .tag(WallpapersFragment.class.getName())
                    .asClass(WallpapersHolder.class);
            if (holder == null)
                throw new Exception("No wallpapers returned.");
            Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from web.", holder.length()));
            if (holder.length() > 0) {
                Inquiry.init(context, DATABASE_NAME, DATABASE_VERSION);
                Inquiry.get().insertInto(TABLE_NAME, Wallpaper.class)
                        .values(holder.wallpapers)
                        .run();
            }
            return holder;
        } catch (Exception e1) {
            Log.d("WallpaperUtils", String.format("Failed to load wallpapers... %s", e1.getMessage()));
            throw e1;
        } finally {
            Inquiry.deinit();
        }
    }

    public static void save(@Nullable final Context context, @Nullable final WallpapersHolder holder) {
        if (context == null || holder == null || holder.length() == 0) return;
        Inquiry.init(context, DATABASE_NAME, DATABASE_VERSION);
        Inquiry.get().deleteFrom(TABLE_NAME, Wallpaper.class).run();
        Inquiry.get().insertInto(TABLE_NAME, Wallpaper.class)
                .values(holder.wallpapers)
                .run(new RunCallback<Long[]>() {
                    @Override
                    public void result(Long[] changed) {
                        // Do nothing here
                    }
                });
    }

    public static void getAll(final Context context, boolean allowCached, final WallpapersCallback callback) {
        Inquiry.init(context, DATABASE_NAME, DATABASE_VERSION);
        if (allowCached) {
            Wallpaper[] cache = Inquiry.get().selectFrom(TABLE_NAME, Wallpaper.class).all();
            if (cache != null && cache.length > 0) {
                Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from cache.", cache.length));
                callback.onRetrievedWallpapers(new WallpapersHolder(cache), null, false);
                return;
            }
        } else {
            Inquiry.get().deleteFrom(TABLE_NAME, Wallpaper.class).run();
        }

        Bridge.get(context.getString(R.string.wallpapers_json_url))
                .tag(WallpapersFragment.class.getName())
                .asClass(WallpapersHolder.class, new ResponseConvertCallback<WallpapersHolder>() {
                    @Override
                    public void onResponse(@NonNull Response response, @Nullable WallpapersHolder holder, @Nullable BridgeException e) {
                        if (e != null) {
                            callback.onRetrievedWallpapers(null, e, e.reason() == BridgeException.REASON_REQUEST_CANCELLED);
                        } else {
                            if (holder == null) {
                                callback.onRetrievedWallpapers(null, new Exception("No wallpapers returned."), false);
                                return;
                            }
                            try {
                                Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from web.", holder.length()));
                                if (holder.length() > 0) {
                                    Inquiry.init(context, DATABASE_NAME, DATABASE_VERSION);
                                    Inquiry.get().insertInto(TABLE_NAME, Wallpaper.class)
                                            .values(holder.wallpapers)
                                            .run();
                                }
                                callback.onRetrievedWallpapers(holder, null, false);
                            } catch (Exception e1) {
                                Log.d("WallpaperUtils", String.format("Failed to load wallpapers... %s", e1.getMessage()));
                                callback.onRetrievedWallpapers(null, e1, false);
                            } finally {
                                Inquiry.deinit();
                            }
                        }
                    }
                });
    }

    private WallpaperUtils() {
    }
}