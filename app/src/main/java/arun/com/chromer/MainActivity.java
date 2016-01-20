package arun.com.chromer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.activities.AboutAppActivity;
import arun.com.chromer.activities.DonateActivity;
import arun.com.chromer.activities.TabActivity;
import arun.com.chromer.chrometabutilites.CustomTabDelegate;
import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.chrometabutilites.MyCustomTabHelper;
import arun.com.chromer.fragments.PreferenceFragment;
import arun.com.chromer.intro.AppIntroMy;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import arun.com.chromer.util.ChangelogUtil;
import arun.com.chromer.util.PrefUtil;
import arun.com.chromer.util.StringConstants;
import arun.com.chromer.util.Util;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private static final String GOOGLE_URL = "http://www.google.com/";
    private static final String CUSTOM_TAB_URL = "https://developer.chrome.com/multidevice/android/customtabs#whentouse";
    private static final String CHROME_PACKAGE = "com.android.chrome";

    private MyCustomActivityHelper mCustomTabActivityHelper;

    private View mColorView;
    private SwitchCompat mWarmUpSwitch;
    private SwitchCompat mPrefetchSwitch;
    private SwitchCompat mWifiSwitch;
    private ImageView mSecondaryBrowser;
    private SwitchCompat mDynamicSwitch;
    private ImageView mDefaultProviderIcn;

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldBind()) {
            mCustomTabActivityHelper.bindCustomTabsService(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mCustomTabActivityHelper.unbindCustomTabsService(this);
        } catch (Exception e) {
            /* Best effort */
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (PrefUtil.isFirstRun(this)) {
            startActivity(new Intent(this, AppIntroMy.class));
        }

        if (ChangelogUtil.shouldShowChangelog(this)) {
            ChangelogUtil.showChangelogDialog(this);
        }

        setupDrawer(toolbar);

        setupFAB();

        setupCustomTab();

        setupColorPicker();

        findViewById(R.id.set_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDefaultBehaviour();
            }
        });

        setupDefaultProvider();

        setUpSecondaryBrowser();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_fragment, PreferenceFragment.newInstance())
                .commit();

        checkAndEducateUser();

        populateUIBasedOnPreferences();

        takeCareOfServices();
    }

    private void setUpSecondaryBrowser() {
        mSecondaryBrowser = (ImageView) findViewById(R.id.secondary_browser_view);

        setIconWithPackageName(mSecondaryBrowser, PrefUtil.getSecondaryPref(this));

        View secondaryBrowserClickTarget = findViewById(R.id.secondary_browser);
        secondaryBrowserClickTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
                List<ResolveInfo> resolvedActivityList = getPackageManager()
                        .queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
                final List<String> packages = new ArrayList<>();
                for (ResolveInfo info : resolvedActivityList) {
                    if (!info.activityInfo.packageName.equalsIgnoreCase(getPackageName()))
                        packages.add(info.activityInfo.packageName);
                }
                String pack[] = Util.getAppNameFromPackages(MainActivity.this, packages);
                int choice = -1;

                String secondaryPref = PrefUtil.getSecondaryPref(MainActivity.this);
                if (Util.isPackageInstalled(getApplicationContext(), secondaryPref)) {
                    choice = packages.indexOf(secondaryPref);
                }

                new MaterialDialog.Builder(MainActivity.this)
                        .title(getString(R.string.choose_secondary_browser))
                        .items(pack)
                        .itemsCallbackSingleChoice(choice,
                                new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView,
                                                               int which, CharSequence text) {
                                        if (packages != null) {
                                            PrefUtil.setSecondaryPref(MainActivity.this,
                                                    packages.get(which));
                                            try {
                                                mSecondaryBrowser.setImageDrawable(
                                                        getPackageManager()
                                                                .getApplicationIcon(packages.get(which)));
                                            } catch (PackageManager.NameNotFoundException e) {
                                                // Ignore, should not happen
                                            }
                                        }
                                        return true;
                                    }
                                })
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        linkAccessibilityAndPrefetch();
    }

    private void populateUIBasedOnPreferences() {
        linkAccessibilityAndPrefetch();

        mWarmUpSwitch = (SwitchCompat) findViewById(R.id.warm_up_switch);
        mWarmUpSwitch.setChecked(PrefUtil.isWarmUpPreferred(this));
        mWarmUpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtil.setWarmUpPreference(MainActivity.this, isChecked);
                takeCareOfServices();
            }
        });

        mPrefetchSwitch = (SwitchCompat) findViewById(R.id.pre_fetch_switch);
        mPrefetchSwitch.setChecked(PrefUtil.isPreFetchPrefered(this));
        linkWarmUpWithPreference(PrefUtil.isPreFetchPrefered(this));
        mPrefetchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean warmup = isChecked ? false : PrefUtil.isWarmUpPreferred(MainActivity.this);

                if (!Util.isAccessibilityServiceEnabled(MainActivity.this)) {
                    mPrefetchSwitch.setChecked(false);
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.accesiblity_dialog_title)
                            .content(R.string.accesiblity_dialog_desc)
                            .positiveText(R.string.open_settings)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                                }
                            })
                            .show();
                } else {
                    mWarmUpSwitch.setChecked(warmup);
                    PrefUtil.setWarmUpPreference(MainActivity.this, warmup);
                    linkWarmUpWithPreference(isChecked);
                }
                PrefUtil.setPrefetchPreference(MainActivity.this, isChecked);
                takeCareOfServices();
            }
        });

        mWifiSwitch = (SwitchCompat) findViewById(R.id.only_wifi_switch);
        mWifiSwitch.setChecked(PrefUtil.isWifiPreferred(this));
        mWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtil.setWifiPrefetch(MainActivity.this, isChecked);
                takeCareOfServices();
            }
        });

        mDynamicSwitch = (SwitchCompat) findViewById(R.id.dynamic_swich);
        mDynamicSwitch.setChecked(PrefUtil.isDynamicToolbar(this));
        mDynamicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtil.setDynamicToolbar(MainActivity.this, isChecked);
                if (isChecked) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.web_dep_tlbr_clr)
                            .content(R.string.dynamic_toolbar_help)
                            .positiveText(android.R.string.ok)
                            .show();
                }
            }
        });
    }

    private void linkAccessibilityAndPrefetch() {
        if (Util.isAccessibilityServiceEnabled(this)) {
            Timber.d("Scanning permission granted");
            if (mPrefetchSwitch != null)
                mPrefetchSwitch.setChecked(PrefUtil.isPreFetchPrefered(this));
        } else {
            // Turn off preference
            if (mPrefetchSwitch != null)
                mPrefetchSwitch.setChecked(false);
            PrefUtil.setPrefetchPreference(MainActivity.this, false);
        }
    }

    private void linkWarmUpWithPreference(boolean isChecked) {
        if (isChecked) {
            mWarmUpSwitch.setEnabled(false);
        } else {
            mWarmUpSwitch.setEnabled(true);
        }
    }

    private void takeCareOfServices() {
        if (PrefUtil.isWarmUpPreferred(this))
            startService(new Intent(this, WarmupService.class));
        else
            stopService(new Intent(this, WarmupService.class));

        try {
            if (PrefUtil.isPreFetchPrefered(this))
                startService(new Intent(this, ScannerService.class));
            else
                stopService(new Intent(this, ScannerService.class));
        } catch (Exception e) {
            Timber.d("Ignoring startup exception of accessibility service");
        }

    }

    private void setupDefaultProvider() {
        mDefaultProviderIcn = (ImageView) findViewById(R.id.default_provider_view);

        final String preferredApp = PrefUtil.getPreferredTabApp(MainActivity.this);

        setIconWithPackageName(mDefaultProviderIcn, preferredApp);

        findViewById(R.id.default_provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] packagesArray = new String[0];
                final List<String> suppPackages = MyCustomTabHelper.getCustomTabSupportingPackages(getApplicationContext());
                if (suppPackages != null) {
                    packagesArray = Util.getAppNameFromPackages(getApplicationContext(), suppPackages);
                }
                int choice = -1;
                if (suppPackages != null && Util.isPackageInstalled(getApplicationContext(), preferredApp)) {
                    choice = suppPackages.indexOf(preferredApp);
                }
                new MaterialDialog.Builder(MainActivity.this)
                        .title(getString(R.string.choose_default_provider))
                        .items(packagesArray)
                        .itemsCallbackSingleChoice(choice,
                                new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView,
                                                               int which, CharSequence text) {
                                        if (suppPackages != null) {
                                            PrefUtil.setPreferredTabApp(MainActivity.this,
                                                    suppPackages.get(which));
                                        }
                                        return true;
                                    }
                                })
                        .show();
            }
        });
    }

    private void setupFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCustomTab(GOOGLE_URL);
            }
        });
    }

    private void setupColorPicker() {
        final int chosenColor = PrefUtil.getToolbarColor(this);
        findViewById(R.id.color_picker_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorChooserDialog.Builder(MainActivity.this, R.string.md_choose_label)
                        .titleSub(R.string.md_presets_label)
                        .doneButton(R.string.md_done_label)
                        .cancelButton(R.string.md_cancel_label)
                        .backButton(R.string.md_back_label)
                        .allowUserColorInputAlpha(false)
                        .preselect(chosenColor)
                        .dynamicButtonColor(false)
                        .show();
            }
        });
        mColorView = findViewById(R.id.color_preview);
        mColorView.setBackgroundColor(chosenColor);
    }

    @SuppressWarnings("SameParameterValue")
    private void handleDefaultBehaviour() {
        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_URL));
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(launchIntent,
                PackageManager.MATCH_DEFAULT_ONLY);

        String packageName = resolveInfo != null ? resolveInfo.activityInfo.packageName : "";
        if (packageName != null) {
            if (packageName.trim().equalsIgnoreCase(getPackageName())) {
                Timber.d("Chromer defaulted");
                Snackbar.make(mColorView, "Already set!", Snackbar.LENGTH_SHORT).show();
            } else if (packageName.equalsIgnoreCase("android") && Util.isPackageInstalled(this, packageName)) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_URL)));
            } else {
                Intent intent = new Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse(
                        "package:" + packageName));
                Toast.makeText(this,
                        Util.getAppNameWithPackage(this, packageName)
                                + " "
                                + getString(R.string.default_clear_msg), Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        }
    }

    private void setupDrawer(Toolbar toolbar) {
        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.chromer)
                        .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        .withDividerBelowHeader(true)
                        .build())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.intro)).withIdentifier(4)
                                .withIcon(GoogleMaterial.Icon.gmd_assignment)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.feedback)).withIdentifier(2)
                                .withIcon(GoogleMaterial.Icon.gmd_feedback)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.rate_play_store)).withIdentifier(3)
                                .withIcon(GoogleMaterial.Icon.gmd_rate_review)
                                .withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(getString(R.string.more_custom_tbs))
                                .withIcon(GoogleMaterial.Icon.gmd_open_in_new)
                                .withIdentifier(5)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.share))
                                .withIcon(GoogleMaterial.Icon.gmd_share)
                                .withDescription(getString(R.string.help_chromer_grow))
                                .withIdentifier(7)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.support_development))
                                .withDescription(R.string.consider_donation)
                                .withIcon(GoogleMaterial.Icon.gmd_favorite)
                                .withIdentifier(6)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.about))
                                .withIcon(GoogleMaterial.Icon.gmd_info_outline)
                                .withIdentifier(8)
                                .withSelectable(false)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == null)
                            return false;
                        int i = drawerItem.getIdentifier();
                        switch (i) {
                            case 2:
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                                        Uri.fromParts("mailto", StringConstants.MAILID, null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                                startActivity(Intent.createChooser(emailIntent,
                                        getString(R.string.send_email)));
                                break;
                            case 3:
                                Util.openPlayStore(MainActivity.this, getPackageName());
                                break;
                            case 4:
                                startActivity(new Intent(MainActivity.this, AppIntroMy.class));
                                break;
                            case 5:
                                launchCustomTab(CUSTOM_TAB_URL);
                                break;
                            case 6:
                                startActivity(new Intent(MainActivity.this, DonateActivity.class));
                                break;
                            case 7:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                                shareIntent.setType("text/plain");
                                startActivity(Intent.createChooser(shareIntent,
                                        getString(R.string.share_via)));
                                break;
                            case 8:
                                Intent aboutActivityIntent = new Intent(MainActivity.this,
                                        AboutAppActivity.class);
                                startActivity(aboutActivityIntent,
                                        ActivityOptions.makeCustomAnimation(MainActivity.this,
                                                R.anim.slide_in_right,
                                                R.anim.slide_out_left).toBundle()
                                );
                                break;
                        }
                        return false;
                    }
                })
                .build();
        drawer.setSelection(-1);
    }

    private void launchCustomTab(String url) {
        CustomTabsIntent mCustomTabsIntent = CustomTabDelegate.getCustomizedTabIntent(getApplicationContext(), url);
        MyCustomActivityHelper.openCustomTab(this, mCustomTabsIntent, Uri.parse(url),
                TabActivity.mCustomTabsFallback);
    }

    private void setupCustomTab() {
        mCustomTabActivityHelper = new MyCustomActivityHelper();

        if (!shouldBind()) {
            try {
                boolean ok = ScannerService.getInstance().mayLaunchUrl(Uri.parse(GOOGLE_URL));
                if (ok) {
                    return;
                }
            } catch (Exception e) {
                // Ignored - best effort
            }
        }

        mCustomTabActivityHelper.setConnectionCallback(
                new MyCustomActivityHelper.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Timber.d("Connect to custom tab");
                        try {
                            mCustomTabActivityHelper.mayLaunchUrl(Uri.parse(GOOGLE_URL), null, null);
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                    }
                });
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        mColorView.setBackgroundColor(selectedColor);
        PrefUtil.setToolbarColor(this, selectedColor);
    }

    private void checkAndEducateUser() {
        List packages = MyCustomTabHelper.getCustomTabSupportingPackages(this);
        if (packages.size() == 0) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.custom_tab_provider_not_found))
                    .content(getString(R.string.custom_tab_provider_not_found_expln))
                    .positiveText(getString(R.string.install))
                    .negativeText(getString(android.R.string.no))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Util.openPlayStore(MainActivity.this, CHROME_PACKAGE);
                        }
                    }).show();
        }
    }

    private boolean shouldBind() {
        if (PrefUtil.isPreFetchPrefered(this) && Util.isAccessibilityServiceEnabled(this)) {
            return false;
        } else if (!PrefUtil.isPreFetchPrefered(this))
            return true;

        return true;
    }

    private void setIconWithPackageName(ImageView imageView, String packageName) {
        if (imageView == null || packageName == null) return;

        try {
            imageView.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }
    }
}
