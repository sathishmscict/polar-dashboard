package com.afollestad.polar.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.assent.AssentCallback;
import com.afollestad.assent.PermissionResultSet;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.RequestsAdapter;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.ui.MainActivity;
import com.afollestad.polar.util.Utils;
import com.pk.requestmanager.AppInfo;
import com.pk.requestmanager.AppLoadListener;
import com.pk.requestmanager.PkRequestManager;
import com.pk.requestmanager.RequestSettings;
import com.pk.requestmanager.SendRequestListener;

import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RequestsFragment extends BasePageFragment implements
        AppLoadListener, SendRequestListener, RequestsAdapter.SelectionChangedListener, AssentCallback, DragSelectRecyclerViewAdapter.SelectionListener {

    private static final Object LOCK = new Object();

    private final static int PERM_RQ = 69;
    private final static int FAB_ANIMATION_DURATION = 250;

    private RequestsAdapter mAdapter;
    private PkRequestManager mRequestManager;
    private MaterialDialog mDialog;
    private boolean mRequestedPermission;
    private int mFabOffset = -1;
    private boolean mFabShown = false;
    private int mLastNumSelected = -1;
    private boolean mAppsLoaded = false;

    @Bind(android.R.id.list)
    DragSelectRecyclerView list;
    @Bind(android.R.id.progress)
    View progress;
    @Bind(R.id.progressText)
    TextView progressText;
    @Bind(android.R.id.empty)
    TextView emptyText;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    public RequestsFragment() {
    }

    @Override
    public int getTitle() {
        return R.string.request_icons;
    }

    private void toggleFab(boolean show) {
        if (mFabOffset == -1) {
            final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            mFabOffset = lp.bottomMargin * 2;
        }
        if (show) {
            if (mFabShown) return;
            mFabShown = true;
            fab.animate().cancel();
            fab.setVisibility(View.VISIBLE);
            fab.setTranslationY(mFabOffset);
            ViewPropertyAnimator animator = fab.animate().translationY(0);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(FAB_ANIMATION_DURATION);
            animator.setListener(null);
            animator.start();
        } else {
            if (!mFabShown) return;
            mFabShown = false;
            fab.animate().cancel();
            fab.setTranslationY(0);
            ViewPropertyAnimator animator = fab.animate().translationY(mFabOffset);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(FAB_ANIMATION_DURATION);
            animator.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    fab.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }

    @Override
    public void updateTitle() {
        synchronized (LOCK) {
            MainActivity act = (MainActivity) getActivity();
            if (act != null) {
                if (fab == null) {
                    act.setTitle(R.string.request_icons);
                    return;
                }

                final int numSelected = mAdapter != null ? mAdapter.getSelectedCount() : 0;
                if (mLastNumSelected == numSelected) return;
                mLastNumSelected = numSelected;

                if (numSelected == 0)
                    act.setTitle(R.string.request_icons);
                else act.setTitle(getString(R.string.request_icons_x, numSelected));

                toggleFab(numSelected > 0);
                // Work around for the icon sometimes being invisible?
                fab.setImageResource(R.drawable.ic_action_apply);
                // Update toolbar items
                act.invalidateOptionsMenu();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_requesticons, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        synchronized (LOCK) {
            inflater.inflate(R.menu.cab_requests, menu);
            MenuItem selectAll = menu.findItem(R.id.selectAll);
            try {
                if (mRequestManager == null || mRequestManager.getNumSelected() == 0)
                    selectAll.setIcon(R.drawable.ic_action_selectall);
                else selectAll.setIcon(R.drawable.ic_action_close);
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
                selectAll.setVisible(false);
                // TODO solve this officially, use different request loading library?
            }
            selectAll.setVisible(mAppsLoaded);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        synchronized (LOCK) {
            if (item.getItemId() == R.id.selectAll) {
                if (mRequestManager.getNumSelected() == 0)
                    mAdapter.selectAll();
                else mAdapter.clearSelection();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        GridLayoutManager lm = new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.requests_grid_width));
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0)
                    return getResources().getInteger(R.integer.requests_grid_width);
                return 1;
            }
        });

        mAdapter = new RequestsAdapter(this);
        mAdapter.setSelectionListener(this);

        list.setLayoutManager(lm);
        list.setAdapter(mAdapter);

        emptyText.setText(R.string.no_apps);
        emptyText.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText(R.string.preparing_to_load);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Assent.isPermissionGranted(Assent.WRITE_EXTERNAL_STORAGE)) {
            if (!mRequestedPermission) {
                Assent.requestPermissions(this, PERM_RQ, Assent.WRITE_EXTERNAL_STORAGE);
                mRequestedPermission = true;
            }
        } else {
            reload();
            if (getActivity() != null)
                ((MainActivity) getActivity()).showChangelogIfNecessary();
        }
    }

    @Override
    public void onPermissionResult(PermissionResultSet permissionResultSet) {
        if (!permissionResultSet.isGranted(Assent.WRITE_EXTERNAL_STORAGE)) {
            progress.setVisibility(View.GONE);
            toggleFab(false);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.write_storage_permission_denied);
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void reload() {
        synchronized (LOCK) {
            if (mRequestManager == null) {
                mRequestManager = PkRequestManager.getInstance(getActivity());
                final File saveFolder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                //noinspection ResultOfMethodCallIgnored
                saveFolder.mkdirs();
                mRequestManager.setSettings(new RequestSettings.Builder()
                        .addEmailAddress(getString(R.string.icon_request_email))
                        .emailSubject(String.format("%s %s", getString(R.string.app_name), getString(R.string.icon_request)))
                        .emailPrecontent("These apps are missing on my phone:\n\n") // Text before the main app information
                        .saveLocation(saveFolder.getAbsolutePath())
                        .appfilterName("appfilter.xml")
                        .compressFormat(PkRequestManager.PNG)
                        .appendInformation(true)
                        .createAppfilter(true)
                        .createZip(true)
                        .filterAutomatic(true)
                        .filterDefined(true)
                        .byteBuffer(2048)
                        .compressQuality(100)
                        .build());
            } else {
                mRequestManager.removeAllListeners();
                mRequestManager.setActivity(getActivity());
            }

            mRequestManager.addAppLoadListener(this);
            mRequestManager.addSendRequestListener(this);
            mRequestManager.setDebugging(BuildConfig.DEBUG);

            if (mRequestManager.getApps() != null && mRequestManager.getApps().size() > 0) {
                mAdapter.setApps(mRequestManager.getApps());
                mAdapter.setApps(mRequestManager.getApps());
                emptyText.setVisibility(mAdapter.getItemCount() == 0 ?
                        View.VISIBLE : View.GONE);
                progress.setVisibility(View.GONE);
            }
            mRequestManager.loadAppsIfEmptyAsync();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        synchronized (LOCK) {
            if (mRequestManager != null)
                mRequestManager.removeAllListeners();
            mRequestManager = null;
            if (mDialog != null)
                mDialog.dismiss();
        }
    }

    // Load apps listener

    @Override
    public void onAppPreload() {
        if (progressText == null) return;
        mAppsLoaded = false;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                emptyText.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                progressText.setText(R.string.preparing_to_load);
            }
        });
    }

    @Override
    public void onAppLoading(int status, final int progress) {
        if (progressText == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || getActivity() == null) return;
                progressText.setText(getString(R.string.loading_progress_x, progress));
            }
        });
    }

    @Override
    public void onAppLoaded() {
        synchronized (LOCK) {
            if (progressText == null) return;
            mAppsLoaded = true;
            progressText.post(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) getActivity().invalidateOptionsMenu();
                    mAdapter.setApps(mRequestManager.getApps());
                    emptyText.setVisibility(mAdapter.getItemCount() == 0 ?
                            View.VISIBLE : View.GONE);
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

    // Send request listener

    @OnClick(R.id.fab)
    public void onClickFab() {
        if (getString(R.string.icon_request_email).trim().isEmpty()) {
            Utils.showError(getActivity(), new Exception("The developer has not set an email for icon requests yet."));
            return;
        }
        synchronized (LOCK) {
            if (getActivity() == null) return;
            final List<AppInfo> apps = mRequestManager.getApps();
            for (int i = 0; i < apps.size(); i++)
                apps.get(i).setSelected(mAdapter.isIndexSelected(i));
            mRequestManager.setActivity(getActivity());
            mRequestManager.sendRequestAsync();
        }
    }

    @Override
    public void onRequestStart(boolean automatic) {
        if (getActivity() == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                mDialog = new MaterialDialog.Builder(getActivity())
                        .content(R.string.preparing_icon_request)
                        .progress(true, -1)
                        .cancelable(false)
                        .show();
            }
        });
    }

    @Override
    public void onRequestBuild(boolean automatic, final int progress) {
        if (getActivity() == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                mDialog.setContent(R.string.building_icon_request, progress);
            }
        });
    }

    @Override
    public void onRequestFinished(boolean automatic, final boolean intentSuccessful, Intent intent) {
        if (getActivity() == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                fab.setVisibility(View.GONE);

                mAdapter.clearSelection();
                mAdapter.notifyDataSetChanged();

                if (!intentSuccessful)
                    Toast.makeText(getActivity(), R.string.icon_request_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDragSelectionChanged(int count) {
        updateTitle();
    }

    @Override
    public void onClick(int index, boolean longClick) {
        if (longClick) {
            list.setDragSelectActive(true, index);
        } else {
            mAdapter.toggleSelected(index);
        }
    }
}