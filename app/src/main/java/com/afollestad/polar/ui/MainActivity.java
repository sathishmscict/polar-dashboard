package com.afollestad.polar.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;

import com.afollestad.bridge.Bridge;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.MainPagerAdapter;
import com.afollestad.polar.dialogs.ChangelogDialog;
import com.afollestad.polar.dialogs.InvalidLicenseDialog;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.LicensingUtils;
import com.afollestad.polar.util.Utils;
import com.google.android.vending.licensing.Policy;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.afollestad.polar.fragments.WallpapersFragment.RQ_CROPANDSETWALLPAPER;
import static com.afollestad.polar.fragments.WallpapersFragment.RQ_VIEWWALLPAPER;
import static com.afollestad.polar.viewer.ViewerActivity.STATE_CURRENT_POSITION;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends BaseThemedActivity implements LicensingUtils.LicensingCallback, NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable
    @Bind(R.id.tabs)
    TabLayout mTabs;
    @Nullable
    @Bind(R.id.navigation_view)
    NavigationView mNavView;
    @Bind(R.id.pager)
    ViewPager mPager;

    public RecyclerView mRecyclerView;

    WindowInsets mDrawerLastInsets;

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean useNavDrawer = getResources().getBoolean(R.bool.use_navigation_drawer);
        if (useNavDrawer)
            setContentView(R.layout.activity_main_drawer);
        else
            setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        setupPager();
        if (useNavDrawer)
            setupNavDrawer();
        else
            setupTabs();

        // Restore last selected page, tab/nav-drawer-item
        final int lastPage = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("last_selected_page", 0);
        mPager.setCurrentItem(lastPage);
    }

    public boolean retryLicenseCheck() {
        return LicensingUtils.check(this, this);
    }

    @Override
    public void onLicensingResult(boolean allow, int reason) {
        if (allow)
            showChangelogIfNecessary();
        else InvalidLicenseDialog.show(this, reason == Policy.RETRY);
    }

    @Override
    public void onLicensingError(int errorCode) {
        Utils.showError(this, new Exception("License checking error occurred, make sure everything is setup correctly. Error code: " + errorCode));
    }

    public void showChangelogIfNecessary() {
        if (!getResources().getBoolean(R.bool.allow_changelog)) {
            retryLicenseCheck();
        } else if (retryLicenseCheck()) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final int currentVersion = BuildConfig.VERSION_CODE;
            if (currentVersion != prefs.getInt("changelog_version", -1)) {
                prefs.edit().putInt("changelog_version", currentVersion).apply();
                ChangelogDialog.show(this);
            }
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

    private void setupNavDrawer() {
        assert mNavView != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {

                    //TODO: Check if NavigationView needs bottom padding

                    WindowInsets drawerLayoutInsets = insets.replaceSystemWindowInsets(
                            insets.getSystemWindowInsetLeft(),
                            insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(),
                            0
                    );

                    mDrawerLastInsets = drawerLayoutInsets;

                    ((DrawerLayout) v).setChildInsets(drawerLayoutInsets,
                            drawerLayoutInsets.getSystemWindowInsetTop() > 0);
                    return insets;
                }
            });
        }

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable menuIcon = ContextCompat.getDrawable(this, R.drawable.ic_action_menu);
        menuIcon = Utils.tintDrawable(menuIcon, DialogUtils.resolveColor(this, R.attr.tab_icon_color));
        getSupportActionBar().setHomeAsUpIndicator(menuIcon);

        drawer.setDrawerListener(new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.drawer_open, R.string.drawer_close));
        drawer.setStatusBarBackgroundColor(DialogUtils.resolveColor(this, R.attr.colorPrimaryDark));
        mNavView.setNavigationItemSelectedListener(this);

        final ColorDrawable navBg = (ColorDrawable) mNavView.getBackground();
        final int selectedIconText = DialogUtils.resolveColor(this, R.attr.colorAccent);
        int iconColor;
        int titleColor;
        int selectedBg;
        if (Utils.isColorLight(navBg.getColor())) {
            iconColor = ContextCompat.getColor(this, R.color.navigationview_normalicon_light);
            titleColor = ContextCompat.getColor(this, R.color.navigationview_normaltext_light);
            selectedBg = ContextCompat.getColor(this, R.color.navigationview_selectedbg_light);
        } else {
            iconColor = ContextCompat.getColor(this, R.color.navigationview_normalicon_dark);
            titleColor = ContextCompat.getColor(this, R.color.navigationview_normaltext_dark);
            selectedBg = ContextCompat.getColor(this, R.color.navigationview_selectedbg_dark);
        }

        final ColorStateList iconSl = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{iconColor, selectedIconText});
        final ColorStateList textSl = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{titleColor, selectedIconText});
        mNavView.setItemTextColor(textSl);
        mNavView.setItemIconTintList(iconSl);

        StateListDrawable bgDrawable = new StateListDrawable();
        bgDrawable.addState(new int[]{android.R.attr.state_checked}, new ColorDrawable(selectedBg));
        mNavView.setItemBackground(bgDrawable);

        mNavView.getHeaderView(0).setBackgroundColor(DialogUtils.resolveColor(this, R.attr.colorAccent));
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final BasePageFragment frag = (BasePageFragment) getFragmentManager().findFragmentByTag("page:" + position);
                if (frag != null) frag.updateTitle();
                switch (position) {
                    case 0:
                        mNavView.setCheckedItem(R.id.drawer_icons);
                        break;
                    case 1:
                        mNavView.setCheckedItem(R.id.drawer_wallpapers);
                        break;
                    case 2:
                        mNavView.setCheckedItem(R.id.drawer_requestIcons);
                        break;
                    case 3:
                        mNavView.setCheckedItem(R.id.drawer_apply);
                        break;
                    case 4:
                        mNavView.setCheckedItem(R.id.drawer_about);
                        break;
                }
            }
        });
    }

    public int getLastStatusBarInsetHeight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        }

        boolean useNavDrawer = getResources().getBoolean(R.bool.use_navigation_drawer);
        if (useNavDrawer) {
            return mDrawerLastInsets.getSystemWindowInsetTop();
        } else {
            return findViewById(R.id.root).getPaddingTop();
        }
    }

    private void setupPager() {
        mPager.setAdapter(new MainPagerAdapter(getFragmentManager()));
        mPager.setOffscreenPageLimit(6);
        mPager.setOffscreenPageLimit(6);
    }

    private void setupTabs() {
        assert mTabs != null;
        mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                final BasePageFragment frag = (BasePageFragment) getFragmentManager().findFragmentByTag("page:" + position);
                if (frag != null) frag.updateTitle();
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .edit().putInt("last_selected_page", position).commit();
            }
        });

        addTab(R.drawable.tab_icons);
        addTab(R.drawable.tab_wallpapers);
        addTab(R.drawable.tab_requests);
        addTab(R.drawable.tab_apply);
        addTab(R.drawable.tab_about);

        mTabs.setSelectedTabIndicatorColor(DialogUtils.resolveColor(this, R.attr.tab_indicator_color));

        applyTopInset(findViewById(R.id.root));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ((DrawerLayout) findViewById(R.id.root)).closeDrawers();
        int index;
        switch (item.getItemId()) {
            default:
            case R.id.drawer_icons:
                index = 0;
                break;
            case R.id.drawer_wallpapers:
                index = 1;
                break;
            case R.id.drawer_requestIcons:
                index = 2;
                break;
            case R.id.drawer_apply:
                index = 3;
                break;
            case R.id.drawer_about:
                index = 4;
                break;
        }
        mPager.setCurrentItem(index);
        return false;
    }

    private void addTab(@DrawableRes int icon) {
        assert mTabs != null;
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
        if (isFinishing()) {
            Bridge.destroy();
            Inquiry.deinit();
            DrawableXmlParser.cleanup();
            LicensingUtils.cleanup();
        }
    }

    @Override
    public void onBackPressed() {
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