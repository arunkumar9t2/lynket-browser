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
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.article.util.ArticleScrollListener
import arun.com.chromer.browsing.article.util.ArticleUtil.changeProgressBarColors
import arun.com.chromer.browsing.article.util.ArticleUtil.changeRecyclerOverscrollColors
import arun.com.chromer.browsing.article.view.ElasticDragDismissFrameLayout
import arun.com.chromer.data.Result
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.gone
import arun.com.chromer.shared.Constants
import kotlinx.android.synthetic.main.activity_article_mode.*

abstract class BaseArticleActivity : BrowsingActivity() {
    private lateinit var browsingArticleViewModel: BrowsingArticleViewModel

    private var url: String? = null

    private var primaryColor: Int = 0
    private var accentColor: Int = 0

    private lateinit var articleAdapter: ArticleAdapter

    private var articleScrollListener: ArticleScrollListener? = null

    override fun getLayoutRes(): Int {
        return R.layout.activity_article_mode
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        readCustomizations()
        super.onCreate(savedInstanceState)
        url = intent.dataString

        setupToolbar()

        transparentSide1.setOnClickListener { finish() }
        transparentSide2.setOnClickListener { finish() }

        articleScrollListener = ArticleScrollListener(toolbar, statusBar, primaryColor)
        recyclerView.addOnScrollListener(articleScrollListener)
        dragDismissLayout.addListener(object : ElasticDragDismissFrameLayout.ElasticDragDismissCallback() {
            override fun onDragDismissed() {
                super.onDragDismissed()
                finish()
            }
        })
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.article_ic_close)
            supportActionBar!!.title = null
        }
    }

    override fun onWebsiteLoaded(website: Website) {
        if (website.themeColor() != Constants.NO_COLOR) {
            setPrimaryColor(website.themeColor())
        }
    }

    private fun setPrimaryColor(@ColorInt primaryColor: Int) {
        this.primaryColor = primaryColor
        changeRecyclerOverscrollColors(recyclerView, primaryColor)
        changeProgressBarColors(progressBar, primaryColor)
        articleScrollListener!!.setPrimaryColor(primaryColor)
    }

    private fun readCustomizations() {
        primaryColor = intent.getIntExtra(ArticleIntent.EXTRA_TOOLBAR_COLOR, ContextCompat.getColor(this, R.color.article_colorPrimary))
        accentColor = intent.getIntExtra(ArticleIntent.EXTRA_ACCENT_COLOR, ContextCompat.getColor(this, R.color.article_colorAccent))
        setDayNightTheme()
    }

    private fun setDayNightTheme() {
        val theme = intent.getIntExtra(ArticleIntent.EXTRA_THEME, ArticleIntent.THEME_AUTO)
        when (theme) {
            ArticleIntent.THEME_LIGHT -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ArticleIntent.THEME_DARK -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        }
    }

    private fun renderArticle(webArticle: WebArticle) {
        articleAdapter = ArticleAdapter(webArticle, accentColor).apply {
            addElements(webArticle.elements)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BaseArticleActivity)
            adapter = articleAdapter
        }
        hideLoading()
    }


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

    private fun hideLoading() {
        progressBar.gone()
    }

    companion object {
        private const val MIN_NUM_ELEMENTS = 1
    }
}
