/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.home.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import arun.com.chromer.R
import arun.com.chromer.browsing.providerselection.ProviderSelectionActivity
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.extenstions.appName
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.extenstions.watch
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.tips.TipsActivity
import arun.com.chromer.util.HtmlCompat
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.OnClick
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_provider_info_card.*
import javax.inject.Inject

/**
 * Created by Arunkumar on 07-04-2017.
 */
class HomeFragment : BaseFragment(), Snackable {
  @Inject
  lateinit var recentsAdapter: RecentsAdapter

  @Inject
  lateinit var rxEventBus: RxEventBus

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var preferences: Preferences

  private lateinit var homeFragmentViewModel: HomeFragmentViewModel

  override fun inject(fragmentComponent: FragmentComponent) = fragmentComponent.inject(this)

  override val layoutRes: Int get() = R.layout.fragment_home

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupMaterialSearch()
    setupRecents()
    setupProviderCard()
    setupTipsCard()
    setupEventListeners()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    homeFragmentViewModel =
      ViewModelProviders.of(this, viewModelFactory).get(HomeFragmentViewModel::class.java)
    observeViewModel()
  }

  override fun onResume() {
    super.onResume()
    if (!isHidden) {
      loadRecents()
    }
  }

  override fun onHiddenChanged(hidden: Boolean) {
    super.onHiddenChanged(hidden)
    if (!hidden) {
      activity?.setTitle(R.string.app_name)
      loadRecents()
    }
  }

  override fun snack(message: String) {
    (activity as Snackable).snack(message)
  }

  override fun snackLong(message: String) {
    (activity as Snackable).snackLong(message)
  }

  private fun setRecents(websites: List<Website>) {
    recentsAdapter.setWebsites(websites)
    if (websites.isEmpty()) {
      recent_missing_text.show()
    } else {
      recent_missing_text.gone()
    }
  }

  private fun setupMaterialSearch() {

  }

  private fun loadRecents() {
    homeFragmentViewModel.loadRecents()
  }

  private fun setupRecents() {
    recentsHeaderIcon.setImageDrawable(
      IconicsDrawable(context!!)
        .icon(CommunityMaterial.Icon.cmd_history)
        .colorRes(R.color.accent)
        .sizeDp(24)
    )
    recentsList.apply {
      layoutManager = GridLayoutManager(activity, 4)
      adapter = recentsAdapter
    }
  }

  private fun observeViewModel() {
    homeFragmentViewModel.recentsResultLiveData.watch(this) { result ->
      when (result) {
        is Result.Loading<List<Website>> -> {
          // TODO Show progress bar.
        }
        is Result.Success<List<Website>> -> {
          setRecents(result.data!!)
        }
        else -> {
        }
      }
    }
  }


  private fun setupProviderCard() {
    if (isAdded && context != null) {
      val customTabProvider: String? = preferences.customTabPackage()
      val isIncognito = preferences.fullIncognitoMode()
      val isWebView = preferences.useWebView()
      if (customTabProvider == null || isIncognito || isWebView) {
        providerDescription.text = HtmlCompat.fromHtml(
          getString(
            R.string.tab_provider_status_message_home,
            getString(R.string.system_webview)
          )
        )
        GlideApp.with(this)
          .load(ApplicationIcon.createUri(Constants.SYSTEM_WEBVIEW))
          .error(
            IconicsDrawable(context!!)
              .icon(CommunityMaterial.Icon.cmd_web)
              .colorRes(R.color.primary)
              .sizeDp(24)
          )
          .into(providerIcon)
        if (isIncognito) {
          providerReason.show()
          providerChangeButton.gone()
        } else {
          providerReason.gone()
          providerChangeButton.show()
        }
      } else {
        providerReason.gone()
        providerChangeButton.show()
        val appName = context!!.appName(customTabProvider)
        providerDescription.text =
          HtmlCompat.fromHtml(getString(R.string.tab_provider_status_message_home, appName))
        GlideApp.with(this)
          .load(ApplicationIcon.createUri(customTabProvider))
          .into(providerIcon)
      }
    }
  }


  private fun setupTipsCard() {
    tipsIcon.setImageDrawable(
      IconicsDrawable(context!!)
        .icon(CommunityMaterial.Icon.cmd_lightbulb_on)
        .colorRes(R.color.md_yellow_700)
        .sizeDp(24)
    )
  }

  private fun setupEventListeners() {
    subs.add(rxEventBus
      .filteredEvents<BrowsingOptionsActivity.ProviderChanged>()
      .subscribe { setupProviderCard() })
  }

  @OnClick(R.id.providerChangeButton)
  fun onProviderChangeClicked() {
    Handler().postDelayed({
      startActivity(Intent(context, ProviderSelectionActivity::class.java))
    }, 200)
  }

  @OnClick(R.id.tipsButton)
  fun onTipsClicked() {
    Handler().postDelayed({
      startActivity(Intent(context, TipsActivity::class.java))
    }, 200)
  }
}
