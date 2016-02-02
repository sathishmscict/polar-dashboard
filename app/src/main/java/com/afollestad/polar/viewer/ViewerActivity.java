package com.afollestad.polar.viewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.util.WallpaperUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.afollestad.polar.fragments.WallpapersFragment.RQ_CROPANDSETWALLPAPER;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressLint("MissingSuperCall")
public class ViewerActivity extends AppCompatActivity {

    private WallpaperUtils.WallpapersHolder mWallpapers;
    @SuppressWarnings("FieldCanBeLocal")
    private ViewerPageAdapter mAdapter;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    public static final String STATE_CURRENT_POSITION = "state_current_position";
    private int mCurrentPosition;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
    }

    public int getNavigationBarHeight(boolean portraitOnly, boolean landscapeOnly) {
        final Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // Cancel out for tablets~
            return 0;
        }

        final Resources r = getResources();
        int id;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (portraitOnly) return 0;
            id = r.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        } else {
            if (landscapeOnly) return 0;
            id = r.getIdentifier("navigation_bar_height", "dimen", "android");
        }
        if (id > 0)
            return r.getDimensionPixelSize(id);
        return 0;
    }

    protected void applyTopInset(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            private int mInitialTop = -1;

            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                if (mInitialTop == -1)
                    mInitialTop = layoutParams.topMargin;
                layoutParams.topMargin = mInitialTop + insets.getSystemWindowInsetTop();
                v.setLayoutParams(layoutParams);
                return insets;
            }
        });
        ViewCompat.requestApplyInsets(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        applyTopInset(mToolbar);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mCurrentPosition = getIntent().getExtras().getInt(STATE_CURRENT_POSITION);
            }
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
        }

        if (getIntent() != null) {
            mWallpapers = (WallpaperUtils.WallpapersHolder) getIntent().getSerializableExtra("wallpapers");
        }

        mAdapter = new ViewerPageAdapter(this, mCurrentPosition, mWallpapers);
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mCurrentPosition);

        // When the view pager is swiped, fragments are notified if they're active or not
        // And the menu updates based on the color mode (light or dark).
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int previousState;
            boolean userScrollChange;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ViewerPageFragment noActive = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                if (noActive != null)
                    noActive.setIsActive(false);
                mCurrentPosition = position;
                ViewerPageFragment active = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
                if (active != null) {
                    active.setIsActive(true);
                }
                mAdapter.mCurrentPage = position;
                setResult(RESULT_OK, getIntent().putExtra(STATE_CURRENT_POSITION, position));
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING)
                    userScrollChange = true;
                else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE)
                    userScrollChange = false;

                previousState = state;
            }
        });

        // Prevents nav bar from overlapping toolbar options in landscape
        mToolbar.setPadding(
                mToolbar.getPaddingLeft(),
                mToolbar.getPaddingTop(),
                getNavigationBarHeight(false, true),
                mToolbar.getPaddingBottom()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_CROPANDSETWALLPAPER) {
            WallpapersFragment.showToast(this, R.string.wallpaper_set);
            WallpapersFragment.resetOptionCache(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ViewerPageFragment active = (ViewerPageFragment) getFragmentManager().findFragmentByTag("page:" + mCurrentPosition);
        if (active != null) {
            active.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mToolbar.animate().cancel();
    }
}