package com.afollestad.polar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.afollestad.polar.R;
import com.afollestad.polar.util.DrawableXmlParser;

import java.util.ArrayList;
import java.util.List;

public class IconMoreAdapter extends RecyclerView.Adapter<IconMoreAdapter.MainViewHolder>
        implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            mListener.onClick(view, Integer.parseInt(view.getTag().toString()));
        }
    }

    public interface ClickListener {
        void onClick(View view, int index);
    }

    public IconMoreAdapter(ClickListener listener) {
        mListener = listener;
        mIcons = new ArrayList<>();
    }

    private final ClickListener mListener;
    private final ArrayList<DrawableXmlParser.Icon> mIcons;

    public void set(List<DrawableXmlParser.Icon> icons) {
        mIcons.clear();
        mIcons.addAll(icons);
        notifyDataSetChanged();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public MainViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }

        final ImageView image;
    }

    public DrawableXmlParser.Icon getIcon(int index) {
        return mIcons.get(index);
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_icon, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        final Context c = holder.itemView.getContext();
        final int res = mIcons.get(position).getId(c);

        if (res == 0) {
            holder.image.setBackgroundColor(Color.parseColor("#40000000"));
        } else {
            holder.image.setBackground(null);
            Glide.with(c)
                    .fromResource()
                    .load(res)
                    .into(holder.image);
        }

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
    }
}