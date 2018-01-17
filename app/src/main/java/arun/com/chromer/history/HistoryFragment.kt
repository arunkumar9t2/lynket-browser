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

package arun.com.chromer.history

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.database.Cursor
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import arun.com.chromer.R
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.util.Utils
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_history.*
import javax.inject.Inject

/**
 * Created by arunk on 07-04-2017.
 */
class HistoryFragment : BaseFragment(), Snackable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var preferences: Preferences

    private lateinit var historyAdapter: HistoryAdapter

    private var viewModel: HistoryFragmentViewModel? = null

    private val formattedMessage: CharSequence
        get() {
            val provider = preferences.customTabPackage()
            return if (provider == null) {
                getString(R.string.enable_history_subtitle)
            } else {
                Utils.html(activity!!, String.format(getString(R.string.enable_history_subtitle_custom_tab), Utils.getAppNameWithPackage(activity!!, provider)))
            }
        }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_history
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun snack(message: String) {
        (activity as Snackable).snack(message)
    }

    override fun snackLong(message: String) {
        (activity as Snackable).snackLong(message)
    }

    fun loading(loading: Boolean) {
        swipeRefreshLayout.isRefreshing = loading
    }

    private fun setCursor(cursor: Cursor?) {
        historyList.postDelayed({
            if (isAdded) {
                historyAdapter.setCursor(cursor)
                error.visibility = if (cursor == null || cursor.isClosed || cursor.count == 0) VISIBLE else GONE
            }
        }, 100)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity?.setTitle(R.string.title_history)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHistoryList()
        setupIncognitoSwitch()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HistoryFragmentViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel?.apply {
            initHistoryLoader()
            loadingLiveData.observe(this@HistoryFragment, Observer { loading(it!!) })
            historyCursorLiveData.observe(this@HistoryFragment, Observer {
                setCursor(it!!)
            })
        }
        loadHistory()
    }

    private fun setupIncognitoSwitch() {
        enableHistoryCard.setOnClickListener { incognitoSwitch.performClick() }
        incognitoSwitch.setOnCheckedChangeListener { _, isChecked -> preferences.incognitoMode(!isChecked) }
        enableHistorySubtitle.text = formattedMessage
    }

    private fun setupHistoryList() {
        val linearLayoutManager = LinearLayoutManager(activity)
        historyList.apply {
            historyList.layoutManager = linearLayoutManager
            historyAdapter = HistoryAdapter(activity!!, linearLayoutManager)
            adapter = historyAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    historyAdapter.onRangeChanged()
                }
            })
        }

        swipeRefreshLayout.apply {
            setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary), ContextCompat.getColor(context!!, R.color.accent))
            setOnRefreshListener { loadHistory() }
        }

        val swipeTouch = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel?.deleteHistory(historyAdapter.getItemAt(viewHolder.adapterPosition))
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeTouch)
        itemTouchHelper.attachToRecyclerView(historyList)
    }

    private fun loadHistory() {
        viewModel?.loadHistory()
    }

    override fun inject(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        incognitoSwitch.isChecked = !preferences.incognitoMode()
    }

    @OnClick(R.id.fab)
    fun onClearAllFabClick() {
        if (historyAdapter.itemCount != 0) {
            MaterialDialog.Builder(activity!!)
                    .title(R.string.are_you_sure)
                    .content(R.string.history_deletion_confirmation_content)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive { _, _ ->
                        viewModel?.deleteAll({ rows ->
                            if (isAdded) {
                                snack(String.format(context!!.getString(R.string.deleted_items), rows))
                            }
                        })
                    }.show()
        }
    }
}
