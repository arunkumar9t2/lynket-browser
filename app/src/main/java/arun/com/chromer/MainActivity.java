package arun.com.chromer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import java.util.Collections;
import java.util.List;

import arun.com.chromer.activities.about.AboutAppActivity;
import arun.com.chromer.activities.about.changelog.Changelog;
import arun.com.chromer.activities.history.HistoryActivity;
import arun.com.chromer.activities.intro.ChromerIntro;
import arun.com.chromer.activities.intro.WebHeadsIntro;
import arun.com.chromer.activities.payments.DonateActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.SettingsGroupActivity;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.search.SuggestionItem;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;
import arun.com.chromer.util.cache.FontCache;
import arun.com.chromer.views.searchview.MaterialSearchView;
import arun.com.chromer.webheads.WebHeadService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.ACTION_CLOSE_ROOT;
import static arun.com.chromer.shared.Constants.REQUEST_CODE_VOICE;

public class MainActivity extends AppCompatActivity implements Home.View {

    @BindView(R.id.bottomsheet)
    public BottomSheetLayout bottomSheetLayout;
    @BindView(R.id.material_search_view)
    public MaterialSearchView materialSearchView;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.coordinator_layout)
    public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.chromer)
    TextView chromer;
    @BindView(R.id.incognito_mode)
    CheckBox incognitoMode;
    @BindView(R.id.recents_list)
    RecyclerView recentsList;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.root)
    LinearLayout root;

    private CustomTabManager customTabManager;
    private Drawer drawer;
    private BroadcastReceiver closeReceiver;

    private RecentsAdapter recentsAdapter;

    private Home.Presenter presenter;

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        presenter = new Home.Presenter(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        chromer.setTypeface(FontCache.get(FontCache.MONO, this));

        if (Preferences.get(this).isFirstRun()) {
            startActivity(new Intent(this, ChromerIntro.class));
        }

        Changelog.conditionalShow(this);

        setupMaterialSearch();
        setupDrawer();
        setupCustomTab();
        setupRecents();

        checkAndEducateUser(false);

        ServiceUtil.takeCareOfServices(getApplicationContext());
        registerCloseReceiver();
    }

    private void setupRecents() {
        recentsList.setLayoutManager(new GridLayoutManager(this, 4));
        recentsAdapter = new RecentsAdapter();
        recentsList.setAdapter(recentsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabManager.bindCustomTabsService(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        invalidateState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabManager.unbindCustomTabsService(this);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
        subscriptions.clear();
        presenter.cleanUp();
        presenter = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this, SettingsGroupActivity.class));
                break;
        }
        return true;
    }

    private void invalidateState() {
        incognitoMode.setChecked(Preferences.get(this).incognitoMode());
        incognitoMode.setCompoundDrawablePadding(Utils.dpToPx(5));
        incognitoMode.setCompoundDrawables(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_incognito)
                .colorRes(R.color.material_dark_color)
                .sizeDp(24), null, null, null);
        incognitoMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                Preferences.get(getApplicationContext()).incognitoMode(isChecked));

        presenter.loadRecents(this);
    }


    private void registerCloseReceiver() {
        closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Finished from receiver");
                MainActivity.this.finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(closeReceiver, new IntentFilter(ACTION_CLOSE_ROOT));
    }

    private void setupMaterialSearch() {
        materialSearchView.clearFocus();
        presenter.registerSearch(materialSearchView.getEditText());
        materialSearchView.setInteractionListener(new MaterialSearchView.SearchViewInteractionListener() {
            @Override
            public void onVoiceIconClick() {
                if (Utils.isVoiceRecognizerPresent(getApplicationContext())) {
                    startActivityForResult(Utils.getRecognizerIntent(MainActivity.this), REQUEST_CODE_VOICE);
                } else {
                    snack(getString(R.string.no_voice_rec_apps));
                }
            }

            @Override
            public void onSearchPerformed(@NonNull String url) {
                launchCustomTab(url);
            }

            @Override
            public void onHamburgerClick() {
                if (drawer.isDrawerOpen()) {
                    drawer.closeDrawer();
                } else {
                    drawer.openDrawer();
                }
            }
        });

    }

    @Override
    public void setSuggestions(@NonNull List<SuggestionItem> suggestions) {
        materialSearchView.setSuggestions(suggestions);
    }

    @Override
    public void setRecents(@NonNull List<WebSite> webSites) {
        recentsAdapter.setWebSites(webSites);
    }

    @Override
    public void snack(@NonNull String textToSnack) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void snackLong(@NonNull String textToSnack) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_LONG).show();
    }


    private void setupDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.chromer)
                        .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        .withDividerBelowHeader(true)
                        .build())
                .addStickyDrawerItems()
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
                ).withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem == null)
                        return false;
                    int i = (int) drawerItem.getIdentifier();
                    switch (i) {
                        case 2:
                            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", Constants.MAILID, null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
                            break;
                        case 3:
                            Utils.openPlayStore(MainActivity.this, getPackageName());
                            break;
                        case 4:
                            startActivity(new Intent(MainActivity.this, ChromerIntro.class));
                            break;
                        case 6:
                            startActivity(new Intent(MainActivity.this, DonateActivity.class));
                            break;
                        case 7:
                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                            shareIntent.setType("text/plain");
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                            break;
                        case 8:
                            startActivity(new Intent(MainActivity.this, AboutAppActivity.class));
                            break;
                        case 9:
                            showJoinBetaDialog();
                            break;
                        case 10:
                            startActivity(new Intent(MainActivity.this, WebHeadsIntro.class));
                            break;
                    }
                    return false;
                })
                .withDelayDrawerClickEvent(200)
                .build();
        drawer.setSelection(-1);
    }

    private void showJoinBetaDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.join_beta)
                .content(R.string.join_beta_content)
                .btnStackedGravity(GravityEnum.END)
                .stackingBehavior(StackingBehavior.ALWAYS)
                .positiveText(R.string.join_google_plus)
                .neutralText(R.string.become_a_tester)
                .onPositive((dialog, which) -> {
                    final Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.G_COMMUNITY_URL));
                    startActivity(googleIntent);
                })
                .onNeutral((dialog, which) -> launchCustomTab(Constants.APP_TESTING_URL))
                .build()
                .show();
    }

    private void launchCustomTab(@Nullable String url) {
        if (url != null) {
            if (Preferences.get(this).webHeads()) {
                final Intent webHeadService = new Intent(this, WebHeadService.class);
                webHeadService.setData(Uri.parse(url));
                startService(webHeadService);
            } else {
                CustomTabs.from(this)
                        .forUrl(url)
                        .withSession(customTabManager.getSession())
                        .prepare()
                        .launch();
            }
        }
    }

    private void setupCustomTab() {
        customTabManager = new CustomTabManager();
        customTabManager.setConnectionCallback(
                new CustomTabManager.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Timber.d("Connected to custom tabs");
                        try {
                            customTabManager.mayLaunchUrl(Uri.parse(Constants.GOOGLE_URL), null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                    }
                });
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
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        Utils.openPlayStore(MainActivity.this, Constants.CHROME_PACKAGE);
                    }).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (materialSearchView.hasFocus()) {
            materialSearchView.clearFocus();
            return;
        }

        if (bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VOICE) {
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
        if (materialSearchView.hasFocus() && materialSearchView.getText().length() > 0) {
            launchCustomTab(materialSearchView.getURL());
        } else {
            launchCustomTab(Constants.GOOGLE_URL);
        }
    }

    @OnClick(R.id.history)
    public void onClick() {
        startActivity(new Intent(this, HistoryActivity.class));
    }
}
