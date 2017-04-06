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

package arun.com.chromer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import java.util.Collections;
import java.util.List;

import arun.com.chromer.activities.SnackHelper;
import arun.com.chromer.activities.about.AboutAppActivity;
import arun.com.chromer.activities.about.changelog.Changelog;
import arun.com.chromer.activities.intro.ChromerIntro;
import arun.com.chromer.activities.intro.WebHeadsIntro;
import arun.com.chromer.activities.main.home.HomeFragment;
import arun.com.chromer.activities.payments.DonateActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.SettingsGroupActivity;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;
import arun.com.chromer.util.cache.FontCache;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.ACTION_CLOSE_ROOT;
import static arun.com.chromer.util.cache.FontCache.MONO;

public class MainActivity extends AppCompatActivity implements SnackHelper {

    @BindView(R.id.bottomsheet)
    public BottomSheetLayout bottomSheetLayout;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.coordinator_layout)
    public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;

    private BroadcastReceiver closeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Preferences.get(this).isFirstRun()) {
            startActivity(new Intent(this, ChromerIntro.class));
        }

        Changelog.conditionalShow(this);

        setupDrawer();
        checkAndEducateUser(false);
        ServiceUtil.takeCareOfServices(getApplicationContext());
        registerCloseReceiver();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
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

    @Override
    public void snack(@NonNull String textToSnack) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void snackLong(@NonNull String textToSnack) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_LONG).show();
    }


    private void setupDrawer() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        collapsingToolbar.setTitle(getString(R.string.app_name));
        collapsingToolbar.setCollapsedTitleTypeface(FontCache.get(MONO, this));
        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
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
                            Answers.getInstance().logCustom(new CustomEvent("Send feedback"));
                            break;
                        case 3:
                            Utils.openPlayStore(MainActivity.this, getPackageName());
                            Answers.getInstance().logCustom(new CustomEvent("Rate on Play Store"));
                            break;
                        case 4:
                            startActivity(new Intent(MainActivity.this, ChromerIntro.class));
                            break;
                        case 6:
                            startActivity(new Intent(MainActivity.this, DonateActivity.class));
                            Answers.getInstance().logCustom(new CustomEvent("Donate clicked"));
                            break;
                        case 7:
                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                            shareIntent.setType("text/plain");
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                            Answers.getInstance().logShare(new ShareEvent());
                            break;
                        case 8:
                            startActivity(new Intent(MainActivity.this, AboutAppActivity.class));
                            break;
                        case 9:
                            showJoinBetaDialog();
                            Answers.getInstance().logCustom(new CustomEvent("Join Beta"));
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
                // .onNeutral((dialog, which) -> launchCustomTab(Constants.APP_TESTING_URL))
                .build()
                .show();
    }

    private void checkAndEducateUser(boolean forceShow) {
        final List packages;
        if (!forceShow) {
            packages = CustomTabs.getCustomTabSupportingPackages(this);
        } else {
            packages = Collections.EMPTY_LIST;
        }
        if (packages.size() == 0 || forceShow) {
            Answers.getInstance().logCustom(new CustomEvent("Missing provider"));
            new MaterialDialog.Builder(this)
                    .title(R.string.custom_tab_provider_not_found)
                    .content(Utils.html(this, R.string.custom_tab_provider_not_found_dialog_content))
                    .positiveText(R.string.install)
                    .negativeText(android.R.string.no)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        Utils.openPlayStore(MainActivity.this, Constants.CHROME_PACKAGE);
                        Answers.getInstance().logCustom(new CustomEvent("Install Chrome Clicked"));
                    }).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet();
            return;
        }
        super.onBackPressed();
    }
}
