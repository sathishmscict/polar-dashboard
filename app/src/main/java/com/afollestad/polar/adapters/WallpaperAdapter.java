package com.afollestad.polar.adapters;

import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.polar.R;
import com.afollestad.polar.util.WallpaperUtils;
import com.afollestad.polar.views.WallpaperAuthorView;
import com.afollestad.polar.views.WallpaperBgFrame;
import com.afollestad.polar.views.WallpaperNameView;
import com.bumptech.glide.Glide;
import com.github.florent37.glidepalette.GlidePalette;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;


public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {

    public final static int SEARCH_RESULT_LIMIT = 10;

    public interface ClickListener {
        boolean onClick(View view, int index, boolean longPress);
    }

    public WallpaperAdapter(ClickListener listener) {
        mListener = listener;
    }

    private final ClickListener mListener;
    private WallpaperUtils.WallpapersHolder mWallpapers;
    private ArrayList<WallpaperUtils.Wallpaper> mFiltered;

    public void filter(String str) {
        if (str == null) {
            mFiltered = null;
            notifyDataSetChanged();
            return;
        }
        str = str.toLowerCase(Locale.getDefault());
        mFiltered = new ArrayList<>();
        for (WallpaperUtils.Wallpaper wallpaper : mWallpapers.wallpapers) {
            if (mFiltered.size() == SEARCH_RESULT_LIMIT)
                break;
            if (wallpaper.name.toLowerCase(Locale.getDefault()).contains(str) ||
                    wallpaper.author.toLowerCase(Locale.getDefault()).contains(str)) {
                mFiltered.add(wallpaper);
            }
        }
        if (mFiltered.size() == 0)
            mFiltered = null;
        notifyDataSetChanged();
    }

    public void set(WallpaperUtils.WallpapersHolder holder) {
        mWallpapers = holder;
        notifyDataSetChanged();
    }

    public static class WallpaperViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public WallpaperViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mListener = listener;
            card = ButterKnife.findById(itemView, R.id.card);
            image = ButterKnife.findById(itemView, R.id.image);
            colorFrame = ButterKnife.findById(itemView, R.id.colorFrame);
            name = ButterKnife.findById(itemView, R.id.name);
            author = ButterKnife.findById(itemView, R.id.author);

            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
        }

        final ClickListener mListener;
        final CardView card;
        final ImageView image;
        final WallpaperBgFrame colorFrame;
        final WallpaperNameView name;
        final WallpaperAuthorView author;

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View v) {
            return mListener.onClick(v, getAdapterPosition(), true);
        }
    }

    @Override
    public WallpaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_wallpaper, parent, false);
        return new WallpaperViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(WallpaperViewHolder holder, int index) {
        final WallpaperUtils.Wallpaper wallpaper = mFiltered != null ? mFiltered.get(index) : mWallpapers.get(index);
        holder.name.setText(wallpaper.name);
        holder.author.setText(wallpaper.author);

        ViewCompat.setTransitionName(holder.image, "view_" + index);

        holder.name.setWallpaper(wallpaper);
        holder.colorFrame.setWallpaper(wallpaper);

        // Color caches, set from the associated views
        if (wallpaper.isPaletteComplete()) {
            holder.name.setTextColor(wallpaper.paletteNameColor);
            holder.author.setTextColor(wallpaper.paletteAuthorColor);
            holder.colorFrame.setBackgroundColor(wallpaper.paletteBgColor);
            Glide.with(holder.itemView.getContext())
                    .load(wallpaper.url)
                    .into(holder.image);
        } else {
            holder.name.setTextColor(Color.WHITE, false);
            holder.author.setTextColor(Color.WHITE, false);
            holder.colorFrame.setBackgroundColor(Color.DKGRAY, false);
            Glide.with(holder.itemView.getContext())
                    .load(wallpaper.url)
                    .listener(GlidePalette.with(wallpaper.url)
                            .use(GlidePalette.Profile.VIBRANT)
                            .intoBackground(holder.colorFrame)
                            .intoTextColor(holder.name, GlidePalette.Swatch.TITLE_TEXT_COLOR)
                            .intoTextColor(holder.author, GlidePalette.Swatch.BODY_TEXT_COLOR)
                    ).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        if (mFiltered != null)
            return mFiltered.size();
        return mWallpapers.length();
    }
}