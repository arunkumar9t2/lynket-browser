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

package arun.com.chromer.tabs.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.*
import android.view.View
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.visible
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_tabs.*
import javax.inject.Inject

/**
 * Created by arunk on 20-12-2017.
 */
class TabsFragment : BaseFragment() {
    @Inject
    lateinit var tabsManager: DefaultTabsManager
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var tabsViewModel: TabsViewModel? = null

    lateinit var tabsAdapter: TabsAdapter

    override fun inject(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun getLayoutRes(): Int = R.layout.fragment_tabs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        with(swipeRefreshLayout) {
            setOnRefreshListener {
                loadTabs()
                isRefreshing = false
            }
            setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.color.colorPrimaryDarker)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tabsViewModel = ViewModelProviders.of(this, viewModelFactory).get(TabsViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        tabsViewModel?.apply {
            initializeTabsLoader()
            loadingLiveData.observe(this@TabsFragment, Observer<Boolean> { loading ->
                showLoading(loading!!)
            })
            tabsData.observe(this@TabsFragment, Observer<MutableList<TabsManager.Tab>> {
                setTabs(it!!)
            })
        }
    }

    private fun setupRecyclerView() {
        // Setup RecyclerView
        tabsAdapter = TabsAdapter(GlideApp.with(this), tabsManager)
        tabsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = tabsAdapter
        }

        val swipeTouch = object : SimpleCallback(0, LEFT or RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val tab = tabsAdapter.getTabAt(viewHolder.adapterPosition)
                activity?.let {
                    tabsManager.finishTabByUrl(activity!!, Website(tab.url), tab.getTargetActivityName())
                    loadTabs()
                }
            }
        }
        ItemTouchHelper(swipeTouch).apply { attachToRecyclerView(tabsRecyclerView) }
    }

    private fun setTabs(tabs: List<TabsManager.Tab>) {
        tabsAdapter.setTabs(tabs)
        TransitionManager.beginDelayedTransition(fragmentTabsRoot)
        if (tabs.isEmpty()) {
            error.visible()
            swipeRefreshLayout.gone()
        } else {
            error.gone()
            swipeRefreshLayout.visible()
        }
    }

    private fun showLoading(loading: Boolean) {
        swipeRefreshLayout.isRefreshing = loading
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity!!.setTitle(R.string.title_tabs)
            loadTabs()
        }

    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            loadTabs()
        }
    }

    private fun loadTabs() {
        tabsViewModel?.loadTabs()
    }

    override fun onDestroy() {
        tabsAdapter.cleanUp()
        super.onDestroy()
    }

    @OnClick(R.id.fab)
    fun onClearAllFabClick() {
        if (tabsAdapter.itemCount != 0) {
            MaterialDialog.Builder(activity!!)
                    .title(R.string.are_you_sure)
                    .content(R.string.tab_deletion_confirmation_content)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive { _, _ -> tabsViewModel?.clearAllTabs() }
                    .show()
        }
    }
}
