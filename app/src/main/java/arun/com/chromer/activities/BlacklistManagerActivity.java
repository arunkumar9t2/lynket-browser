package arun.com.chromer.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import arun.com.chromer.MainActivity;
import arun.com.chromer.R;
import arun.com.chromer.db.BlacklistedApps;
import arun.com.chromer.model.App;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.ServicesUtil;
import arun.com.chromer.util.Util;
import arun.com.chromer.views.IntentPickerSheetView;
import arun.com.chromer.views.adapter.BlackListAppRender;
import timber.log.Timber;

public class BlacklistManagerActivity extends AppCompatActivity implements BlackListAppRender.ItemClickListener {

    private List<App> mApps = new ArrayList<>();
    private List<String> sBlacklistedApps = new ArrayList<>();
    private MaterialDialog mProgress;
    private RecyclerView mRecyclerView;
    private ImageView mSecondaryBrowserIcon;
    private BottomSheetLayout mBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist_manager);
        setupToolbarAndFab();

        mRecyclerView = (RecyclerView) findViewById(R.id.app_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        setupSecondaryBrowser();

        new AppProcessorTask().execute();
    }

    private void setupToolbarAndFab() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupSecondaryBrowser() {
        mSecondaryBrowserIcon = (ImageView) findViewById(R.id.secondary_browser_view);

        setIconWithPackageName(Preferences.secondaryBrowserPackage(this));

        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.GOOGLE_URL));
        final IntentPickerSheetView browserPicker = new IntentPickerSheetView(this,
                webIntent,
                getString(R.string.choose_secondary_browser),
                new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                        mBottomSheet.dismissSheet();
                        String componentNameFlatten = activityInfo.componentName.flattenToString();
                        if (componentNameFlatten != null) {
                            Preferences.secondaryBrowserComponent(getApplicationContext(), componentNameFlatten);
                        }
                        setIconWithPackageName(activityInfo.componentName.getPackageName());
                        snack(String.format(getString(R.string.secondary_browser_success), activityInfo.label));
                    }
                });
        browserPicker.setFilter(new IntentPickerSheetView.Filter() {
            @Override
            public boolean include(IntentPickerSheetView.ActivityInfo info) {
                return !info.componentName.getPackageName().equalsIgnoreCase(getPackageName());
            }
        });
        findViewById(R.id.secondary_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheet != null) mBottomSheet.showWithSheetView(browserPicker);
            }
        });
    }

    private void setIconWithPackageName(String packageName) {
        try {
            mSecondaryBrowserIcon.setImageDrawable(getApplicationContext().getPackageManager().getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            mSecondaryBrowserIcon.setImageDrawable(new IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_error_outline)
                    .color(ContextCompat.getColor(this, R.color.error))
                    .sizeDp(24));
        }
    }

    private void snack(String textToSnack) {
        // Have to provide a view for view traversal, so providing the recycler view.
        Snackbar.make(mRecyclerView, textToSnack, Snackbar.LENGTH_SHORT).show();
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
                boolean shouldCheck = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? Preferences.blacklist(this) && Util.canReadUsageStats(this)
                        : Preferences.blacklist(this);

                Preferences.blacklist(this, shouldCheck);

                blackListSwitch.setChecked(Preferences.blacklist(this));
                snack(String.format(getString(R.string.blacklist_on), Preferences.blacklist(this) ? "on" : "off"));
                blackListSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !Util.canReadUsageStats(getApplicationContext())) {
                            buttonView.setChecked(false);
                            requestUsagePermission();
                        } else {
                            snack(String.format(getString(R.string.blacklist_on), isChecked ? "on" : "off"));
                            Preferences.blacklist(getApplicationContext(), isChecked);
                            ServicesUtil.takeCareOfServices(getApplicationContext());
                        }
                    }
                });
            }
        }
        return true;
    }

    private void requestUsagePermission() {
        new MaterialDialog.Builder(this)
                .title(R.string.permission_required)
                .content(R.string.usage_permission_explanation_blacklist)
                .positiveText(R.string.grant)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO Some devices don't have this activity. Should // FIXME: 28/02/2016
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    }
                }).show();
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
            final PackageManager pm = getApplicationContext().getPackageManager();

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
