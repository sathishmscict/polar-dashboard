package com.afollestad.polar.views;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.polar.adapters.WallpaperAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperColorFrame extends LinearLayout {

    public WallpaperColorFrame(Context context) {
        super(context);
    }

    public WallpaperColorFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperColorFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private WallpaperAdapter.WallpaperViewHolder mViewHolder;

    public void setViewHolder(WallpaperAdapter.WallpaperViewHolder viewHolder) {
        mViewHolder = viewHolder;
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        super.setBackgroundColor(color);
        ViewGroup parent = (ViewGroup) getParent();
        CardView card = (CardView) parent.getParent();
        card.setCardBackgroundColor(color);
        if (mViewHolder != null)
            mViewHolder.vibrantColor = color;
    }
}
