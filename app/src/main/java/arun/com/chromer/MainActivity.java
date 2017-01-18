package arun.com.chromer;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Collections;
import java.util.List;

import arun.com.chromer.activities.about.AboutAppActivity;
import arun.com.chromer.activities.about.changelog.Changelog;
import arun.com.chromer.activities.intro.ChromerIntro;
import arun.com.chromer.activities.intro.WebHeadsIntro;
import arun.com.chromer.activities.payments.DonateActivity;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.fragments.CustomizeFragment;
import arun.com.chromer.fragments.OptionsFragment;
import arun.com.chromer.fragments.WebHeadsFragment;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.AppPreferenceCardView;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Benchmark;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;
import arun.com.chromer.views.IntentPickerSheetView;
import arun.com.chromer.views.MaterialSearchView;
import arun.com.chromer.views.TabView;
import arun.com.chromer.webheads.WebHeadService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, OptionsFragment.FragmentInteractionListener {

    @BindView(R.id.bottomsheet)
    public BottomSheetLayout mBottomSheet;
    @BindView(R.id.material_search_view)
    public MaterialSearchView mMaterialSearchView;
    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.tab_layout)
    public TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    public ViewPager mViewPager;
    @BindView(R.id.coordinator_layout)
    public CoordinatorLayout mCoordinatorLayout;

    private CustomTabManager mCustomTabManager;
    private Drawer mDrawer;
    private BroadcastReceiver closeReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabManager.bindCustomTabsService(this);
        // startService(new Intent(this, WebHeadService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabManager.unbindCustomTabsService(this);
        // stopService(new Intent(this, WebHeadService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        if (Preferences.isFirstRun(this)) {
            startActivity(new Intent(this, ChromerIntro.class));
        }

        Changelog.conditionalShow(this);

        setUpAppBarLayout();

        setupMaterialSearch();

        setupDrawer();

        setupCustomTab();

        checkAndEducateUser(false);

        ServiceUtil.takeCareOfServices(getApplicationContext());

        registerCloseReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
    }

    private void registerCloseReceiver() {
        closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Finished from receiver");
                MainActivity.this.finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                closeReceiver,
                new IntentFilter(Constants.ACTION_CLOSE_MAIN));
    }

    private void setUpAppBarLayout() {
        setSupportActionBar(mToolbar);

        mViewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        final TabLayout.Tab optionsTab = mTabLayout.getTabAt(0);
        if (optionsTab != null) {
            final TabView tabView = new TabView(this, TabView.TAB_TYPE_OPTIONS);
            optionsTab.setCustomView(tabView);
            tabView.setSelected(true);
        }

        final TabLayout.Tab webHeadsTab = mTabLayout.getTabAt(1);
        if (webHeadsTab != null) {
            webHeadsTab.setCustomView(new TabView(this, TabView.TAB_TYPE_WEB_HEADS));
        }

        final TabLayout.Tab customizeTab = mTabLayout.getTabAt(2);
        if (customizeTab != null) {
            customizeTab.setCustomView(new TabView(this, TabView.TAB_TYPE_CUSTOMIZE));
        }
    }

    private void setupMaterialSearch() {
        mMaterialSearchView.clearFocus();
        mMaterialSearchView.setInteractionListener(new MaterialSearchView.InteractionListener() {
            @Override
            public void onVoiceIconClick() {
                if (Utils.isVoiceRecognizerPresent(getApplicationContext())) {
                    startActivityForResult(Utils.getRecognizerIntent(MainActivity.this), Constants.REQUEST_CODE_VOICE);
                } else {
                    snack(getString(R.string.no_voice_rec_apps));
                }
            }

            @Override
            public void onSearchPerformed(@NonNull String url) {
                launchCustomTab(url);
            }

            @Override
            public void onMenuClick() {
                if (mDrawer.isDrawerOpen()) {
                    mDrawer.closeDrawer();
                } else {
                    mDrawer.openDrawer();
                }
            }
        });
    }

    public void snack(@NonNull String textToSnack) {
        Snackbar.make(mCoordinatorLayout, textToSnack, Snackbar.LENGTH_SHORT).show();
    }

    public void snackLong(@NonNull String textToSnack) {
        Snackbar.make(mCoordinatorLayout, textToSnack, Snackbar.LENGTH_LONG).show();
    }


    private void setupDrawer() {
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.chromer)
                        .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        .withDividerBelowHeader(true)
                        .build())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.intro)).withIdentifier(4)
                                .withIcon(CommunityMaterial.Icon.cmd_clipboard_text)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.web_heads_intro)).withIdentifier(10)
                                .withIcon(CommunityMaterial.Icon.cmd_chart_bubble)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.feedback)).withIdentifier(2)
                                .withIcon(CommunityMaterial.Icon.cmd_message_text)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.rate_play_store)).withIdentifier(3)
                                .withIcon(CommunityMaterial.Icon.cmd_comment_text)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.join_beta)
                                .withIdentifier(9)
                                .withIcon(CommunityMaterial.Icon.cmd_beta)
                                .withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(getString(R.string.more_custom_tbs))
                                .withIcon(CommunityMaterial.Icon.cmd_open_in_new)
                                .withIdentifier(5)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.share))
                                .withIcon(CommunityMaterial.Icon.cmd_share_variant)
                                .withDescription(getString(R.string.help_chromer_grow))
                                .withIdentifier(7)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.support_development))
                                .withDescription(R.string.consider_donation)
                                .withIcon(CommunityMaterial.Icon.cmd_heart)
                                .withIconColorRes(R.color.accent)
                                .withIdentifier(6)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.about))
                                .withIcon(CommunityMaterial.Icon.cmd_information)
                                .withIdentifier(8)
                                .withSelectable(false)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == null)
                            return false;
                        int i = (int) drawerItem.getIdentifier();
                        switch (i) {
                            case 2:
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                                        Uri.fromParts("mailto", Constants.MAILID, null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
                                break;
                            case 3:
                                Utils.openPlayStore(MainActivity.this, getPackageName());
                                break;
                            case 4:
                                startActivity(new Intent(MainActivity.this, ChromerIntro.class));
                                break;
                            case 5:
                                launchCustomTab(Constants.CUSTOM_TAB_URL);
                                break;
                            case 6:
                                startActivity(new Intent(MainActivity.this, DonateActivity.class));
                                break;
                            case 7:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                                shareIntent.setType("text/plain");
                                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                                break;
                            case 8:
                                Intent aboutActivityIntent = new Intent(MainActivity.this, AboutAppActivity.class);
                                startActivity(aboutActivityIntent,
                                        ActivityOptions.makeCustomAnimation(MainActivity.this,
                                                R.anim.slide_in_right_medium,
                                                R.anim.slide_out_left_medium).toBundle()
                                );
                                break;
                            case 9:
                                showJoinBetaDialog();
                                break;
                            case 10:
                                startActivity(new Intent(MainActivity.this, WebHeadsIntro.class));
                                break;
                        }
                        return false;
                    }
                })
                .withDelayDrawerClickEvent(200)
                .build();
        mDrawer.setSelection(-1);
    }

    private void showJoinBetaDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.join_beta)
                .content(R.string.join_beta_content)
                .btnStackedGravity(GravityEnum.END)
                .forceStacking(true)
                .positiveText(R.string.join_google_plus)
                .neutralText(R.string.become_a_tester)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.G_COMMUNITY_URL));
                        startActivity(googleIntent);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchCustomTab(Constants.APP_TESTING_URL);
                    }
                })
                .build()
                .show();
    }

    private void launchCustomTab(@Nullable String url) {
        if (url != null) {
            if (Preferences.webHeads(this)) {
                final Intent webHeadService = new Intent(this, WebHeadService.class);
                webHeadService.setData(Uri.parse(url));
                startService(webHeadService);
            } else {
                Benchmark.start("Custom tab launching");
                CustomTabs.from(this)
                        .forUrl(url)
                        .withSession(mCustomTabManager.getSession())
                        .prepare()
                        .launch();
                Benchmark.end();
            }
        }
    }

    private void refreshCustomTabBindings() {
        // Unbind from currently bound service
        mCustomTabManager.unbindCustomTabsService(this);
        setupCustomTab();
        mCustomTabManager.bindCustomTabsService(this);

        // Restarting services will make them update their bindings.
        ServiceUtil.refreshCustomTabBindings(getApplicationContext());
    }

    private void setupCustomTab() {
        mCustomTabManager = new CustomTabManager();
        mCustomTabManager.setConnectionCallback(
                new CustomTabManager.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Timber.d("Connected to custom tabs");
                        try {
                            mCustomTabManager.mayLaunchUrl(Uri.parse(Constants.GOOGLE_URL), null, null);
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
        switch (dialog.getTitle()) {
            case R.string.default_toolbar_color:
                Intent toolbarColorIntent = new Intent(Constants.ACTION_TOOLBAR_COLOR_SET);
                toolbarColorIntent.putExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, selectedColor);
                LocalBroadcastManager.getInstance(this).sendBroadcast(toolbarColorIntent);
                break;
            case R.string.web_heads_color:
                Intent webHeadColorIntent = new Intent(Constants.ACTION_WEBHEAD_COLOR_SET);
                webHeadColorIntent.putExtra(Constants.EXTRA_KEY_WEBHEAD_COLOR, selectedColor);
                LocalBroadcastManager.getInstance(this).sendBroadcast(webHeadColorIntent);
                break;
        }
    }

    private void checkAndEducateUser(boolean forceShow) {
        final List packages;
        if (!forceShow) {
            packages = CustomTabs.getCustomTabSupportingPackages(this);
        } else {
            packages = Collections.EMPTY_LIST;
        }
        if (packages.size() == 0 || forceShow) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.custom_tab_provider_not_found))
                    .content(getString(R.string.custom_tab_provider_not_found_expln))
                    .positiveText(getString(R.string.install))
                    .negativeText(getString(android.R.string.no))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            Utils.openPlayStore(MainActivity.this, Constants.CHROME_PACKAGE);
                        }
                    }).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mMaterialSearchView.hasFocus()) {
            mMaterialSearchView.clearFocus();
            return;
        }

        if (mBottomSheet.isSheetShowing()) {
            mBottomSheet.dismissSheet();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_VOICE) {
            switch (resultCode) {
                case RESULT_OK:
                    final List<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultList != null && !resultList.isEmpty()) {
                        launchCustomTab(Utils.getSearchUrl(resultList.get(0)));
                    }
                    break;
                default:
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        if (mMaterialSearchView.hasFocus() && mMaterialSearchView.getText().length() > 0) {
            launchCustomTab(mMaterialSearchView.getURL());
        } else
            launchCustomTab(Constants.GOOGLE_URL);
    }

    private void showPicker(final IntentPickerSheetView browserPicker) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBottomSheet.showWithSheetView(browserPicker);
            }
        }, 150);
    }


    @Override
    public void onDefaultCustomTabProviderClick(final AppPreferenceCardView customTabPreferenceCard) {
        final List<IntentPickerSheetView.ActivityInfo> customTabApps = Utils.getCustomTabActivityInfos(this);
        if (customTabApps.isEmpty()) {
            checkAndEducateUser(true);
            return;
        }

        final IntentPickerSheetView customTabPicker = new IntentPickerSheetView(this,
                Constants.DUMMY_INTENT,
                R.string.default_provider,
                new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                        mBottomSheet.dismissSheet();
                        customTabPreferenceCard.updatePreference(activityInfo.componentName);
                        refreshCustomTabBindings();
                        snack(String.format(getString(R.string.default_provider_success), activityInfo.label));
                    }
                });
        customTabPicker.setFilter(IntentPickerSheetView.selfPackageExcludeFilter(this));
        customTabPicker.setMixins(customTabApps);
        showPicker(customTabPicker);
    }

    @Override
    public void onSecondaryBrowserClick(final AppPreferenceCardView browserPreferenceCard) {
        final IntentPickerSheetView browserPicker = new IntentPickerSheetView(this,
                Constants.WEB_INTENT,
                R.string.choose_secondary_browser,
                new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                        mBottomSheet.dismissSheet();
                        browserPreferenceCard.updatePreference(activityInfo.componentName);
                        snack(String.format(getString(R.string.secondary_browser_success), activityInfo.label));
                    }
                });
        browserPicker.setFilter(IntentPickerSheetView.selfPackageExcludeFilter(this));
        showPicker(browserPicker);
    }

    @Override
    public void onFavoriteShareAppClick(final AppPreferenceCardView favShareAppPreferenceCard) {
        final IntentPickerSheetView favSharePicker = new IntentPickerSheetView(this,
                Constants.TEXT_SHARE_INTENT,
                R.string.choose_fav_share_app,
                new IntentPickerSheetView.OnIntentPickedListener() {
                    @Override
                    public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                        mBottomSheet.dismissSheet();
                        favShareAppPreferenceCard.updatePreference(activityInfo.componentName);
                        snack(String.format(getString(R.string.fav_share_success), activityInfo.label));
                    }
                });
        favSharePicker.setFilter(IntentPickerSheetView.selfPackageExcludeFilter(this));
        showPicker(favSharePicker);
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private final String[] mSections = new String[]{
                getString(R.string.options),
                getString(R.string.web_heads),
                getString(R.string.customize)
        };

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return OptionsFragment.newInstance();
                case 1:
                    return WebHeadsFragment.newInstance();
                case 2:
                    return CustomizeFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mSections.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSections[position];
        }

    }
}
