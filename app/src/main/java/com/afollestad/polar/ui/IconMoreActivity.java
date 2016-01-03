package com.afollestad.polar.ui;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.afollestad.polar.adapters.IconMoreAdapter;
import com.afollestad.polar.R;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.ui.base.BelowStatusBarActivity;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconMoreActivity extends BelowStatusBarActivity implements IconMoreAdapter.ClickListener {

    private final static int REVEAL_ANIMATION_DURATION = 550;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.list)
    RecyclerView list;
    @Bind(R.id.root)
    View root;

    private IconMoreAdapter mAdapter;

    @Override
    @LayoutRes
    public int getLayout() {
        return R.layout.activity_icons_more;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        final DrawableXmlParser.Category category = (DrawableXmlParser.Category) getIntent().getSerializableExtra("category");

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(category.getName());

        mAdapter = new IconMoreAdapter(this);
        list.setLayoutManager(new GridLayoutManager(this,
                getResources().getInteger(R.integer.icon_grid_width)));
        list.setAdapter(mAdapter);
        list.setClipToPadding(false);
        list.setPadding(list.getPaddingLeft(),
                list.getPaddingTop(),
                list.getPaddingRight(),
                list.getPaddingBottom() + getResources().getDimensionPixelOffset(R.dimen.nav_bar_offset));
        mAdapter.set(category.getIcons());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.waitForLayout(root, new Utils.LayoutCallback<View>() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayout(View view) {
                    final int[] buttonLocation = getIntent().getIntArrayExtra("button_location");
                    final int cx = buttonLocation[0];
                    final int cy = buttonLocation[1];
                    int finalRadius = Math.max(root.getMeasuredWidth(), root.getMeasuredHeight());
                    Animator anim = ViewAnimationUtils.createCircularReveal(root, cx, cy, 0, finalRadius);
                    anim.setDuration(REVEAL_ANIMATION_DURATION);
                    anim.start();
                }
            });
        }
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View view, int index) {
        IconsFragment.selectItem(this, null, mAdapter.getIcon(index));
    }
}
