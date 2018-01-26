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

package arun.com.chromer.home.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.view.View
import arun.com.chromer.R
import arun.com.chromer.browsing.customtabs.CustomTabs
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.extenstions.appName
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.util.HtmlCompat
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Arunkumar on 07-04-2017.
 */
class HomeFragment : BaseFragment() {
    @Inject
    lateinit var recentsAdapter: RecentsAdapter
    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var homeFragmentViewModel: HomeFragmentViewModel

    override fun inject(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_home
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMaterialSearch()
        setupRecents()
        setupInfoCards()
        setupEventListeners()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeFragmentViewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeFragmentViewModel::class.java)
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

    fun snack(message: String) {
        (activity as Snackable).snack(message)
    }

    fun snackLong(message: String) {
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
        recentsHeaderIcon.setImageDrawable(IconicsDrawable(context!!)
                .icon(CommunityMaterial.Icon.cmd_history)
                .colorRes(R.color.accent)
                .sizeDp(24))
        recentsList.apply {
            layoutManager = GridLayoutManager(activity, 4)
            adapter = recentsAdapter
        }
    }

    private fun observeViewModel() {
        subs.add(homeFragmentViewModel
                .recentsObservable()
                .subscribe({
                    when (it) {
                        is Result.Loading<List<Website>> -> {
                            // TODO Show progress bar.
                        }
                        is Result.Success<List<Website>> -> {
                            setRecents(it.data!!)
                        }
                    }
                }, Timber::e))
    }


    private fun setupInfoCards() {
        val customTabProvider: String? = Preferences.get(context!!).customTabPackage()
        if (customTabProvider == null) {
            providerDescription.text = HtmlCompat.fromHtml(getString(R.string.tab_provider_status_message_home, getString(R.string.system_webview)))
            GlideApp.with(this)
                    .load(ApplicationIcon.createUri(Constants.SYSTEM_WEBVIEW))
                    .error(IconicsDrawable(context!!)
                            .icon(CommunityMaterial.Icon.cmd_web)
                            .colorRes(R.color.primary)
                            .sizeDp(24))
                    .into(providerIcon)
        } else {
            val appName = context!!.appName(customTabProvider)
            providerDescription.text = HtmlCompat.fromHtml(getString(R.string.tab_provider_status_message_home, appName))
            GlideApp.with(this)
                    .load(ApplicationIcon.createUri(customTabProvider))
                    .into(providerIcon)
        }
    }

    private fun setupEventListeners() {
        subs.add(rxEventBus.filteredEvents(BrowsingOptionsActivity.ProviderChanged::class.java).subscribe { setupInfoCards() })
    }

    @OnClick(R.id.providerChangeButton)
    fun onProviderChangeClicked() {
        Handler().postDelayed({
            if (CustomTabs.getCustomTabSupportingPackages(context!!).isNotEmpty()) {
                startActivity(Intent(context, BrowsingOptionsActivity::class.java))
            } else {
                MaterialDialog.Builder(context!!)
                        .title(R.string.custom_tab_provider_not_found)
                        .content(HtmlCompat.fromHtml(context!!.getString(R.string.custom_tab_provider_not_found_dialog_content)))
                        .positiveText(R.string.install)
                        .negativeText(android.R.string.no)
                        .onPositive({ _, _ ->
                            Utils.openPlayStore(activity!!, Constants.CHROME_PACKAGE)
                        }).show()
            }
        }, 200)
    }
}
