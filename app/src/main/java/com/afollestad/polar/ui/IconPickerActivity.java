package com.afollestad.polar.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.ui.base.BelowStatusBarActivity;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @author Aidan Follestad (afollestad)
 */
public class IconPickerActivity extends BelowStatusBarActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    @LayoutRes
    public int getLayout() {
        return R.layout.activity_picker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new IconsFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}