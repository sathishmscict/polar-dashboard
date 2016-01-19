package com.afollestad.polar.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import com.afollestad.polar.fragments.AboutFragment;
import com.afollestad.polar.fragments.ApplyFragment;
import com.afollestad.polar.fragments.HomeFragment;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.fragments.RequestsFragment;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.fragments.ZooperFragment;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.LicensingUtils;
import com.afollestad.polar.util.PagesBuilder;
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

    private WindowInsets mDrawerLastInsets;
    private PagesBuilder mPages;

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean useNavDrawer = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("nav_drawer_mode", getResources().getBoolean(R.bool.nav_drawer_mode_default));
        if (useNavDrawer)
            setContentView(R.layout.activity_main_drawer);
        else
            setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        setupPages();
        setupPager();
        if (useNavDrawer)
            setupNavDrawer();
        else setupTabs();

        // Restore last selected page, tab/nav-drawer-item
        int lastPage = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("last_selected_page", 0);
        if (lastPage > mPager.getAdapter().getCount() - 1) lastPage = 0;
        mPager.setCurrentItem(lastPage);
        if (mNavView != null) invalidateNavViewSelection(lastPage);
    }

    private void setupPages() {
        final Resources r = getResources();
        mPages = new PagesBuilder(7);
        if (r.getBoolean(R.bool.homepage_enabled))
            mPages.add(new PagesBuilder.Page(R.id.drawer_home, R.drawable.tab_home, R.string.home, new HomeFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_icons, R.drawable.tab_icons, R.string.icons, new IconsFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_wallpapers, R.drawable.tab_wallpapers, R.string.wallpapers, new WallpapersFragment()));
        if (r.getBoolean(R.bool.icon_requests_enabled))
            mPages.add(new PagesBuilder.Page(R.id.drawer_requestIcons, R.drawable.tab_requests, R.string.request_icons, new RequestsFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_apply, R.drawable.tab_apply, R.string.apply, new ApplyFragment()));
        if (r.getBoolean(R.bool.zooper_enabled))
            mPages.add(new PagesBuilder.Page(R.id.drawer_zooper, R.drawable.tab_zooper, R.string.zooper, new ZooperFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_about, R.drawable.tab_about, R.string.about, new AboutFragment()));
    }

    public boolean retryLicenseCheck() {
        return LicensingUtils.check(this, this);
    }

    @Override
    public void onLicensingResult(boolean allow, int reason) {
        if (allow)
            showChangelogIfNecessary(true);
        else InvalidLicenseDialog.show(this, reason == Policy.RETRY);
    }

    @Override
    public void onLicensingError(int errorCode) {
        Utils.showError(this, new Exception("License checking error occurred, make sure everything is setup correctly. Error code: " + errorCode));
    }

    public void showChangelogIfNecessary(boolean licenseAllowed) {
        if (!getResources().getBoolean(R.bool.changelog_enabled)) {
            retryLicenseCheck();
        } else if (licenseAllowed || retryLicenseCheck()) {
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

        if (!getResources().getBoolean(R.bool.changelog_enabled))
            menu.findItem(R.id.changelog).setVisible(false);

        MenuItem darkTheme = menu.findItem(R.id.darkTheme);
        if (!getResources().getBoolean(R.bool.allow_theme_switching))
            darkTheme.setVisible(false);
        else darkTheme.setChecked(darkTheme());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        MenuItem navDrawerMode = menu.findItem(R.id.navDrawerMode);
        if (getResources().getBoolean(R.bool.allow_nav_drawer_mode_switch)) {
            navDrawerMode.setVisible(true);
            navDrawerMode.setChecked(prefs.getBoolean("nav_drawer_mode",
                    getResources().getBoolean(R.bool.nav_drawer_mode_default)));
        }

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
        } else if (item.getItemId() == R.id.navDrawerMode) {
            item.setChecked(!item.isChecked());
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("nav_drawer_mode", item.isChecked()).commit();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavDrawer() {
        assert mNavView != null;
        mNavView.getMenu().clear();
        for (PagesBuilder.Page page : mPages)
            page.addToMenu(mNavView.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);

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
                if (!getResources().getBoolean(R.bool.homepage_enabled))
                    position++;
                invalidateNavViewSelection(position);
            }
        });
    }

    private void invalidateNavViewSelection(int position) {
        assert mNavView != null;
        final int selectedId = mPages.get(position).drawerId;
        mNavView.post(new Runnable() {
            @Override
            public void run() {
                mNavView.setCheckedItem(selectedId);
            }
        });
    }

    public int getLastStatusBarInsetHeight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        }

        boolean useNavDrawer = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("nav_drawer_mode", getResources().getBoolean(R.bool.nav_drawer_mode_default));
        if (useNavDrawer) {
            return mDrawerLastInsets.getSystemWindowInsetTop();
        } else {
            return findViewById(R.id.root).getPaddingTop();
        }
    }

    private void setupPager() {
        mPager.setAdapter(new MainPagerAdapter(getFragmentManager(), mPages));
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
            }
        });

        for (PagesBuilder.Page page : mPages)
            addTab(page.iconRes);
        mTabs.setSelectedTabIndicatorColor(DialogUtils.resolveColor(this, R.attr.tab_indicator_color));
        applyTopInset(findViewById(R.id.root));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ((DrawerLayout) findViewById(R.id.drawer)).closeDrawers();
        final int index = mPages.findPositionForItem(item);
        if (index > -1)
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
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                .edit().putInt("last_selected_page", mPager.getCurrentItem()).commit();
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