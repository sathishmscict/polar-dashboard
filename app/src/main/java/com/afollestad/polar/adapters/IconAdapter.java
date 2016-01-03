package com.afollestad.polar.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.bumptech.glide.Glide;
import com.afollestad.polar.R;
import com.afollestad.polar.ui.IconMoreActivity;
import com.afollestad.polar.util.DrawableXmlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IconAdapter extends SectionedRecyclerViewAdapter<IconAdapter.MainViewHolder>
        implements View.OnClickListener {

    public final static int SEARCH_RESULT_LIMIT = 20;

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            if (view.getTag() instanceof String) {
                // Grid item
                final String[] tag = view.getTag().toString().split(":");
                mListener.onClick(view,
                        Integer.parseInt(tag[0]),
                        Integer.parseInt(tag[1]),
                        Integer.parseInt(tag[2]));
            } else {
                // 'More' button
                final int index = (Integer) view.getTag();
                final DrawableXmlParser.Category category = mFiltered != null ?
                        mFiltered.get(index) : mCategories.get(index);
                final int[] location = new int[2];
                view.getLocationInWindow(location);
                location[0] += view.getMeasuredWidth() / 2;
                mContext.startActivity(new Intent(mContext, IconMoreActivity.class)
                        .putExtra("category", category)
                        .putExtra("button_location", location));
            }
        }
    }

    public interface ClickListener {
        void onClick(View view, int section, int relative, int absolute);
    }

    public IconAdapter(Context context, int gridWidth, ClickListener listener) {
        mContext = context;
        mIconsPerSection = gridWidth * 2;
        mListener = listener;
        mCategories = new ArrayList<>();
    }

    private final Context mContext;
    private final int mIconsPerSection;
    private final ClickListener mListener;
    private final ArrayList<DrawableXmlParser.Category> mCategories;
    private ArrayList<DrawableXmlParser.Category> mFiltered;

    public void filter(String str) {
        if (str == null || str.trim().isEmpty()) {
            mFiltered = null;
            notifyDataSetChanged();
            return;
        }

        str = str.toLowerCase(Locale.getDefault());
        mFiltered = new ArrayList<>();

        for (DrawableXmlParser.Category cat : mCategories) {
            DrawableXmlParser.Category include = null;
            for (DrawableXmlParser.Icon icon : cat.getIcons()) {
                if (mFiltered.size() == SEARCH_RESULT_LIMIT)
                    break; // limit number of search results to reduce computation time
                if (icon.getName().toLowerCase(Locale.getDefault()).contains(str)) {
                    if (include != null) {
                        if (include.getName().equalsIgnoreCase(icon.getCategory().getName())) {
                            include.addItem(icon);
                        } else {
                            mFiltered.add(include);
                            include = null;
                        }
                    }
                    if (include == null) {
                        include = new DrawableXmlParser.Category(icon.getCategory().getName());
                        include.addItem(icon);
                    }
                }
            }
            if (include != null) mFiltered.add(include);
        }

        if (mFiltered.size() == 0)
            mFiltered = null;
        notifyDataSetChanged();
    }

    public void set(List<DrawableXmlParser.Category> categories) {
        mCategories.clear();
        mCategories.addAll(categories);
        notifyDataSetChanged();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public MainViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            moreButton = (Button) itemView.findViewById(R.id.moreButton);
        }

        final ImageView image;
        final TextView title;
        final Button moreButton;
    }

    public DrawableXmlParser.Icon getIcon(int section, int relative) {
        final DrawableXmlParser.Category category = mFiltered != null ?
                mFiltered.get(section) : mCategories.get(section);
        return category.getIcons().get(relative);
    }

    @Override
    public int getSectionCount() {
        return mFiltered != null ? mFiltered.size() : mCategories.size();
    }

    @Override
    public int getItemCount(int section) {
        int count = mFiltered != null ? mFiltered.get(section).size() : mCategories.get(section).size();
        if (count > mIconsPerSection) return mIconsPerSection;
        return count;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, boolean header) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                header ? R.layout.list_item_icon_header : R.layout.list_item_icon, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(MainViewHolder holder, int section) {
        final DrawableXmlParser.Category category = mFiltered != null ?
                mFiltered.get(section) : mCategories.get(section);
        holder.title.setText(category.getName());

        if (category.size() > mIconsPerSection) {
            holder.moreButton.setVisibility(View.VISIBLE);
            holder.moreButton.setTag(section);
            holder.moreButton.setOnClickListener(this);
            holder.moreButton.setText(holder.itemView.getContext().getString(
                    R.string.more_x, category.size() - mIconsPerSection));
        } else {
            holder.moreButton.setVisibility(View.GONE);
            holder.moreButton.setTag(null);
            holder.moreButton.setOnClickListener(null);
        }
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int section, int relativePos, int absolutePos) {
        final Context c = holder.itemView.getContext();
        final DrawableXmlParser.Category category = mFiltered != null ?
                mFiltered.get(section) : mCategories.get(section);
        final int res = category.getIcons().get(relativePos).getId(c);

        if (res == 0) {
            holder.image.setBackgroundColor(Color.parseColor("#40000000"));
        } else {
            holder.image.setBackground(null);
            Glide.with(c)
                    .fromResource()
                    .load(res)
                    .into(holder.image);
        }

        holder.itemView.setTag(String.format("%d:%d:%d", section, relativePos, absolutePos));
        holder.itemView.setOnClickListener(this);
    }
}