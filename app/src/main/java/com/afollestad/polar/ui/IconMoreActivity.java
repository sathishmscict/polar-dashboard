package com.afollestad.polar.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.afollestad.polar.R;
import com.afollestad.polar.adapters.IconMoreAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.transitions.CircularRevealTransition;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconMoreActivity extends BaseThemedActivity implements IconMoreAdapter.ClickListener {

    public static final String EXTRA_REVEAL_ANIM_LOCATION = "com.afollestad.polar.REVEAL_ANIM_LOCATION";

    public static final String EXTRA_CATEGORY = "com.afollestad.polar.CATEGORY";


    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(android.R.id.list)
    RecyclerView mRecyclerView;
    @Bind(R.id.circular_reveal_view)
    ViewGroup mCircularRevealView;

    private IconMoreAdapter mAdapter;

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons_more);
        ButterKnife.bind(this);

        final View root = findViewById(R.id.root);
        applyTopInset(root);
        applyBottomInset(mRecyclerView);

        final DrawableXmlParser.Category category = (DrawableXmlParser.Category) getIntent().getSerializableExtra(EXTRA_CATEGORY);

        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(category.getName());

        final int gridWidth = Config.get().gridWidthIcons();
        mAdapter = new IconMoreAdapter(this, gridWidth, this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridWidth));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setClipToPadding(false);
        mAdapter.set(category.getIcons());

        setUpTransitions();

        supportPostponeEnterTransition();
        Utils.waitForLayout(mRecyclerView, new Utils.LayoutCallback<RecyclerView>() {
            @Override
            public void onLayout(RecyclerView view) {
                supportStartPostponedEnterTransition();
            }
        });
    }

    private void setUpTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementsUseOverlay(true);

            final float[] buttonLocation = getIntent().getFloatArrayExtra(EXTRA_REVEAL_ANIM_LOCATION);
            final float x = buttonLocation[0];
            final float y = buttonLocation[1];

            CircularRevealTransition circularRevealTransition = new CircularRevealTransition(x, y);
            circularRevealTransition.addTarget(getString(R.string.transition_name_circular_reveal));
            circularRevealTransition.setInterpolator(new FastOutSlowInInterpolator());

            Slide enterSlide = new Slide();
            enterSlide.setDuration(300);
            enterSlide.setStartDelay(400);
            enterSlide.setInterpolator(new FastOutSlowInInterpolator());
            enterSlide.excludeTarget(getString(R.string.transition_name_circular_reveal), true);
            enterSlide.excludeTarget(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, true);

            Slide returnSide = new Slide();
            returnSide.setDuration(300);
            returnSide.setInterpolator(new FastOutSlowInInterpolator());
            returnSide.excludeTarget(getString(R.string.transition_name_circular_reveal), true);
            returnSide.excludeTarget(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, true);

            mCircularRevealView.setTransitionGroup(true);

            TransitionSet set = new TransitionSet()
                    .addTransition(circularRevealTransition)
                    .addTransition(enterSlide);

            TransitionSet set2 = new TransitionSet()
                    .addTransition(returnSide)
                    .addTransition(circularRevealTransition);

            getWindow().setEnterTransition(set);
            getWindow().setReturnTransition(set2);

            ChangeBounds enterBounds = new ChangeBounds();
            enterBounds.setDuration(300);
            enterBounds.setStartDelay(400);
            enterBounds.setInterpolator(new FastOutSlowInInterpolator());

            ChangeBounds returnBounds = new ChangeBounds();
            returnBounds.setDuration(300);
            returnBounds.setInterpolator(new FastOutSlowInInterpolator());

            getWindow().setSharedElementEnterTransition(enterBounds);
            getWindow().setSharedElementReturnTransition(returnBounds);
        }
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
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