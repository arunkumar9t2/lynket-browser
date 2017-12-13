/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.blacklist;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.data.common.App;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.shared.base.Snackable;
import arun.com.chromer.shared.base.activity.BaseMVPActivity;
import arun.com.chromer.util.ServiceManager;
import arun.com.chromer.util.Utils;
import butterknife.BindView;

public class BlacklistManagerActivity extends BaseMVPActivity<Blacklist.View, Blacklist.Presenter> implements
        Blacklist.View,
        CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener, Snackable {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_recyclerview)
    RecyclerView blackListedAppsList;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private BlacklistAdapter blacklistAdapter;

    @Inject
    Blacklist.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();

        blacklistAdapter = new BlacklistAdapter(this);
        blackListedAppsList.setLayoutManager(new LinearLayoutManager(this));
        blackListedAppsList.setAdapter(blacklistAdapter);
        presenter.handleSelections(blacklistAdapter.clicks());
        loadApps();
    }

    @Override
    public void inject(@NonNull ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    private void loadApps() {
        presenter.loadAppList(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDarker);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void onDestroy() {
        blacklistAdapter.cleanUp();
        super.onDestroy();
    }

    @NonNull
    @Override
    public Blacklist.Presenter createPresenter() {
        return presenter;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_blacklist;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.blacklist_switch_item);
        if (menuItem != null) {
            final SwitchCompat blackListSwitch = menuItem.getActionView().findViewById(R.id.blacklist_switch);
            if (blackListSwitch != null) {
                final boolean blackListActive = Preferences.get(this).blacklist() && Utils.canReadUsageStats(this);
                Preferences.get(this).blacklist(blackListActive);
                blackListSwitch.setChecked(Preferences.get(this).blacklist());
                blackListSwitch.setOnCheckedChangeListener(this);
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestUsagePermission() {
        new MaterialDialog.Builder(this)
                .title(R.string.permission_required)
                .content(R.string.usage_permission_explanation_blacklist)
                .positiveText(R.string.grant)
                .onPositive((dialog, which) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // For finishing activity on clicking up caret
            finishWithTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithTransition();
        super.onBackPressed();
    }

    private void finishWithTransition() {
        finish();
        overridePendingTransition(R.anim.slide_in_left_medium, R.anim.slide_out_right_medium);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !Utils.canReadUsageStats(getApplicationContext())) {
            buttonView.setChecked(false);
            requestUsagePermission();
        } else {
            snack(isChecked ? getString(R.string.blacklist_on) : getString(R.string.blacklist_off));
            Preferences.get(this).blacklist(isChecked);
            ServiceManager.takeCareOfServices(getApplicationContext());
        }
    }

    @Override
    public void setApps(@NonNull List<App> apps) {
        blacklistAdapter.setApps(apps);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void onRefresh() {
        loadApps();
    }

    @Override
    public void snack(@NonNull String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void snackLong(@NonNull String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}
