package com.afollestad.polar.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.util.TintUtils;
import com.bumptech.glide.Glide;

import butterknife.ButterKnife;


public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.MainViewHolder> implements View.OnClickListener {

    public interface OptionsClickListener {

        void onOptionFeedback();

        void onOptionDonate();
    }

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

    public AboutAdapter(Activity context, OptionsClickListener cb) {
        mContext = context;
        mTitles = context.getResources().getStringArray(R.array.about_titles);
        mDescriptions = context.getResources().getStringArray(R.array.about_descriptions);
        mImages = context.getResources().getStringArray(R.array.about_images);
        mCovers = context.getResources().getStringArray(R.array.about_covers);
        mDevButtonTitles = context.getResources().getStringArray(R.array.about_button_1_titles);
        mDevButtonLinks = context.getResources().getStringArray(R.array.about_button_1_links);
        mDevButtonTitles2 = context.getResources().getStringArray(R.array.about_button_2_titles);
        mDevButtonLinks2 = context.getResources().getStringArray(R.array.about_button_2_links);
        mOptionCb = cb;
        mOptionsEnabled = Config.get().feedbackEnabled() || Config.get().donationEnabled();
        mItemAidan = mTitles.length -3;
        mItemTom = mTitles.length -2;
        mItemDaniel = mTitles.length-1;
    }

    private final Context mContext;
    private final String[] mTitles;
    private final String[] mDescriptions;
    private final String[] mImages;
    private final String[] mCovers;
    private final String[] mDevButtonTitles;
    private final String[] mDevButtonLinks;
    private final String[] mDevButtonTitles2;
    private final String[] mDevButtonLinks2;
    private final OptionsClickListener mOptionCb;
    private final boolean mOptionsEnabled;
    // saving values in case there's more than one dev
    private int mItemAidan;
    private final int mItemTom;
    private final int mItemDaniel;

    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MainViewHolder(View itemView, OptionsClickListener optionsCb) {
            super(itemView);
            cover = ButterKnife.findById(itemView, R.id.cover);
            image = ButterKnife.findById(itemView, R.id.image);
            title = ButterKnife.findById(itemView, R.id.title);
            description = ButterKnife.findById(itemView, R.id.description);
            badges = ButterKnife.findById(itemView, R.id.badgesFrame);
            leftButton = ButterKnife.findById(itemView, R.id.badgeButtonLeft);
            rightButton = ButterKnife.findById(itemView, R.id.badgeButtonRight);

            feedbackButton = ButterKnife.findById(itemView, R.id.feedbackButton);
            feedbackImage = ButterKnife.findById(itemView, R.id.feedbackImage);
            donateButton = ButterKnife.findById(itemView, R.id.donateButton);
            donateImage = ButterKnife.findById(itemView, R.id.donateImage);
            mOptionsCb = optionsCb;
            if (feedbackButton != null)
                feedbackButton.setOnClickListener(this);
            if (donateButton != null)
                donateButton.setOnClickListener(this);
        }

        final ImageView cover;
        final ImageView image;
        final TextView title;
        final TextView description;
        final LinearLayout badges;

        final View feedbackButton;
        final ImageView feedbackImage;
        final View donateButton;
        final ImageView donateImage;
        final Button leftButton;
        final Button rightButton;
        private final OptionsClickListener mOptionsCb;

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.feedbackButton) {
                mOptionsCb.onOptionFeedback();
            } else {
                mOptionsCb.onOptionDonate();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mTitles.length == 3) {
            // Gaufrer config
            if (position == 0)
                return -1;
            else if (position == 1)
                return 2;
            else
                return 1;
        } else {
            if (mOptionsEnabled) {
                if (position == 0) return -1;
                position--;
            }
            return position;
        }
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @LayoutRes
        int layoutRes = getLayoutResourceForViewType(viewType);
        final View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MainViewHolder(v, mOptionCb);
    }

    @LayoutRes
    private int getLayoutResourceForViewType(int viewType) {
        if(viewType == -1) return R.layout.list_item_about_options;
        else if(viewType == mItemAidan) return R.layout.list_item_about_aidan;
        else if(viewType == mItemTom) return R.layout.list_item_about_tom;
        else if(viewType == mItemDaniel) return R.layout.list_item_about_daniel;
        else return R.layout.list_item_about_dev;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int index) {
        if (holder.title == null) {
            // Options
            final boolean feedbackEnabled = Config.get().feedbackEnabled();
            final boolean donationEnabled = Config.get().donationEnabled();
            final int accentColor = DialogUtils.resolveColor(mContext, R.attr.colorAccent);

            if (feedbackEnabled) {
                holder.feedbackImage.setImageDrawable(TintUtils.createTintedDrawable(
                        ContextCompat.getDrawable(mContext, R.drawable.ic_action_feedback), accentColor));
            } else {
                ((LinearLayout.LayoutParams) holder.donateButton.getLayoutParams()).weight = 2;
                holder.feedbackButton.setVisibility(View.GONE);
            }
            if (donationEnabled) {
                holder.donateImage.setImageDrawable(TintUtils.createTintedDrawable(
                        ContextCompat.getDrawable(mContext, R.drawable.ic_action_donate), accentColor));
            } else {
                ((LinearLayout.LayoutParams) holder.feedbackButton.getLayoutParams()).weight = 2;
                holder.donateButton.setVisibility(View.GONE);
            }
            return;
        }
        if (mOptionsEnabled)
            index--;

        holder.title.setText(mTitles[index]);
        holder.description.setText(Html.fromHtml(mDescriptions[index]));
        holder.description.setMovementMethod(LinkMovementMethod.getInstance());

        if(index != mItemAidan && index != mItemTom && index != mItemDaniel) {
            holder.leftButton.setText(mDevButtonTitles[index]);
            holder.leftButton.setTag(mDevButtonLinks[index]);
            holder.rightButton.setText(mDevButtonTitles2[index]);
            holder.rightButton.setTag(mDevButtonLinks2[index]);
        }

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
        int count = mTitles.length;
        if (mOptionsEnabled) count++;
        return count;
    }
}