package com.afollestad.polar.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.bridge.Bridge;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.MainPagerAdapter;
import com.afollestad.polar.dialogs.ChangelogDialog;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.ui.base.BelowStatusBarActivity;
import com.afollestad.polar.util.DrawableXmlParser;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.afollestad.polar.fragments.WallpapersFragment.RQ_CROPANDSETWALLPAPER;
import static com.afollestad.polar.fragments.WallpapersFragment.RQ_VIEWWALLPAPER;
import static com.afollestad.polar.viewer.ViewerActivity.STATE_CURRENT_POSITION;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends BelowStatusBarActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tabs)
    TabLayout mTabs;
    @Bind(R.id.pager)
    ViewPager mPager;

    public RecyclerView mRecyclerView;

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setupPager();
    }

    @SuppressLint("CommitPrefEdits")
    public void showChangelogIfNecessary() {
        if (!getResources().getBoolean(R.bool.allow_changelog)) return;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final int currentVersion = BuildConfig.VERSION_CODE;
        if (currentVersion != prefs.getInt("changelog_version", -1)) {
            prefs.edit().putInt("changelog_version", currentVersion).commit();
            ChangelogDialog.show(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        if (!getResources().getBoolean(R.bool.allow_changelog))
            menu.findItem(R.id.changelog).setVisible(false);

        MenuItem darkTheme = menu.findItem(R.id.darkTheme);
        if (!getResources().getBoolean(R.bool.allow_theme_switching))
            darkTheme.setVisible(false);
        else darkTheme.setChecked(darkTheme());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.changelog) {
            ChangelogDialog.show(this);
            return true;
        } else if (item.getItemId() == R.id.darkTheme) {
            darkTheme(!darkTheme());
            mToolbar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 500);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupPager() {
        mPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        mPager.setOffscreenPageLimit(6);
        mPager.setOffscreenPageLimit(6);
        mTabs.setSelectedTabIndicatorColor(DialogUtils.resolveColor(this, R.attr.tab_indicator_color));

        mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .edit().putInt("last_selected_page", position).commit();
            }
        });

        addTab(R.drawable.tab_icons);
        addTab(R.drawable.tab_wallpapers);
        addTab(R.drawable.tab_requests);
        addTab(R.drawable.tab_apply);
        addTab(R.drawable.tab_about);

        final int lastPage = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("last_selected_page", 0);
        mPager.setCurrentItem(lastPage);
    }

    private void addTab(@DrawableRes int icon) {
        TabLayout.Tab tab = mTabs.newTab().setIcon(icon);
        if (tab.getIcon() != null)
            tab.getIcon().setColorFilter(DialogUtils.resolveColor(this, R.attr.tab_icon_color), PorterDuff.Mode.SRC_ATOP);
        mTabs.addTab(tab);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void setTitle(CharSequence title) {
        mToolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        mToolbar.setTitle(titleId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bridge.destroy();
        Inquiry.deinit();
        DrawableXmlParser.cleanup();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_CROPANDSETWALLPAPER) {
            WallpapersFragment.showToast(this, R.string.wallpaper_set);
            WallpapersFragment.resetOptionCache(true);
        } else if (requestCode == RQ_VIEWWALLPAPER) {
            if (data != null && mRecyclerView != null) {
                mRecyclerView.requestFocus();
                final int currentPos = data.getIntExtra(STATE_CURRENT_POSITION, 0);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(currentPos);
                    }
                });
            }
        }
    }
}