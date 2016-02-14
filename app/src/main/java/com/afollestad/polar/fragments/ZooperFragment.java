package com.afollestad.polar.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.assent.AssentCallback;
import com.afollestad.assent.PermissionResultSet;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.ZooperAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.ui.MainActivity;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.Utils;
import com.afollestad.polar.zooper.ZooperUtil;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ZooperFragment extends BasePageFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    @Bind(android.R.id.list)
    RecyclerView mRecyclerView;
    @Bind(android.R.id.empty)
    TextView mEmpty;
    @Bind(android.R.id.progress)
    View mProgress;
    @Bind(R.id.fabInstall)
    FloatingActionButton mFabInstall;

    private ZooperAdapter mAdapter;
    private String mQueryText;
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.filter(mQueryText);
            setListShown(true);
        }
    };
    private ArrayList<PreviewItem> mPreviews;
    private Drawable mWallpaper;

    public ZooperFragment() {
    }

    @Override
    public int getTitle() {
        return R.string.zooper;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zooper, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.zooper, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mSearchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setQueryHint(getString(R.string.search_widgets));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        if (getActivity() != null) {
            final MainActivity act = (MainActivity) getActivity();
            TintUtils.themeSearchView(act.getToolbar(), mSearchView, DialogUtils.resolveColor(act, R.attr.tab_icon_color));
        }
    }

    private void setListShown(boolean shown) {
        final View v = getView();
        if (v != null) {
            mRecyclerView.setVisibility(shown ?
                    View.VISIBLE : View.GONE);
            mProgress.setVisibility(shown ?
                    View.GONE : View.VISIBLE);
            mEmpty.setVisibility(shown && mAdapter.getItemCount() == 0 ?
                    View.VISIBLE : View.GONE);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mAdapter = new ZooperAdapter(getActivity());
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                Config.get().gridWidthZooper(), StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        ZooperUtil.getPreviews(getActivity(), new ZooperUtil.PreviewCallback() {
            @Override
            public void onPreviewsLoaded(ArrayList<PreviewItem> previews, Drawable wallpaper, Exception error) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded())
                    return;
                else if (error != null) {
                    error.printStackTrace();
                    setListShown(true);
                    mAdapter.setPreviewFiles(null, null);
                    mEmpty.setVisibility(View.VISIBLE);
                    if (error.getMessage().trim().isEmpty())
                        mEmpty.setText(error.toString());
                    else mEmpty.setText(error.getMessage());
                    return;
                }
                mPreviews = previews;
                mWallpaper = wallpaper;
                mAdapter.setPreviewFiles(mPreviews, mWallpaper);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().isFinishing()) {
            if (mAdapter != null)
                mAdapter.recycle();
            Utils.wipe(ZooperUtil.getWidgetPreviewCache(getActivity()));
            mWallpaper = null;
            mPreviews = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fabInstall)
    public void onInstall() {
        mFabInstall.hide();
        if (!Assent.isPermissionGranted(Assent.WRITE_EXTERNAL_STORAGE)) {
            Assent.requestPermissions(new AssentCallback() {
                @Override
                public void onPermissionResult(PermissionResultSet permissionResultSet) {
                    checkInstalled();
                }
            }, 69, Assent.WRITE_EXTERNAL_STORAGE);
        } else {
            checkInstalled();
        }
    }

    private void checkInstalled() {
        ZooperUtil.checkInstalled(getActivity(), new ZooperUtil.CheckResult() {
            @Override
            public void onCheckResult(boolean fontsInstalled, boolean iconsetsInstalled, boolean bitmapsInstalled) {
                performInstallZooper(fontsInstalled, iconsetsInstalled, bitmapsInstalled);
            }
        });
    }

    private void performInstallZooper(boolean fontsInstalled, boolean iconsetsInstalled, boolean bitmapsInstalled) {
        ZooperUtil.install(getActivity(), !fontsInstalled, !iconsetsInstalled, !bitmapsInstalled,
                new ZooperUtil.InstallResult() {
                    @Override
                    public void onInstallResult(Exception e) {
                        mFabInstall.show();
                        if (e != null) {
                            Utils.showError(getActivity(), e);
                        } else {
                            Toast.makeText(getActivity(), R.string.assets_installed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Search

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mQueryText = newText;
        mRecyclerView.postDelayed(searchRunnable, 400);
        return false;
    }

    @Override
    public boolean onClose() {
        mRecyclerView.removeCallbacks(searchRunnable);
        mQueryText = null;
        mAdapter.filter(null);
        setListShown(true);
        return false;
    }

    public static class PreviewItem implements Serializable {

        public final String name;
        public final Bitmap image;

        public PreviewItem(String name, Bitmap image) {
            this.name = name;
            this.image = image;
        }
    }
}