package com.afollestad.polar.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pk.requestmanager.AppInfo;
import com.afollestad.polar.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestVH> {

    public interface SelectionChangedListener {
        void onSelectionChanged();
    }

    private ArrayList<AppInfo> mApps;
    private final SelectionChangedListener mListener;

    public RequestsAdapter(SelectionChangedListener listener) {
        mListener = listener;
    }

    public void setApps(List<AppInfo> apps) {
        mApps = (ArrayList<AppInfo>) apps;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 1;
        return 0;
    }

    @Override
    public RequestVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 1 ? R.layout.list_item_header :
                        R.layout.list_item_iconrequest, parent, false);
        return new RequestVH(view);
    }

    @Override
    public void onBindViewHolder(RequestVH holder, int position) {
        if (position == 0) {
            holder.title.setText(R.string.tap_apps_to_select_them);
            return;
        }
        final AppInfo app = mApps.get(position - 1);
        holder.image.setImageDrawable(app.getImage());
        holder.title.setText(app.getName());
        if (holder.card != null)
            holder.card.setActivated(app.isSelected());
    }

    @Override
    public int getItemCount() {
        return mApps != null ? mApps.size() + 1 : 0;
    }

    public void selectAll() {
        updateSelection(true);
    }

    public void clearSelection() {
        updateSelection(false);
    }

    private void updateSelection(boolean select) {
        synchronized (mListener) {
            for (AppInfo app : mApps)
                app.setSelected(select);
            notifyDataSetChanged();
            mListener.onSelectionChanged();
        }
    }

    public class RequestVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        final CardView card;
        final TextView title;
        final ImageView image;

        public RequestVH(View itemView) {
            super(itemView);
            card = ButterKnife.findById(itemView, R.id.card);
            title = ButterKnife.findById(itemView, R.id.title);
            image = ButterKnife.findById(itemView, R.id.image);
            if (card != null)
                card.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AppInfo info = mApps.get(getAdapterPosition() - 1);
            info.setSelected(!info.isSelected());
            notifyItemChanged(getAdapterPosition());
            if (mListener != null)
                mListener.onSelectionChanged();
        }
    }
}
