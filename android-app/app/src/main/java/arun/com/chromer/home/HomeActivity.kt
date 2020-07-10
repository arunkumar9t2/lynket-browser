/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import arun.com.chromer.R
import arun.com.chromer.about.changelog.Changelog
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.extenstions.watch
import arun.com.chromer.home.bottomsheet.HomeBottomSheet
import arun.com.chromer.home.epoxycontroller.HomeFeedController
import arun.com.chromer.intro.ChromerIntroActivity
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.SettingsGroupActivity
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.tips.TipsActivity
import arun.com.chromer.util.RxEventBus
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dev.arunkumar.android.dagger.viewmodel.UsesViewModel
import dev.arunkumar.android.dagger.viewmodel.ViewModelKey
import dev.arunkumar.android.dagger.viewmodel.viewModel
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@SuppressLint("CheckResult")
class HomeActivity : BaseActivity(), Snackable, UsesViewModel {
  @Inject
  lateinit var tabsManager: TabsManager

  @Inject
  lateinit var rxEventBus: RxEventBus

  @Inject
  lateinit var tabsManger: TabsManager

  @Inject
  override lateinit var viewModelFactory: ViewModelProvider.Factory
  private val homeActivityViewModel by viewModel<HomeActivityViewModel>()

  override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

  override fun getLayoutRes() = R.layout.activity_main

  @Inject
  lateinit var homeFeedController: HomeFeedController

  @Inject
  lateinit var tabsLifecycleObserver: TabsLifecycleObserver

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme_NoActionBar)
    super.onCreate(savedInstanceState)

    if (Preferences.get(this).isFirstRun) {
      startActivity(Intent(this, ChromerIntroActivity::class.java))
    }

    Changelog.conditionalShow(this)

    setupToolbar()
    setupSearchBar()
    setupFeed()
    setupEventListeners()
  }

  override fun snack(textToSnack: String) {
    Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_SHORT).show()
  }

  override fun snackLong(textToSnack: String) {
    Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_LONG).show()
  }

  private fun setupToolbar() {
    tipsIcon.setImageDrawable(IconicsDrawable(this)
        .icon(CommunityMaterial.Icon.cmd_lightbulb_on)
        .colorRes(R.color.md_yellow_700)
        .sizeDp(24))
  }

  private fun setupEventListeners() {
    subs.add(rxEventBus.filteredEvents<TabsManager.FinishRoot>().subscribe { finish() })
    settingsIcon.setOnClickListener {
      startActivity(Intent(this, SettingsGroupActivity::class.java))
    }
    tipsIcon.setOnClickListener {
      startActivity(Intent(this, TipsActivity::class.java))
    }
  }

  override fun onStart() {
    super.onStart()
    tabsLifecycleObserver.activeTabs().subscribe { tabs ->
      homeFeedController.tabs = tabs
    }
  }

  private fun setupFeed() {
    homeFeedRecyclerView.apply {
      setController(homeFeedController)
      (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }
    val owner = this
    homeActivityViewModel.run {
      providerInfoLiveData.watch(owner) { providerInfo ->
        homeFeedController.customTabProviderInfo = providerInfo
      }
      recentsLiveData.watch(owner) { recentWebsites ->
        homeFeedController.recentWebSites = recentWebsites
      }
    }
  }

  private fun setupSearchBar() {
    materialSearchView.apply {
      // Handle voice item failed
      voiceSearchFailed()
          .takeUntil(lifecycleEvents.destroys)
          .subscribe {
            snack(getString(R.string.no_voice_rec_apps))
          }

      // Handle search events
      searchPerforms()
          .takeUntil(lifecycleEvents.destroys)
          .subscribe { url ->
            postDelayed(150) {
              tabsManger.openUrl(this@HomeActivity, Website(url))
            }
          }

      // No focus initially
      clearFocus()

      // Handle focus changes
      focusChanges()
          .takeUntil(lifecycleEvents.destroys)
          .subscribe { hasFocus ->
            if (hasFocus) {
              shadowView.show()
            } else {
              shadowView.gone()
            }
          }

      // Menu clicks
      menuClicks()
          .takeUntil(lifecycleEvents.destroys)
          .subscribe {
            HomeBottomSheet().show(supportFragmentManager, "home-bottom-shher")
          }
    }
  }

  override fun onBackPressed() {
    if (materialSearchView.hasFocus()) {
      materialSearchView.clearFocus()
      return
    }
    super.onBackPressed()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    materialSearchView.onActivityResult(requestCode, resultCode, data)
  }

  @Module
  abstract class HomeBuilder {
    @Binds
    @IntoMap
    @ViewModelKey(HomeActivityViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeActivityViewModel): ViewModel
  }
}
