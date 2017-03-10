package arun.com.chromer.activities.blacklist;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.activities.SnackHelper;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.common.App;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BlacklistManagerActivity extends AppCompatActivity implements
        Blacklist.View,
        BlacklistAdapter.BlackListItemClickedListener,
        CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener, SnackHelper {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_recyclerview)
    RecyclerView blackListedAppsList;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private Blacklist.Presenter presenter;

    private BlacklistAdapter blacklistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new Blacklist.Presenter(this);
        setContentView(R.layout.activity_blacklist);
        ButterKnife.bind(this);
        setupToolbar();

        blacklistAdapter = new BlacklistAdapter(this, this);
        blackListedAppsList.setLayoutManager(new LinearLayoutManager(this));
        blackListedAppsList.setAdapter(blacklistAdapter);
        loadApps();
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
        presenter.cleanUp();
        blacklistAdapter.cleanUp();
        super.onDestroy();
    }

    @Override
    public void onBlackListItemClick(App app) {
        presenter.updateBlacklist(this, app);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.blacklist_switch_item);
        if (menuItem != null) {
            final SwitchCompat blackListSwitch = (SwitchCompat) menuItem.getActionView().findViewById(R.id.blacklist_switch);
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
            ServiceUtil.takeCareOfServices(getApplicationContext());
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
