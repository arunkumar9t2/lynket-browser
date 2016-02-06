package arun.com.chromer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
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
import arun.com.chromer.activities.intro.ChromerIntro;
import arun.com.chromer.activities.intro.WebHeadsIntro;
import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.chrometabutilites.CustomTabDelegate;
import arun.com.chromer.chrometabutilites.CustomTabHelper;
import arun.com.chromer.fragments.PersonalizationPreferenceFragment;
import arun.com.chromer.fragments.WebHeadPreferenceFragment;
import arun.com.chromer.model.App;
import arun.com.chromer.services.ScannerService;
import arun.com.chromer.services.WarmupService;
import arun.com.chromer.util.ChangelogUtil;
import arun.com.chromer.util.DatabaseConstants;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.ServicesUtil;
import arun.com.chromer.util.StringConstants;
import arun.com.chromer.util.Util;
import arun.com.chromer.views.IntentPickerSheetView;
import arun.com.chromer.views.MaterialSearchView;
import arun.com.chromer.views.adapter.AppRenderAdapter;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    public static final String GOOGLE_URL = "http://www.google.com/";
    private static final String CUSTOM_TAB_URL = "https://developer.chrome.com/multidevice/android/customtabs#whentouse";
    private static final int VOICE_REQUEST = 10001;

    private CustomActivityHelper mCustomTabActivityHelper;

    private View mColorView;
    private SwitchCompat mWarmUpSwitch;
    private SwitchCompat mPrefetchSwitch;
    private ImageView mSecondaryBrowserIcon;
    private ImageView mDefaultProviderIcn;
    private AppCompatButton mSetDefaultButton;
    private BottomSheetLayout mBottomSheet;
    private ImageView mFavShareAppIcon;
    private MaterialSearchView mMaterialSearchView;

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldBind()) {
            mCustomTabActivityHelper.bindCustomTabsService(this);
        }

        // startService(new Intent(this, WebHeadService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
        // stopService(new Intent(this, WebHeadService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePrefetchIfPermissionGranted();
        setupDefaultBrowser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Preferences.isFirstRun(this)) {
            startActivity(new Intent(this, ChromerIntro.class));
        }

        if (ChangelogUtil.shouldShowChangelog(this)) {
            ChangelogUtil.showChangelogDialog(this);
        }

        mBottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);

        setupDefaultBrowser();

        setupMaterialSearch();

        setupDrawer(toolbar);

        setupFAB();

        setupSwitches();

        setupCustomTab();

        setupColorPicker();

        setupDefaultProvider();

        setupSecondaryBrowser();

        setupFavShareApp();

        attachFragments();

        setUpWebHeadsInro();

        checkAndEducateUser();

        ServicesUtil.takeCareOfServices(getApplicationContext());

        cleanOldDbs();
    }

    private void attachFragments() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.webhead_container, WebHeadPreferenceFragment.newInstance());
        ft.replace(R.id.preference_container, PersonalizationPreferenceFragment.newInstance())
                .commit();
    }

    private void setupMaterialSearch() {
        mMaterialSearchView = (MaterialSearchView) findViewById(R.id.material_search_view);
        mMaterialSearchView.clearFocus();
        mMaterialSearchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    launchCustomTab(Util.processSearchText(mMaterialSearchView.getText()));
                    return true;
                }
                return false;
            }
        });
        mMaterialSearchView.setVoiceIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isVoiceRecognizerPresent(getApplicationContext())) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                    startActivityForResult(intent, VOICE_REQUEST);

                } else snack("No compatible voice recognition apps found");

            }
        });
    }

    private void snack(String textToSnack) {
        // Have to provide a view for view traversal, so providing the set default button.
        Snackbar.make(mSetDefaultButton, textToSnack, Snackbar.LENGTH_SHORT).show();
    }


    private void setupSecondaryBrowser() {
        mSecondaryBrowserIcon = (ImageView) findViewById(R.id.secondary_browser_view);

        setIconWithPackageName(mSecondaryBrowserIcon, Preferences.secondaryBrowserPackage(this));

        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_URL));
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
                        setIconWithPackageName(mSecondaryBrowserIcon, activityInfo.componentName.getPackageName());
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

    private void setupSwitches() {
        updatePrefetchIfPermissionGranted();

        mWarmUpSwitch = (SwitchCompat) findViewById(R.id.warm_up_switch);
        mWarmUpSwitch.setChecked(Preferences.preFetch(this) || Preferences.warmUp(this));
        mWarmUpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.warmUp(getApplicationContext(), isChecked);
                ServicesUtil.takeCareOfServices(getApplicationContext());
            }
        });

        mPrefetchSwitch = (SwitchCompat) findViewById(R.id.pre_fetch_switch);
        mPrefetchSwitch.setChecked(Preferences.preFetch(this));
        enableDisableWarmUpSwitch(Preferences.preFetch(this));
        mPrefetchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean warmUp = !isChecked && Preferences.warmUp(getApplicationContext());

                if (!Util.isAccessibilityServiceEnabled(getApplicationContext())) {
                    mPrefetchSwitch.setChecked(false);
                    guideUserToSettings();
                } else {
                    mWarmUpSwitch.setChecked(!warmUp);
                    Preferences.warmUp(getApplicationContext(), warmUp);
                    enableDisableWarmUpSwitch(isChecked);
                }
                Preferences.preFetch(getApplicationContext(), isChecked);

                if (!isChecked)
                    // Since pre fetch is not active, the  warm up preference should properly reflect what's on the
                    // UI, hence setting the preference to the checked value of the warm up switch.
                    Preferences.warmUp(getApplicationContext(), mWarmUpSwitch.isChecked());

                ServicesUtil.takeCareOfServices(getApplicationContext());
            }
        });

        SwitchCompat mWifiSwitch = (SwitchCompat) findViewById(R.id.only_wifi_switch);
        mWifiSwitch.setChecked(Preferences.wifiOnlyPrefetch(this));
        mWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.wifiOnlyPrefetch(getApplicationContext(), isChecked);
                ServicesUtil.takeCareOfServices(getApplicationContext());
            }
        });
    }

    private void guideUserToSettings() {
        new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.accessibility_dialog_title)
                .content(R.string.accessibility_dialog_desc)
                .positiveText(R.string.open_settings)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                        // Close the dialog to avoid potential memory leak
                    }
                })
                .show();
    }

    private void updatePrefetchIfPermissionGranted() {
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

    private void enableDisableWarmUpSwitch(boolean isChecked) {
        if (isChecked) {
            mWarmUpSwitch.setEnabled(false);
        } else {
            mWarmUpSwitch.setEnabled(true);
        }
    }

    private void setupDefaultProvider() {
        mDefaultProviderIcn = (ImageView) findViewById(R.id.default_provider_view);

        final String preferredApp = Preferences.customTabApp(MainActivity.this);

        if (preferredApp == null || preferredApp.length() == 0)
            // Setting an error icon
            mDefaultProviderIcn.setImageDrawable(new IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_error_outline)
                    .color(ContextCompat.getColor(this, R.color.error))
                    .sizeDp(24));
        else setIconWithPackageName(mDefaultProviderIcn, preferredApp);

        findViewById(R.id.default_provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<App> customTabApps = Util.getCustomTabApps(getApplicationContext());

                if (customTabApps.size() == 0) {
                    checkAndEducateUser();
                    return;
                }
                new MaterialDialog.Builder(MainActivity.this)
                        .title(getString(R.string.choose_default_provider))
                        .adapter(new AppRenderAdapter(getApplicationContext(), customTabApps),
                                new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        App app = customTabApps.get(which);
                                        if (app != null) {
                                            String packageName = app.getPackageName();
                                            Preferences.customTabApp(getApplicationContext(), packageName);
                                            setIconWithPackageName(mDefaultProviderIcn, packageName);
                                            snack(String.format(getString(R.string.default_provider_success), app.getAppName()));

                                            // Refresh bindings so as to reflect changed custom tab package
                                            refreshCustomTabBindings();
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
                if (mMaterialSearchView.hasFocus() && mMaterialSearchView.getText().length() > 0) {
                    launchCustomTab(Util.processSearchText(mMaterialSearchView.getText()));
                } else
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
                        Snackbar.make(mColorView, "Already set!", Snackbar.LENGTH_SHORT).show();
                    } else if (defaultBrowserPackage.equalsIgnoreCase("android")
                            && Util.isPackageInstalled(getApplicationContext(), defaultBrowserPackage)) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_URL)));
                    } else {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
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
                                startActivity(new Intent(MainActivity.this, ChromerIntro.class));
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

    private void setupFavShareApp() {
        mFavShareAppIcon = (ImageView) findViewById(R.id.fav_share_app_view);

        setIconWithPackageName(mFavShareAppIcon, Preferences.favSharePackage(this));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "");
        final IntentPickerSheetView picker = new IntentPickerSheetView(this,
                shareIntent,
                getString(R.string.choose_fav_share_app),
                new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                        mBottomSheet.dismissSheet();
                        String componentNameFlatten = activityInfo.componentName.flattenToString();
                        if (componentNameFlatten != null) {
                            Preferences.favShareComponent(getApplicationContext(), componentNameFlatten);
                        }
                        setIconWithPackageName(mFavShareAppIcon,
                                activityInfo.componentName.getPackageName());
                        snack(String.format(getString(R.string.fav_share_success),
                                activityInfo.label));
                    }
                });
        picker.setFilter(new IntentPickerSheetView.Filter() {
            @Override
            public boolean include(IntentPickerSheetView.ActivityInfo info) {
                return !info.componentName.getPackageName().startsWith("com.android")
                        && !info.componentName.getPackageName().equalsIgnoreCase(getPackageName());
            }
        });
        findViewById(R.id.fav_share_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheet != null) mBottomSheet.showWithSheetView(picker);
            }
        });
    }

    private void launchCustomTab(String url) {
        CustomTabsIntent customTabsIntent = CustomTabDelegate.getCustomizedTabIntent(getApplicationContext(), url);
        CustomActivityHelper.openCustomTab(this, customTabsIntent, Uri.parse(url), TabActivity.mCustomTabsFallback);
    }

    private boolean shouldBind() {
        if (Preferences.warmUp(this)) return false;
        if (Preferences.preFetch(this) && Util.isAccessibilityServiceEnabled(this)) {
            return false;
        } else if (!Preferences.preFetch(this))
            return true;

        return true;
    }

    private void refreshCustomTabBindings() {
        // Unbind from currently bound service
        mCustomTabActivityHelper.unbindCustomTabsService(this);
        setupCustomTab();
        mCustomTabActivityHelper.bindCustomTabsService(this);

        // Restarting services will make them update their bindings.
        ServicesUtil.refreshCustomTabBindings(getApplicationContext());

    }

    private void setupCustomTab() {
        mCustomTabActivityHelper = new CustomActivityHelper();
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
                new CustomActivityHelper.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Timber.d("Connect to custom tab in main activity");
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
        List packages = CustomTabHelper.getCustomTabSupportingPackages(this);
        if (packages.size() == 0) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.custom_tab_provider_not_found))
                    .content(getString(R.string.custom_tab_provider_not_found_expln))
                    .positiveText(getString(R.string.install))
                    .negativeText(getString(android.R.string.no))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            Util.openPlayStore(getApplicationContext(), StringConstants.CHROME_PACKAGE);
                        }
                    }).show();
        }
    }

    private void setIconWithPackageName(ImageView imageView, String packageName) {
        if (imageView == null || packageName == null) return;

        try {
            imageView.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void cleanOldDbs() {
        if (Preferences.shouldCleanDB(this)) {
            boolean ok = deleteDatabase(DatabaseConstants.DATABASE_NAME);
            Timber.d("Deleted " + DatabaseConstants.DATABASE_NAME + ": " + ok);
            ok = deleteDatabase(DatabaseConstants.OLD_DATABASE_NAME);
            Timber.d("Deleted " + DatabaseConstants.OLD_DATABASE_NAME + ": " + ok);
        }
    }

    @Override
    public void onBackPressed() {
        if (mMaterialSearchView.hasFocus()) {
            mMaterialSearchView.clearFocus();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    List<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (!resultList.isEmpty()) {
                        launchCustomTab(Util.processSearchText(resultList.get(0)));
                    }
                    break;
                default:
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpWebHeadsInro() {
        findViewById(R.id.web_heads_intro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), WebHeadsIntro.class));
            }
        });
    }
}
