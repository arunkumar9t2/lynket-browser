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

package arun.com.chromer.activities.main.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.transition.Fade
import android.support.transition.TransitionManager.beginDelayedTransition
import android.support.v7.widget.GridLayoutManager
import android.view.View
import arun.com.chromer.R
import arun.com.chromer.activities.settings.Preferences
import arun.com.chromer.customtabs.CustomTabManager
import arun.com.chromer.data.website.model.WebSite
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.visible
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.Constants.REQUEST_CODE_VOICE
import arun.com.chromer.shared.common.BaseMVPFragment
import arun.com.chromer.shared.common.Snackable
import arun.com.chromer.util.Utils
import arun.com.chromer.util.Utils.getRecognizerIntent
import arun.com.chromer.webheads.WebHeadService
import com.jakewharton.rxbinding.widget.RxTextView.afterTextChangeEvents
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Arunkumar on 07-04-2017.
 */
class HomeFragment : BaseMVPFragment<Home.View, Home.Presenter>(), Home.View {
    private lateinit var customTabManager: CustomTabManager

    @Inject
    lateinit var recentsAdapter: RecentsAdapter
    @Inject
    lateinit var homePresenter: Home.Presenter

    override fun createPresenter(): Home.Presenter {
        return homePresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMaterialSearch()
        setupCustomTab()
        setupRecents()
    }

    override fun onStart() {
        super.onStart()
        customTabManager.bindCustomTabsService(activity)
    }

    override fun inject(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        invalidateState()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity?.setTitle(R.string.app_name)
            homePresenter.loadRecents()
        }
    }

    override fun onStop() {
        super.onStop()
        customTabManager.unbindCustomTabsService(activity)
    }

    override fun snack(message: String) {
        (activity as Snackable).snack(message)
    }

    override fun snackLong(message: String) {
        (activity as Snackable).snackLong(message)
    }

    override fun setSuggestions(suggestions: List<SuggestionItem>) {
        material_search_view.setSuggestions(suggestions)
    }

    override fun setRecents(webSites: List<WebSite>) {
        recentsAdapter.setWebSites(webSites)
        if (webSites.isEmpty()) {
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
            subs.add(voiceIconClicks().subscribe {
                if (Utils.isVoiceRecognizerPresent(activity!!)) {
                    startActivityForResult(getRecognizerIntent(activity!!), REQUEST_CODE_VOICE)
                } else {
                    snack(getString(R.string.no_voice_rec_apps))
                }
            })
            subs.add(searchPerforms().subscribe { url ->
                material_search_view.postDelayed({ launchCustomTab(url) }, 150)
            })
            clearFocus()
            homePresenter.registerSearch(afterTextChangeEvents(getEditText())
                    .map { changeEvent -> changeEvent.editable()!!.toString() })
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
                    .sizeDp(24), null, null, null)
            setOnCheckedChangeListener { _, isChecked -> Preferences.get(context!!).incognitoMode(isChecked) }
        }
        homePresenter.loadRecents()
    }

    private fun setupRecents() {
        recents_header.apply {
            compoundDrawablePadding = Utils.dpToPx(22.0)
            setCompoundDrawables(IconicsDrawable(context!!)
                    .icon(CommunityMaterial.Icon.cmd_history)
                    .colorRes(R.color.accent)
                    .sizeDp(20), null, null, null)
        }
        recents_list.apply {
            layoutManager = GridLayoutManager(activity, 4)
            adapter = recentsAdapter

        }
    }

    private fun launchCustomTab(url: String?) {
        if (url != null) {
            if (Preferences.get(context!!).webHeads()) {
                if (Utils.isOverlayGranted(context!!)) {
                    val webHeadService = Intent(context, WebHeadService::class.java).apply { data = Uri.parse(url) }
                    activity?.startService(webHeadService)
                } else {
                    Utils.openDrawOverlaySettings(activity!!)
                }
            } else {
                activityComponent.customTabs().apply {
                    forUrl(url)
                    withSession(customTabManager.session)
                    launch()
                }
                homePresenter.logHistory(url)
            }
        }
    }

    private fun setupCustomTab() {
        customTabManager = CustomTabManager().apply {
            setConnectionCallback(
                    object : CustomTabManager.ConnectionCallback {
                        override fun onCustomTabsConnected() {
                            Timber.d("Connected to custom tabs")
                            try {
                                customTabManager.mayLaunchUrl(Uri.parse(Constants.GOOGLE_URL), null, null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }

                        override fun onCustomTabsDisconnected() {}
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_VOICE) {
            when (resultCode) {
                RESULT_OK -> {
                    val resultList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (resultList != null && !resultList.isEmpty()) {
                        launchCustomTab(Utils.getSearchUrl(resultList[0]))
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
