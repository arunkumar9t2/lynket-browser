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

import android.content.Intent
import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.TransitionManager.beginDelayedTransition
import android.support.v7.widget.GridLayoutManager
import android.view.View
import arun.com.chromer.R
import arun.com.chromer.browsing.customtabs.CustomTabs
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.extenstions.appName
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.visible
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.fragment.BaseMVPFragment
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

/**
 * Created by Arunkumar on 07-04-2017.
 */
class HomeFragment : BaseMVPFragment<HomeFragmentContract.View, HomeFragmentContract.Presenter>(), HomeFragmentContract.View {
    @Inject
    lateinit var recentsAdapter: RecentsAdapter
    @Inject
    lateinit var homeFragmentContractPresenter: HomeFragmentContract.Presenter
    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var tabsManger: DefaultTabsManager

    override fun createPresenter(): HomeFragmentContract.Presenter {
        return homeFragmentContractPresenter
    }

    override fun inject(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMaterialSearch()
        setupRecents()
        setupInfoCards()
        setupEventListeners()
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            invalidateState()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity?.setTitle(R.string.app_name)
            invalidateState()
        }
    }

    override fun snack(message: String) {
        (activity as Snackable).snack(message)
    }

    override fun snackLong(message: String) {
        (activity as Snackable).snackLong(message)
    }

    override fun setRecents(websites: List<Website>) {
        recentsAdapter.setWebsites(websites)
        if (websites.isEmpty()) {
            recent_missing_text.visible()
        } else {
            recent_missing_text.gone()
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_home
    }

    private fun setupMaterialSearch() {
        material_search_view.apply {
            subs.add(voiceSearchFailed().subscribe {
                snack(getString(R.string.no_voice_rec_apps))
            })
            subs.add(searchPerforms().subscribe { url ->
                postDelayed({ launchCustomTab(url) }, 150)
            })
            clearFocus()
            subs.add(focusChanges().subscribe {
                beginDelayedTransition(fragmentHome, Fade().addTarget(shadow_layout))
                if (it) {
                    shadow_layout.visible()
                } else {
                    shadow_layout.gone()
                }
            })
        }
    }

    private fun invalidateState() {
        incognito_mode.apply {
            isChecked = Preferences.get(context!!).incognitoMode()
            compoundDrawablePadding = Utils.dpToPx(5.0)
            setCompoundDrawables(IconicsDrawable(context!!)
                    .icon(CommunityMaterial.Icon.cmd_incognito)
                    .colorRes(R.color.material_dark_color)
                    .sizeDp(18), null, null, null)
            setOnCheckedChangeListener { _, isChecked -> Preferences.get(context!!).incognitoMode(isChecked) }
        }
        homeFragmentContractPresenter.loadRecents()
        setupRecents()
    }

    private fun setupRecents() {
        recents_header_icon.setImageDrawable(IconicsDrawable(context!!)
                .icon(CommunityMaterial.Icon.cmd_history)
                .colorRes(R.color.accent)
                .sizeDp(24))

        recents_list.apply {
            layoutManager = GridLayoutManager(activity, 4)
            adapter = recentsAdapter
        }
    }


    private fun setupInfoCards() {
        val customTabProvider: String? = Preferences.get(context!!).customTabPackage()
        if (customTabProvider == null) {
            providerDescription.text = Utils.html(context!!, getString(R.string.tab_provider_status_message_home, getString(R.string.system_webview)))
            GlideApp.with(this)
                    .load(ApplicationIcon.createUri(Constants.SYSTEM_WEBVIEW))
                    .error(IconicsDrawable(context!!)
                            .icon(CommunityMaterial.Icon.cmd_web)
                            .colorRes(R.color.primary)
                            .sizeDp(24))
                    .into(providerIcon)
        } else {
            val appName = context!!.appName(customTabProvider)
            providerDescription.text = Utils.html(context!!, getString(R.string.tab_provider_status_message_home, appName))
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
        if (CustomTabs.getCustomTabSupportingPackages(context!!).isNotEmpty()) {
            startActivity(Intent(context, BrowsingOptionsActivity::class.java))
        } else {
            MaterialDialog.Builder(context!!)
                    .title(R.string.custom_tab_provider_not_found)
                    .content(Utils.html(context!!, R.string.custom_tab_provider_not_found_dialog_content))
                    .positiveText(R.string.install)
                    .negativeText(android.R.string.no)
                    .onPositive({ _, _ ->
                        Utils.openPlayStore(activity!!, Constants.CHROME_PACKAGE)
                    }).show()
        }
    }

    private fun launchCustomTab(url: String?) {
        if (url != null && activity != null) {
            tabsManger.openUrl(activity!!, Website(url))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        material_search_view.onActivityResult(requestCode, resultCode, data)
    }
}
