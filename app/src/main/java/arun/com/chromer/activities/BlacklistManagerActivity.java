package arun.com.chromer.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import arun.com.chromer.R;
import arun.com.chromer.db.BlacklistedApps;
import arun.com.chromer.model.App;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.ServicesUtil;
import arun.com.chromer.util.Util;
import arun.com.chromer.views.adapter.BlackListAppRender;
import timber.log.Timber;

public class BlacklistManagerActivity extends AppCompatActivity implements BlackListAppRender.ItemClickListener {

    private List<App> mApps = new ArrayList<>();
    private List<String> sBlacklistedApps = new ArrayList<>();
    private MaterialDialog mProgress;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist_manager);
        setupToolbarAndFab();

        mRecyclerView = (RecyclerView) findViewById(R.id.app_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new AppProcessorTask().execute();
    }

    private void setupToolbarAndFab() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(int position, App app, boolean checked) {
        updateBlacklists(app.getPackageName(), checked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_menu, menu);
        MenuItem item = menu.findItem(R.id.blacklist_switch_item);
        if (item != null) {
            SwitchCompat blackListSwitch = (SwitchCompat) item.getActionView().findViewById(R.id.blacklist_switch);
            if (blackListSwitch != null) {
                blackListSwitch.setChecked(Preferences.blacklist(this));
                blackListSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Preferences.blacklist(getApplicationContext(), isChecked);
                        ServicesUtil.takeCareOfServices(getApplicationContext());
                    }
                });
            }
        }
        return true;
    }

    private void updateBlacklists(String packageName, boolean checked) {
        if (packageName == null) return;
        List<BlacklistedApps> blacklisted = BlacklistedApps.find(BlacklistedApps.class, "package_name = ?", packageName);
        BlacklistedApps blackListedApp = null;
        if (blacklisted.size() > 0 && blacklisted.get(0).getPackageName().equalsIgnoreCase(packageName)) {
            blackListedApp = blacklisted.get(0);
        }
        if (checked) {
            if (blackListedApp == null) {
                blackListedApp = new BlacklistedApps(packageName);
                blackListedApp.save();
            }
        } else if (blackListedApp != null) blackListedApp.delete();
    }

    private void initList() {
        BlackListAppRender adapter = new BlackListAppRender(BlacklistManagerActivity.this, mApps);
        adapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    private void getBlacklistedPkgsFromDB() {
        List<BlacklistedApps> blacklistedApps = BlacklistedApps.listAll(BlacklistedApps.class);
        for (BlacklistedApps blacklistedApp : blacklistedApps) {
            if (blacklistedApp.getPackageName() != null) {
                sBlacklistedApps.add(blacklistedApp.getPackageName());
            }
        }
        Timber.d(sBlacklistedApps.toString());
    }

    private class AppProcessorTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress = new MaterialDialog.Builder(BlacklistManagerActivity.this)
                    .title(R.string.loading)
                    .content(R.string.please_wait)
                    .progress(true, 0).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getBlacklistedPkgsFromDB();
            final PackageManager pm = getPackageManager();

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveList = pm.queryIntentActivities(intent, 0);

            SortedSet<App> sortedSet = new TreeSet<>();
            for (ResolveInfo resolveInfo : resolveList) {
                String pkg = resolveInfo.activityInfo.packageName;

                if (pkg.equalsIgnoreCase(getPackageName())) continue;

                App app = new App();
                app.setAppName(Util.getAppNameWithPackage(getApplicationContext(), pkg));
                app.setPackageName(pkg);
                app.setBlackListed(sBlacklistedApps.contains(pkg));
                sortedSet.add(app);
            }
            mApps.addAll(sortedSet);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.dismiss();
            initList();
        }
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
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
