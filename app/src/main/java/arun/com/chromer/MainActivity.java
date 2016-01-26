package arun.com.chromer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
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
import arun.com.chromer.adapter.AppRenderAdapter;
import arun.com.chromer.adapter.SecondaryBrowserAdapter;
import arun.com.chromer.chrometabutilites.CustomTabDelegate;
import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.chrometabutilites.MyCustomTabHelper;
import arun.com.chromer.fragments.PreferenceFragment;
import arun.com.chromer.intro.AppIntroMy;
import arun.com.chromer.model.App;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import arun.com.chromer.util.ChangelogUtil;
import arun.com.chromer.util.DatabaseConstants;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.StringConstants;
import arun.com.chromer.util.Util;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    public static final String GOOGLE_URL = "http://www.google.com/";
    private static final String CUSTOM_TAB_URL = "https://developer.chrome.com/multidevice/android/customtabs#whentouse";
    private static final String CHROME_PACKAGE = "com.android.chrome";

    private MyCustomActivityHelper mCustomTabActivityHelper;

    private View mColorView;
    private SwitchCompat mWarmUpSwitch;
    private SwitchCompat mPrefetchSwitch;
    private SwitchCompat mWifiSwitch;
    private ImageView mSecondaryBrowser;
    private ImageView mDefaultProviderIcn;
    private AppCompatButton mSetDefaultButton;

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

        if (Preferences.isFirstRun(this)) {
            startActivity(new Intent(this, AppIntroMy.class));
        }

        if (ChangelogUtil.shouldShowChangelog(this)) {
            ChangelogUtil.showChangelogDialog(this);
        }

        setupDrawer(toolbar);

        setupFAB();

        populateUIBasedOnPreferences();

        setupCustomTab();

        setupColorPicker();

        setupDefaultBrowser();

        setupDefaultProvider();

        setUpSecondaryBrowser();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_fragment, PreferenceFragment.newInstance())
                .commit();

        checkAndEducateUser();

        takeCareOfServices();

        cleanOldDbs();
    }

    private void snack(String textToSnack) {
        // Have to provide a view for view traversal, so providing the set default button.
        Snackbar.make(mSetDefaultButton, textToSnack, Snackbar.LENGTH_SHORT).show();
    }


    private void setUpSecondaryBrowser() {
        mSecondaryBrowser = (ImageView) findViewById(R.id.secondary_browser_view);

        setIconWithPackageName(mSecondaryBrowser, Preferences.secondaryBrowser(this));

        View secondaryBrowserClickTarget = findViewById(R.id.secondary_browser);
        secondaryBrowserClickTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<App> compatibleApps = Util.getScndryBrwsrApps(getApplicationContext());

                new MaterialDialog.Builder(MainActivity.this)
                        .title(getString(R.string.choose_secondary_browser))
                        .adapter(new SecondaryBrowserAdapter(getApplicationContext(), compatibleApps),
                                new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        App app = compatibleApps.get(which);
                                        if (app != null) {
                                            Preferences.secondaryBrowser(getApplicationContext(), app.getPackageName());
                                            setIconWithPackageName(mSecondaryBrowser, app.getPackageName());
                                            snack(String.format(getString(R.string.secondary_browser_success), app.getAppName()));
                                        }
                                        if (dialog != null) dialog.dismiss();
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
        setupDefaultBrowser();
    }

    private void populateUIBasedOnPreferences() {
        linkAccessibilityAndPrefetch();

        mWarmUpSwitch = (SwitchCompat) findViewById(R.id.warm_up_switch);
        mWarmUpSwitch.setChecked(Preferences.preFetch(this) || Preferences.warmUp(this));
        mWarmUpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.warmUp(getApplicationContext(), isChecked);
                takeCareOfServices();
            }
        });

        mPrefetchSwitch = (SwitchCompat) findViewById(R.id.pre_fetch_switch);
        mPrefetchSwitch.setChecked(Preferences.preFetch(this));
        linkWarmUpWithPrefetch(Preferences.preFetch(this));
        mPrefetchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean warmup = !isChecked && Preferences.warmUp(getApplicationContext());

                if (!Util.isAccessibilityServiceEnabled(getApplicationContext())) {
                    mPrefetchSwitch.setChecked(false);
                    guideUserToSettings();
                } else {
                    mWarmUpSwitch.setChecked(!warmup);
                    Preferences.warmUp(getApplicationContext(), warmup);
                    linkWarmUpWithPrefetch(isChecked);
                }
                Preferences.preFetch(getApplicationContext(), isChecked);

                if (!isChecked)
                    // Since pre fetch is not active, the  warmup preference should properly reflect what's on the
                    // UI, hence setting the preference to the checked value of the warm up switch.
                    Preferences.warmUp(getApplicationContext(), mWarmUpSwitch.isChecked());

                takeCareOfServices();
            }
        });

        mWifiSwitch = (SwitchCompat) findViewById(R.id.only_wifi_switch);
        mWifiSwitch.setChecked(Preferences.wifiOnlyPrefetch(this));
        mWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.wifiOnlyPrefetch(getApplicationContext(), isChecked);
                takeCareOfServices();
            }
        });

        setupDynamicToolbar();
    }

    private void setupDynamicToolbar() {
        SwitchCompat mDynamicSwitch = (SwitchCompat) findViewById(R.id.dynamic_swich);
        mDynamicSwitch.setChecked(Preferences.dynamicToolbar(this));
        mDynamicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.dynamicToolbar(getApplicationContext(), isChecked);
                if (isChecked) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.dynamic_toolbar_color)
                            .content(R.string.dynamic_toolbar_help)
                            .items(new String[]{
                                    getString(R.string.based_on_app),
                                    getString(R.string.based_on_web)})
                            .positiveText(android.R.string.ok)
                            .alwaysCallMultiChoiceCallback()
                            .itemsCallbackMultiChoice(Preferences.dynamicToolbarSelections(getApplicationContext()),
                                    new MaterialDialog.ListCallbackMultiChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog,
                                                                   Integer[] which,
                                                                   CharSequence[] text) {
                                            Preferences.updateAppAndWeb(getApplicationContext(), which);
                                            takeCareOfServices();
                                            return true;
                                        }
                                    })
                            .show();
                }
            }
        });
    }

    private void guideUserToSettings() {
        new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.accesiblity_dialog_title)
                .content(R.string.accesiblity_dialog_desc)
                .positiveText(R.string.open_settings)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                    }
                })
                .show();
    }

    private void linkAccessibilityAndPrefetch() {
        if (Util.isAccessibilityServiceEnabled(this)) {
            Timber.d("Scanning permission granted");
            if (mPrefetchSwitch != null)
                mPrefetchSwitch.setChecked(Preferences.preFetch(this));
        } else {
            // Turn off preference
            if (mPrefetchSwitch != null)
                mPrefetchSwitch.setChecked(false);
            Preferences.preFetch(MainActivity.this, false);
        }
    }

    private void linkWarmUpWithPrefetch(boolean isChecked) {
        if (isChecked) {
            mWarmUpSwitch.setEnabled(false);
        } else {
            mWarmUpSwitch.setEnabled(true);
        }
    }

    private void takeCareOfServices() {
        if (Preferences.warmUp(this))
            startService(new Intent(this, WarmupService.class));
        else
            stopService(new Intent(this, WarmupService.class));

        if (Preferences.dynamicToolbarOnApp(this) && Preferences.dynamicToolbar(this))
            startService(new Intent(this, AppDetectService.class));
        else
            stopService(new Intent(this, AppDetectService.class));

        try {
            if (Preferences.preFetch(this))
                startService(new Intent(this, ScannerService.class));
            else
                stopService(new Intent(this, ScannerService.class));
        } catch (Exception e) {
            Timber.d("Ignoring startup exception of accessibility service");
        }

    }

    private void setupDefaultProvider() {
        mDefaultProviderIcn = (ImageView) findViewById(R.id.default_provider_view);

        final String preferredApp = Preferences.customTabApp(MainActivity.this);

        setIconWithPackageName(mDefaultProviderIcn, preferredApp);

        findViewById(R.id.default_provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<App> customTabApps = Util.getCustomTabApps(getApplicationContext());

                new MaterialDialog.Builder(MainActivity.this)
                        .title(getString(R.string.choose_default_provider))
                        .adapter(new AppRenderAdapter(getApplicationContext(), customTabApps),
                                new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        App app = customTabApps.get(which);
                                        if (app != null) {
                                            Preferences.customTabApp(getApplicationContext(), app.getPackageName());
                                            setIconWithPackageName(mDefaultProviderIcn, app.getPackageName());
                                            snack(String.format(getString(R.string.default_provider_success), app.getAppName()));
                                        }
                                        if (dialog != null) dialog.dismiss();
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
        final int chosenColor = Preferences.toolbarColor(this);
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

    private void setupDefaultBrowser() {
        final String defaultBrowserPackage = Util.getDefaultBrowserPackage(MainActivity.this);

        mSetDefaultButton = (AppCompatButton) findViewById(R.id.set_default);
        mSetDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultBrowserPackage != null) {
                    if (defaultBrowserPackage.trim().equalsIgnoreCase(getPackageName())) {
                        Timber.d("Chromer defaulted");
                        Snackbar.make(mColorView, "Already set!", Snackbar.LENGTH_SHORT).show();
                    } else if (defaultBrowserPackage.equalsIgnoreCase("android") && Util.isPackageInstalled(getApplicationContext(), defaultBrowserPackage)) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_URL)));
                    } else {
                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + defaultBrowserPackage));
                        Toast.makeText(getApplicationContext(),
                                Util.getAppNameWithPackage(getApplicationContext(), defaultBrowserPackage)
                                        + " "
                                        + getString(R.string.default_clear_msg), Toast.LENGTH_LONG).show();
                        startActivity(intent);
                    }
                }
            }
        });

        if (defaultBrowserPackage.trim().equalsIgnoreCase(getPackageName())) {
            mSetDefaultButton.setVisibility(View.GONE);
            ImageView defaultSuccessIcon = (ImageView) findViewById(R.id.default_icon_c);
            defaultSuccessIcon.setVisibility(View.VISIBLE);
            defaultSuccessIcon.setImageDrawable(new IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_check_circle)
                    .color(ContextCompat.getColor(this, R.color.default_success))
                    .sizeDp(24));
            TextView explanation = (TextView) findViewById(R.id.default_setting_xpln);
            explanation.setText(R.string.chromer_defaulted);
            explanation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
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
        MyCustomActivityHelper.openCustomTab(this, mCustomTabsIntent, Uri.parse(url), TabActivity.mCustomTabsFallback);
    }

    private void setupCustomTab() {
        mCustomTabActivityHelper = new MyCustomActivityHelper();
        List<Bundle> possibleUrls = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(CUSTOM_TAB_URL));
        possibleUrls.add(bundle);

        if (!shouldBind()) {
            try {
                boolean ok;
                if (ScannerService.getInstance() != null) {
                    ok = ScannerService.getInstance().mayLaunchUrl(Uri.parse(GOOGLE_URL), possibleUrls);
                    if (ok) return;
                }
                if (WarmupService.getInstance() != null) {
                    ok = WarmupService.getInstance().mayLaunchUrl(Uri.parse(GOOGLE_URL), possibleUrls);
                    if (ok) return;
                }
            } catch (Exception e) {
                // Ignored - best effort
                // If mayLaunch with a service failed, then we will bind a connection with this activity
                // and pre fetch the google url.
                e.printStackTrace();
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
                            e.printStackTrace();
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
        Preferences.toolbarColor(this, selectedColor);
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
                            Util.openPlayStore(getApplicationContext(), CHROME_PACKAGE);
                        }
                    }).show();
        }
    }

    private boolean shouldBind() {
        if (Preferences.warmUp(this)) return false;
        if (Preferences.preFetch(this) && Util.isAccessibilityServiceEnabled(this)) {
            return false;
        } else if (!Preferences.preFetch(this))
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

    private void cleanOldDbs() {
        if (Preferences.shouldCleanDB(this)) {
            boolean ok = deleteDatabase(DatabaseConstants.DATABASE_NAME);
            Timber.d("Deleted " + DatabaseConstants.DATABASE_NAME + ": " + ok);
            ok = deleteDatabase(DatabaseConstants.OLD_DATABASE_NAME);
            Timber.d("Deleted " + DatabaseConstants.OLD_DATABASE_NAME + ": " + ok);
        } else
            Timber.d("Skipped cleaning DB");
    }
}
