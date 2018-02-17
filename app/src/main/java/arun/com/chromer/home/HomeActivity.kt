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
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.app.FragmentManager
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.about.AboutAppActivity
import arun.com.chromer.about.changelog.Changelog
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.extenstions.circularHideWithSelfCenter
import arun.com.chromer.extenstions.circularRevealWithSelfCenter
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.history.HistoryFragment
import arun.com.chromer.home.fragment.HomeFragment
import arun.com.chromer.intro.ChromerIntroActivity
import arun.com.chromer.payments.DonateActivity
import arun.com.chromer.search.view.MaterialSearchView
import arun.com.chromer.search.view.behavior.MaterialSearchViewBehavior
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.SettingsGroupActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.Constants.APP_TESTING_URL
import arun.com.chromer.shared.Constants.G_COMMUNITY_URL
import arun.com.chromer.shared.FabHandler
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.shared.behavior.FloatingActionButtonBehavior
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.tabs.ui.TabsFragment
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import butterknife.OnClick
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.StackingBehavior
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import it.sephiroth.android.library.bottomnavigation.BottomBehavior
import it.sephiroth.android.library.bottomnavigation.BottomNavigation
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class HomeActivity : BaseActivity(), Snackable {
    @Inject
    lateinit var tabsManager: DefaultTabsManager
    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var activeFragmentManagerFactory: ActiveFragmentsManager.Factory
    @Inject
    lateinit var tabsManger: DefaultTabsManager

    private lateinit var activeFragmentManager: ActiveFragmentsManager

    // Track bottom nav selection across config changes.
    private var selectedIndex: Int = HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        if (Preferences.get(this).isFirstRun) {
            startActivity(Intent(this, ChromerIntroActivity::class.java))
        }

        Changelog.conditionalShow(this)

        selectedIndex = savedInstanceState?.getInt(Companion.SELECTED_INDEX) ?: HOME

        setupToolbar()
        setupFab()
        setupSearchBar()
        setupDrawer()
        setupFragments(savedInstanceState)
        setupEventListeners()
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
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_SHORT).show()
    }

    override fun snackLong(textToSnack: String) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_LONG).show()
    }

    private fun setupEventListeners() {
        subs.add(rxEventBus.filteredEvents(TabsManager.FinishRoot::class.java).subscribe { finish() })
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        activeFragmentManager = activeFragmentManagerFactory.get(supportFragmentManager, materialSearchView, appbar, fab)
        activeFragmentManager.initialize(savedInstanceState)

        bottomNavigation.setOnMenuItemClickListener(
                object : BottomNavigation.OnMenuItemSelectionListener {
                    override fun onMenuItemSelect(itemId: Int, position: Int, fromUser: Boolean) {
                        activeFragmentManager.handleBottomMenuClick(itemId)
                        selectedIndex = position
                    }

                    override fun onMenuItemReselect(itemId: Int, position: Int, fromUser: Boolean) {
                    }
                })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(Companion.SELECTED_INDEX, bottomNavigation.selectedIndex)
        super.onSaveInstanceState(outState)
    }

    private fun setupFab() {
        (fab.layoutParams as CoordinatorLayout.LayoutParams).behavior = FloatingActionButtonBehavior()
    }

    private fun setupToolbar() {
        GlideApp.with(this).load(R.drawable.chromer_header_small).into(backdrop)
        // Hide title when expanded
        collapsingToolbar.title = " "
        appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout!!.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.title = toolbar.title
                    isShow = true
                } else if (isShow) {
                    collapsingToolbar.title = " "
                    isShow = false
                }
            }
        })
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
                    4 -> startActivity(Intent(this@HomeActivity, ChromerIntroActivity::class.java))
                    6 -> startActivity(Intent(this@HomeActivity, DonateActivity::class.java))
                    7 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                        shareIntent.type = "text/plain"
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
                    }
                    8 -> startActivity(Intent(this@HomeActivity, AboutAppActivity::class.java))
                    9 -> showJoinBetaDialog()
                }
                false
            }
            withDelayDrawerClickEvent(200)
            build()
        }.setSelection(-1)
    }

    private fun setupSearchBar() {
        materialSearchView.apply {
            // Attach a behaviour to handle scroll
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = MaterialSearchViewBehavior()
            // Handle voice item failed
            subs.add(voiceSearchFailed().subscribe {
                snack(getString(R.string.no_voice_rec_apps))
            })
            // Handle search events
            subs.add(searchPerforms().subscribe { url ->
                postDelayed({ launchCustomTab(url) }, 150)
            })
            // No focus initially
            clearFocus()
            // Handle focus changes
            subs.add(focusChanges().subscribe { hasFocus ->
                TransitionManager.beginDelayedTransition(coordinatorLayout, Fade().apply {
                    addTarget(shadowView)
                    addTarget(bottomNavigation)
                })
                handleBottomBar(hasFocus)
                if (hasFocus) {
                    shadowView.show()
                } else {
                    shadowView.gone()
                }
            })

            // Reveal the search bar with animation after layout pass
            if (selectedIndex == HOME) {
                post { materialSearchView.circularRevealWithSelfCenter() }
            }
        }
    }

    private fun launchCustomTab(url: String?) {
        if (url != null) {
            tabsManger.openUrl(this, Website(url))
        }
    }

    override fun onBackPressed() {
        if (materialSearchView.hasFocus()) {
            materialSearchView.clearFocus()
            return
        }
        super.onBackPressed()
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

    /**
     * Trigger's coordinator's layout dispatch for scrolling manually.
     */
    private fun handleBottomBar(hide: Boolean) {
        val bottomBehavior = (bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomBehavior
        bottomBehavior.onNestedFling(
                coordinatorLayout,
                bottomNavigation,
                materialSearchView,
                0f,
                if (hide) 10000f else -10000f,
                true
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        materialSearchView.onActivityResult(requestCode, resultCode, data)
    }

    @OnClick(R.id.fab)
    fun onFabClick() {
        supportFragmentManager.fragments
                ?.filter { !it.isHidden && it is FabHandler }
                ?.map { it as FabHandler }
                ?.get(0)
                ?.onFabClick()
    }

    /**
     * Since we have different available fragments based on API level, we create this class to help
     * delegate calls to correct implementation to manage active fragments.
     */
    abstract class ActiveFragmentsManager(
            private val fm: FragmentManager,
            private var materialSearchView: MaterialSearchView,
            private val fab: FloatingActionButton
    ) {
        protected var historyFragment: HistoryFragment? = null
        protected var homeFragment: HomeFragment? = null

        open fun revealSearch() {
            fab.hide()
            materialSearchView.circularRevealWithSelfCenter()
        }

        open fun hideSearch() {
            fab.show()
            materialSearchView.circularHideWithSelfCenter()
        }

        /**
         * Based on @param[savedInstance] tries to restore existing fragments that were reattached
         * or creates and attaches new instances.
         */
        open fun initialize(savedInstance: Bundle?) {
            val selectedIndex = savedInstance?.getInt(Companion.SELECTED_INDEX) ?: HOME
            if (selectedIndex > HOME) {
                fab.show()
            } else {
                fab.hide()
            }
            if (savedInstance == null) {
                historyFragment = HistoryFragment()
                homeFragment = HomeFragment()
                fm.beginTransaction().apply {
                    add(R.id.fragment_container, homeFragment, HomeFragment::class.java.name)
                    add(R.id.fragment_container, historyFragment, HistoryFragment::class.java.name)
                    show(homeFragment)
                    hide(historyFragment)
                }.commit()
            } else {
                historyFragment = fm.findFragmentByTag(HistoryFragment::class.java.name) as HistoryFragment
                homeFragment = fm.findFragmentByTag(HomeFragment::class.java.name) as HomeFragment
            }
        }

        /**
         * Responsible for handling click events from BottomNavigationMenu. Should be delegated from
         * [BottomNavigationView#setOnNavigationItemSelectedListener]
         */
        abstract fun handleBottomMenuClick(menuItemId: Int): Boolean

        @PerActivity
        class Factory @Inject constructor() {
            fun get(
                    supportFragmentManager: FragmentManager,
                    materialSearchView: MaterialSearchView,
                    appbar: AppBarLayout,
                    fab: FloatingActionButton
            ): ActiveFragmentsManager {
                return if (Utils.ANDROID_LOLLIPOP) {
                    LollipopActiveFragmentManager(supportFragmentManager, materialSearchView, fab)
                } else {
                    PreLollipopActiveFragmentManager(supportFragmentManager, materialSearchView, appbar, fab)
                }
            }
        }
    }

    /**
     * Fragment manager for Lollipop which includes the [TabsFragment]
     */
    class LollipopActiveFragmentManager(
            private var fm: FragmentManager,
            materialSearchView: MaterialSearchView,
            fab: FloatingActionButton
    ) : ActiveFragmentsManager(fm, materialSearchView, fab) {
        private var tabsFragment: TabsFragment? = null

        override fun initialize(savedInstance: Bundle?) {
            super.initialize(savedInstance)
            if (savedInstance == null) {
                tabsFragment = TabsFragment()
                fm.beginTransaction().apply {
                    add(R.id.fragment_container, tabsFragment, TabsFragment::class.java.name)
                    hide(tabsFragment)
                }.commit()
            } else {
                tabsFragment = fm.findFragmentByTag(TabsFragment::class.java.name) as TabsFragment
            }
        }

        override fun handleBottomMenuClick(menuItemId: Int): Boolean {
            when (menuItemId) {
                R.id.home -> {
                    revealSearch()
                    fm.beginTransaction().apply {
                        show(homeFragment)
                        hide(historyFragment)
                        hide(tabsFragment)
                    }.commit()
                }
                R.id.history -> {
                    hideSearch()
                    fm.beginTransaction().apply {
                        show(historyFragment)
                        hide(homeFragment)
                        hide(tabsFragment)
                    }.commit()
                }
                R.id.tabs -> {
                    hideSearch()
                    fm.beginTransaction().apply {
                        show(tabsFragment)
                        hide(homeFragment)
                        hide(historyFragment)
                    }.commit()
                }
            }
            return false
        }
    }

    /**
     * Fragment manager for pre lollip without [TabsFragment]
     */
    class PreLollipopActiveFragmentManager(
            private var fm: FragmentManager,
            materialSearchView: MaterialSearchView,
            private var appbar: AppBarLayout,
            fab: FloatingActionButton
    ) : ActiveFragmentsManager(fm, materialSearchView, fab) {

        override fun handleBottomMenuClick(menuItemId: Int): Boolean {
            when (menuItemId) {
                R.id.home -> {
                    revealSearch()
                    fm.beginTransaction().apply {
                        show(homeFragment)
                        hide(historyFragment)
                    }.commit()
                }
                R.id.history -> {
                    hideSearch()
                    fm.beginTransaction().apply {
                        show(historyFragment)
                        hide(homeFragment)
                    }.commit()
                }
                R.id.tabs -> {
                    Toast.makeText(appbar.context, R.string.feature_requires_lollipop, Toast.LENGTH_SHORT).show()
                }
            }
            return false
        }
    }

    companion object {
        private const val HOME = 0
        private const val TABS = 1
        private const val HISTORY = 2
        private const val SELECTED_INDEX = "BOTTOM_NAV_SELECTED_INDEX"
    }
}
