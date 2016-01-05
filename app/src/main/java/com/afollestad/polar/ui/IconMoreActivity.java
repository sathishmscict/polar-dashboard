package com.afollestad.polar.ui;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionSet;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.afollestad.polar.R;
import com.afollestad.polar.adapters.IconMoreAdapter;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.util.DrawableXmlParser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconMoreActivity extends BaseThemedActivity implements IconMoreAdapter.ClickListener {

    public static final String EXTRA_REVEAL_ANIM_LOCATION = "com.afollestad.polar.REVEAL_ANIM_LOCATION";
    public static final String EXTRA_ = "com.afollestad.polar.BUTTON_LOCATION";

    public static final String EXTRA_CATEGORY = "com.afollestad.polar.CATEGORY";

    final static int REVEAL_ANIMATION_DURATION = 550;


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.list)
    RecyclerView list;

    private IconMoreAdapter mAdapter;

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons_more);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final float[] buttonLocation = getIntent().getFloatArrayExtra(EXTRA_REVEAL_ANIM_LOCATION);
            final float x = buttonLocation[0];
            final float y = buttonLocation[1];

            TransitionSet set = new TransitionSet();
            CircularRevealTransition circularRevealTransition = new CircularRevealTransition(x, y);
            circularRevealTransition.addTarget("circularReveal");
            //set.addTransition(circularRevealTransition);


            getWindow().setEnterTransition(circularRevealTransition);
            getWindow().setExitTransition(circularRevealTransition);
        }


        final DrawableXmlParser.Category category = (DrawableXmlParser.Category) getIntent().getSerializableExtra(EXTRA_CATEGORY);

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class CircularRevealTransition extends Visibility {

        private float startX;
        private float startY;

        public CircularRevealTransition(float x, float y) {
            super();
            startX = x;
            startY = y;
        }

        @Override
        public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
            return createAnimator(view, true);
        }

        @Override
        public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
            return createAnimator(view, false);
        }

        public Animator createAnimator(View view, boolean appear) {
            float dx = Math.max(view.getMeasuredWidth() - startX, startX);
            float dy = Math.max(view.getMeasuredHeight() - startY, startY);

            float radius = (float) Math.hypot(dx, dy);

            Animator anim = ViewAnimationUtils.createCircularReveal(view, (int) startX, (int) startY, appear ? 0 : radius, appear ? radius : 0);
            anim.setDuration(REVEAL_ANIMATION_DURATION);
            anim.setInterpolator(new FastOutSlowInInterpolator());

            return anim;
        }
    }
}
