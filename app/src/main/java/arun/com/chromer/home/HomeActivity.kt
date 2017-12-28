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

package arun.com.chromer.home

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import arun.com.chromer.R
import arun.com.chromer.about.AboutAppActivity
import arun.com.chromer.about.changelog.Changelog
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.history.HistoryFragment
import arun.com.chromer.home.fragment.HomeFragment
import arun.com.chromer.intro.ChromerIntro
import arun.com.chromer.intro.WebHeadsIntro
import arun.com.chromer.payments.DonateActivity
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.SettingsGroupActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.Constants.APP_TESTING_URL
import arun.com.chromer.shared.Constants.G_COMMUNITY_URL
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.tabs.ui.TabsFragment
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.Utils
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.StackingBehavior
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.widget_bottom_sheet_layout.*
import javax.inject.Inject

class HomeActivity : BaseActivity(), Snackable {
    @Inject
    lateinit var tabsManager: DefaultTabsManager
    @Inject
    lateinit var rxEventBus: RxEventBus

    private var historyFragment: HistoryFragment? = null
    private var homeFragment: HomeFragment? = null
    private var tabsFragment: TabsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        if (Preferences.get(this).isFirstRun) {
            startActivity(Intent(this, ChromerIntro::class.java))
        }

        Changelog.conditionalShow(this)

        setupDrawer()

        if (savedInstanceState == null) {
            historyFragment = HistoryFragment()
            homeFragment = HomeFragment()
            tabsFragment = TabsFragment()
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, homeFragment, HomeFragment::class.java.name)
                add(R.id.fragment_container, historyFragment, HistoryFragment::class.java.name)
                if (Utils.ANDROID_LOLLIPOP) {
                    add(R.id.fragment_container, tabsFragment, TabsFragment::class.java.name)
                }
                hide(historyFragment)
                hide(tabsFragment)
                show(homeFragment)
            }.commit()
        } else {
            historyFragment = supportFragmentManager.findFragmentByTag(HistoryFragment::class.java.name) as HistoryFragment
            homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment::class.java.name) as HomeFragment
            if (Utils.ANDROID_LOLLIPOP) {
                tabsFragment = supportFragmentManager.findFragmentByTag(TabsFragment::class.java.name) as TabsFragment
            }
        }

        with(bottom_navigation) {
            selectedItemId = R.id.home
            setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> supportFragmentManager.beginTransaction()
                            .apply {
                                show(homeFragment)
                                if (Utils.ANDROID_LOLLIPOP) {
                                    hide(tabsFragment)
                                }
                                hide(historyFragment)
                            }.commit()
                    R.id.tabs -> supportFragmentManager.beginTransaction()
                            .apply {
                                if (Utils.ANDROID_LOLLIPOP) {
                                    show(tabsFragment)
                                }
                                hide(homeFragment)
                                hide(historyFragment)
                            }.commit()
                    R.id.history -> supportFragmentManager.beginTransaction()
                            .apply {
                                show(historyFragment)
                                if (Utils.ANDROID_LOLLIPOP) {
                                    hide(tabsFragment)
                                }
                                hide(homeFragment)
                            }.commit()
                }
                false
            }
            if (!Utils.ANDROID_LOLLIPOP) {
                menu.removeItem(R.id.tabs)
            }
        }

        subs.add(rxEventBus.filteredEvents(TabsManager.FinishRoot::class.java).subscribe { finish() })
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> startActivity(Intent(this, SettingsGroupActivity::class.java))
        }
        return true
    }

    override fun snack(textToSnack: String) {
        Snackbar.make(coordinator_layout, textToSnack, Snackbar.LENGTH_SHORT).show()
    }

    override fun snackLong(textToSnack: String) {
        Snackbar.make(coordinator_layout, textToSnack, Snackbar.LENGTH_LONG).show()
    }


    private fun setupDrawer() {
        setSupportActionBar(toolbar)
        with(DrawerBuilder()) {
            withActivity(this@HomeActivity)
            withToolbar(toolbar)
            withAccountHeader(
                    with(AccountHeaderBuilder()) {
                        withActivity(this@HomeActivity)
                        withHeaderBackground(R.drawable.chromer)
                        withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        withDividerBelowHeader(true)
                        build()
                    })
            addStickyDrawerItems()
            addDrawerItems(
                    PrimaryDrawerItem()
                            .withName(getString(R.string.intro))
                            .withIdentifier(4)
                            .withIcon(CommunityMaterial.Icon.cmd_clipboard_text)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.feedback))
                            .withIdentifier(2)
                            .withIcon(CommunityMaterial.Icon.cmd_message_text)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(getString(R.string.rate_play_store))
                            .withIdentifier(3)
                            .withIcon(CommunityMaterial.Icon.cmd_comment_text)
                            .withSelectable(false),
                    PrimaryDrawerItem()
                            .withName(R.string.join_beta)
                            .withIdentifier(9)
                            .withIcon(CommunityMaterial.Icon.cmd_beta)
                            .withSelectable(false),
                    DividerDrawerItem(),
                    SecondaryDrawerItem()
                            .withName(getString(R.string.share))
                            .withIcon(CommunityMaterial.Icon.cmd_share_variant)
                            .withDescription(getString(R.string.help_chromer_grow))
                            .withIdentifier(7)
                            .withSelectable(false),
                    SecondaryDrawerItem()
                            .withName(getString(R.string.support_development))
                            .withDescription(R.string.consider_donation)
                            .withIcon(CommunityMaterial.Icon.cmd_heart)
                            .withIconColorRes(R.color.accent)
                            .withIdentifier(6)
                            .withSelectable(false),
                    SecondaryDrawerItem()
                            .withName(getString(R.string.about))
                            .withIcon(CommunityMaterial.Icon.cmd_information)
                            .withIdentifier(8)
                            .withSelectable(false)
            )
            withOnDrawerItemClickListener { _, _, drawerItem ->
                val i = drawerItem.identifier.toInt()
                when (i) {
                    2 -> {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", Constants.MAILID, null))
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
                    }
                    3 -> Utils.openPlayStore(this@HomeActivity, packageName)
                    4 -> startActivity(Intent(this@HomeActivity, ChromerIntro::class.java))
                    6 -> startActivity(Intent(this@HomeActivity, DonateActivity::class.java))
                    7 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                        shareIntent.type = "text/plain"
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
                    }
                    8 -> startActivity(Intent(this@HomeActivity, AboutAppActivity::class.java))
                    9 -> showJoinBetaDialog()
                    10 -> startActivity(Intent(this@HomeActivity, WebHeadsIntro::class.java))
                }
                false
            }
            withDelayDrawerClickEvent(200)
            build()
        }.setSelection(-1)
    }

    private fun showJoinBetaDialog() {
        with(MaterialDialog.Builder(this)) {
            title(R.string.join_beta)
            content(R.string.join_beta_content)
            btnStackedGravity(GravityEnum.END)
            stackingBehavior(StackingBehavior.ALWAYS)
            positiveText(R.string.join_google_plus)
            neutralText(R.string.become_a_tester)
            onPositive { _, _ ->
                val googleIntent = Intent(ACTION_VIEW, Uri.parse(G_COMMUNITY_URL))
                startActivity(googleIntent)
            }
            onNeutral { _, _ -> tabsManager.openUrl(this@HomeActivity, Website(APP_TESTING_URL)) }
            build()
        }.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        if (bottomsheet.isSheetShowing) {
            bottomsheet.dismissSheet()
            return
        }
        super.onBackPressed()
    }
}
