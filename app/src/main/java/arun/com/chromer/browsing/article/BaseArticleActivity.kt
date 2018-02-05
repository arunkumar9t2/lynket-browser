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

package arun.com.chromer.browsing.article

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.article.adapter.ArticleAdapter
import arun.com.chromer.browsing.article.util.ArticleScrollListener
import arun.com.chromer.browsing.article.util.ArticleUtil.changeProgressBarColors
import arun.com.chromer.browsing.article.util.ArticleUtil.changeRecyclerOverscrollColors
import arun.com.chromer.browsing.article.view.ElasticDragDismissFrameLayout
import arun.com.chromer.browsing.menu.MenuDelegate
import arun.com.chromer.data.Result
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.setMenuBackgroundColor
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.Preferences.*
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.Constants.EXTRA_KEY_TOOLBAR_COLOR
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import it.sephiroth.android.library.bottomnavigation.BottomNavigation
import kotlinx.android.synthetic.main.activity_article_mode.*
import javax.inject.Inject

abstract class BaseArticleActivity : BrowsingActivity() {
    private lateinit var browsingArticleViewModel: BrowsingArticleViewModel

    private var url: String? = null

    private var primaryColor: Int = 0
    private var accentColor: Int = 0

    private lateinit var articleAdapter: ArticleAdapter

    private var articleScrollListener: ArticleScrollListener? = null

    @Inject
    lateinit var tabsManager: DefaultTabsManager
    @Inject
    lateinit var menuDelegate: MenuDelegate

    override fun getLayoutRes(): Int {
        return R.layout.activity_article_mode
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readCustomizations()
        url = intent.dataString

        setupToolbar()
        setupCloseListeners()
        setupBottombar()
        setupTheme()

        articleScrollListener = ArticleScrollListener(toolbar, statusBar, primaryColor)
        recyclerView.addOnScrollListener(articleScrollListener)
    }

    private fun setupCloseListeners() {
        transparentSide1.setOnClickListener { finish() }
        transparentSide2.setOnClickListener { finish() }
        dragDismissLayout.addListener(
                object : ElasticDragDismissFrameLayout.ElasticDragDismissCallback() {
                    override fun onDragDismissed() {
                        finish()
                    }
                })
    }


    private fun setupBottombar() {
        bottomNavigation.setMenuBackgroundColor(ContextCompat.getColor(this, R.color.article_windowBackground))

        if (preferences.bottomBar()) {
            bottomNavigation.apply {
                setSelectedIndex(-1, false)
                setOnMenuItemClickListener(object : BottomNavigation.OnMenuItemSelectionListener {
                    override fun onMenuItemSelect(itemId: Int, position: Int, fromUser: Boolean) {
                        menuDelegate.handleItemSelected(itemId)
                        post { setSelectedIndex(-1, false) }
                    }

                    override fun onMenuItemReselect(itemId: Int, position: Int, fromUser: Boolean) {
                    }
                })
            }
        } else {
            bottomNavigation.gone()
        }
    }

    private fun setupTheme() {
        val theme = preferences.articleTheme()
        if (theme == Preferences.THEME_BLACK) {
            coordinatorLayout.background = ColorDrawable(Color.BLACK)
            bottomNavigation.setMenuBackgroundColor(Color.BLACK)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        browsingArticleViewModel = ViewModelProviders.of(this, viewModelFactory).get(BrowsingArticleViewModel::class.java)
        subs.add(browsingArticleViewModel.loadWebSiteDetails(intent.dataString)
                .subscribe { result ->
                    when (result) {
                        is Result.Success -> {
                            val webArticle: WebArticle? = result.data
                            if (webArticle == null) {
                                onArticleLoadingFailed(null)
                            } else {
                                onArticleLoaded(webArticle)
                            }
                        }
                        is Result.Failure -> onArticleLoadingFailed(result.throwable)
                    }
                })
    }


    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.article_ic_close)
        supportActionBar?.title = null
    }

    override fun onWebsiteLoaded(website: Website) {
        if (website.themeColor() != Constants.NO_COLOR) {
            setPrimaryColor(website.themeColor())
        }
    }

    private fun setPrimaryColor(@ColorInt primaryColor: Int) {
        if (preferences.dynamiceToolbarEnabledAndWebEnabled()) {
            this.primaryColor = primaryColor
            changeRecyclerOverscrollColors(recyclerView, primaryColor)
            changeProgressBarColors(progressBar, primaryColor)
            articleScrollListener?.setPrimaryColor(primaryColor)
        }
    }

    private fun readCustomizations() {
        val appPrimaryColor = preferences.toolbarColor()
        primaryColor = intent.getIntExtra(EXTRA_KEY_TOOLBAR_COLOR, appPrimaryColor)
        accentColor = ContextCompat.getColor(this, R.color.accent)

        if (preferences.dynamiceToolbarEnabledAndWebEnabled()) {
            accentColor = primaryColor
        }

        val theme = preferences.articleTheme()
        when (theme) {
            THEME_LIGHT -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK, THEME_BLACK -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        }
    }

    private fun renderArticle(webArticle: WebArticle) {
        articleAdapter = ArticleAdapter(
                webArticle,
                accentColor,
                GlideApp.with(this)
        ).apply {
            setElements(webArticle.elements)
            subs.add(keywordsClicks().map { Utils.getSearchUrl(it) }.subscribe {
                tabsManager.openUrl(this@BaseArticleActivity, Website(it))
            })
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BaseArticleActivity)
            adapter = articleAdapter
        }
        hideLoading()
    }

    @CallSuper
    protected open fun onArticleLoadingFailed(throwable: Throwable?) {
        hideLoading()
    }

    protected open fun onArticleLoaded(webArticle: WebArticle) {
        if (webArticle.elements != null && webArticle.elements.size >= MIN_NUM_ELEMENTS) {
            renderArticle(webArticle)
        } else {
            onArticleLoadingFailed(null)
        }
    }

    override fun onToolbarColorSet(websiteThemeColor: Int) {
    }

    private fun hideLoading() {
        progressBar.gone()
    }

    companion object {
        private const val MIN_NUM_ELEMENTS = 1
    }
}
