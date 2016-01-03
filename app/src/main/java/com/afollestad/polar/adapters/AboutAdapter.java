package com.afollestad.polar.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.afollestad.polar.R;

import butterknife.ButterKnife;


public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.MainViewHolder> implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof String) {
            try {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse((String) view.getTag())));
            } catch (Exception e) {
                Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public AboutAdapter(Activity context) {
        mContext = context;
        mTitles = context.getResources().getStringArray(R.array.about_titles);
        mDescriptions = context.getResources().getStringArray(R.array.about_descriptions);
        mImages = context.getResources().getStringArray(R.array.about_images);
        mCovers = context.getResources().getStringArray(R.array.about_covers);
    }

    private final Context mContext;
    private final String[] mTitles;
    private final String[] mDescriptions;
    private final String[] mImages;
    private final String[] mCovers;

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public MainViewHolder(View itemView) {
            super(itemView);
            cover = ButterKnife.findById(itemView, R.id.cover);
            image = ButterKnife.findById(itemView, R.id.image);
            title = ButterKnife.findById(itemView, R.id.title);
            description = ButterKnife.findById(itemView, R.id.description);
            badges = ButterKnife.findById(itemView, R.id.badgesFrame);
        }

        final ImageView cover;
        final ImageView image;
        final TextView title;
        final TextView description;
        final LinearLayout badges;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 1) return 1;
        return 0;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                viewType == 1 ? R.layout.list_item_about_aidan : R.layout.list_item_about_tom, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int index) {
        holder.title.setText(mTitles[index]);
        holder.description.setText(Html.fromHtml(mDescriptions[index]));
        holder.description.setMovementMethod(LinkMovementMethod.getInstance());

        Glide.with(mContext)
                .load(mCovers[index])
                .into(holder.cover);
        Glide.with(mContext)
                .load(mImages[index])
                .into(holder.image);

        for (int i = 0; i < holder.badges.getChildCount(); i++)
            holder.badges.getChildAt(i).setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }
}