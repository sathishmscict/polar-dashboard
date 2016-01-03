package com.afollestad.polar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.afollestad.polar.adapters.WallpaperAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperNameView extends TextView {

    public WallpaperNameView(Context context) {
        super(context);
    }

    public WallpaperNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private WallpaperAdapter.WallpaperViewHolder mViewHolder;

    public void setViewHolder(WallpaperAdapter.WallpaperViewHolder viewHolder) {
        mViewHolder = viewHolder;
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mViewHolder != null)
            mViewHolder.mutedDarkColor = color;
    }
}
