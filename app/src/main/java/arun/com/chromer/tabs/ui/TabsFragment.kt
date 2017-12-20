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

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import arun.com.chromer.R
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.shared.base.fragment.BaseMVPFragment
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import kotlinx.android.synthetic.main.fragment_tabs.*
import rx.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by arunk on 20-12-2017.
 */
class TabsFragment : BaseMVPFragment<Tabs.View, Tabs.Presenter>(), Tabs.View {
    @Inject
    lateinit var tabsPresenter: Tabs.Presenter

    private lateinit var tabsAdapter: TabsAdapter
    private val loaderSubject: PublishSubject<Int> = PublishSubject.create()

    override fun inject(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun getLayoutRes(): Int = R.layout.fragment_tabs

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tabsAdapter = TabsAdapter(GlideApp.with(this))
        // Setup RecyclerView
        tabsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = tabsAdapter
        }

        tabsPresenter.register(loaderSubject)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity!!.setTitle(R.string.title_tabs)
            loaderSubject.onNext(0)
        }

    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            loaderSubject.onNext(0)
        }
    }

    override fun onDestroy() {
        tabsAdapter.cleanUp()
        super.onDestroy()
    }

    override fun createPresenter(): Tabs.Presenter = tabsPresenter

    override fun setTabs(tabs: List<TabsManager.Tab>) {
        tabsAdapter.setTabs(tabs)
    }

    override fun setTab(index: Int, tab: TabsManager.Tab) {
        tabsAdapter.setTabs(index, tab)
    }
}
