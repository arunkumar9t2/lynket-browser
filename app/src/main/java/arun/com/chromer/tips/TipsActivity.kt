/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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

package arun.com.chromer.tips

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.inflate
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.util.Utils
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_tips.*
import javax.inject.Inject

class TipsActivity : BaseActivity() {
    override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun getLayoutRes() = R.layout.activity_tips

    @Inject
    lateinit var requestManager: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupTipsList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setTitle(R.string.tips)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.article_ic_close)
        }
    }

    private fun setupTipsList() {
        tips_recycler_view.layoutManager = LinearLayoutManager(this)
        tips_recycler_view.setHasFixedSize(true)
        tips_recycler_view.adapter = TipsRecyclerViewAdapter()
    }

    inner class TipsRecyclerViewAdapter : RecyclerView.Adapter<TipsItemHolder>() {
        private val provider = 0
        private val secBrowser = 1
        private val perApp = 2
        private val bottomBar = 3
        private val articleKeywords = 4
        private val quicksettings = 5

        private val items = ArrayList<Int>()

        init {
            items.add(provider)
            items.add(secBrowser)
            items.add(perApp)
            if (Utils.ANDROID_LOLLIPOP) {
                items.add(bottomBar)
            }
            items.add(articleKeywords)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                items.add(quicksettings)
            }
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: TipsItemHolder, position: Int) {
            when (items[position]) {
                provider -> {
                    holder.title?.setText(R.string.choose_provider)
                    holder.subtitle?.setText(R.string.choose_provider_tip)
                    requestManager.load(R.drawable.tips_providers).into(holder.image)
                    holder.iconView?.setImageDrawable(IconicsDrawable(this@TipsActivity)
                            .icon(CommunityMaterial.Icon.cmd_cards)
                            .colorRes(R.color.accent)
                            .sizeDp(24))
                }
                secBrowser -> {
                    holder.title?.setText(R.string.choose_secondary_browser)
                    holder.subtitle?.setText(R.string.tips_secondary_browser)
                    requestManager.load(R.drawable.tip_secondary_browser).into(holder.image)
                    holder.iconView?.setImageDrawable(IconicsDrawable(this@TipsActivity)
                            .icon(CommunityMaterial.Icon.cmd_earth)
                            .colorRes(R.color.accent)
                            .sizeDp(24))
                }
                perApp -> {
                    holder.title?.setText(R.string.per_app_settings)
                    holder.subtitle?.setText(R.string.per_app_settings_explanation)
                    requestManager.load(R.drawable.tips_per_app_settings).into(holder.image)
                    holder.iconView?.setImageDrawable(IconicsDrawable(this@TipsActivity)
                            .icon(CommunityMaterial.Icon.cmd_apps)
                            .colorRes(R.color.accent)
                            .sizeDp(24))
                }
                bottomBar -> {
                    holder.title?.setText(R.string.bottom_bar)
                    holder.subtitle?.setText(R.string.tips_bottom_bar)
                    requestManager.load(R.drawable.tips_bottom_bar).into(holder.image)
                    holder.iconView?.setImageDrawable(IconicsDrawable(this@TipsActivity)
                            .icon(CommunityMaterial.Icon.cmd_drag_horizontal)
                            .colorRes(R.color.accent)
                            .sizeDp(24))
                }
                articleKeywords -> {
                    holder.title?.setText(R.string.article_mode)
                    holder.subtitle?.setText(R.string.tips_article_mode)
                    requestManager.load(R.drawable.tips_article_keywords).into(holder.image)
                    holder.iconView?.setImageDrawable(IconicsDrawable(this@TipsActivity)
                            .icon(CommunityMaterial.Icon.cmd_file_document)
                            .colorRes(R.color.accent)
                            .sizeDp(24))
                }
                quicksettings -> {
                    holder.title?.setText(R.string.quick_settings)
                    holder.subtitle?.setText(R.string.quick_settings_tip)
                    requestManager.load(R.drawable.tips_quick_settings).into(holder.image)
                    holder.iconView?.setImageDrawable(IconicsDrawable(this@TipsActivity)
                            .icon(CommunityMaterial.Icon.cmd_settings)
                            .colorRes(R.color.accent)
                            .sizeDp(24))
                }
            }
        }

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ) = TipsItemHolder(parent.inflate(R.layout.layout_tips_card))
    }

    class TipsItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.icon)
        @JvmField
        var iconView: ImageView? = null
        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null
        @BindView(R.id.subtitle)
        @JvmField
        var subtitle: TextView? = null
        @BindView(R.id.image)
        @JvmField
        var image: ImageView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
