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

package arun.com.chromer.browsing.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.InflateException
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.browsing.BrowsingActivity
import arun.com.chromer.browsing.menu.MenuDelegate
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.Utils
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.activity_web_view_content.*
import timber.log.Timber
import javax.inject.Inject

class WebViewActivity : BrowsingActivity() {
    @Inject
    lateinit var menuDelegate: MenuDelegate

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_web_view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupSwipeRefresh()
        setupWebView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return menuDelegate.createOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return menuDelegate.prepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuDelegate.handleItemSelected(item.itemId)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onWebsiteLoaded(website: Website) {
        val themeColor = website.themeColor()
        if (themeColor != Constants.NO_COLOR && preferences.dynamiceToolbarEnabledAndWebEnabled()) {
            setAppBarColor(themeColor)
        }

        val title = website.safeLabel()
        val subtitle = website.url
        toolbar.title = title
        if (subtitle != title) {
            toolbar.subtitle = subtitle
        }
    }

    private fun setupSwipeRefresh() {
        with(swipeRefreshLayout) {
            setOnRefreshListener {
                webView.reload()
            }
            setColorSchemeColors(
                    ContextCompat.getColor(context, R.color.primary),
                    ContextCompat.getColor(context, R.color.accent))
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.article_ic_close)
            supportActionBar!!.title = website?.safeLabel() ?: intent.dataString
        }
        val toolbarColor = intent.getIntExtra(Constants.EXTRA_KEY_TOOLBAR_COLOR, 0)
        setAppBarColor(toolbarColor)

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        try {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    showLoading()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    hideLoading()
                }
            }
            val webSettings = webView!!.settings
            webSettings.javaScriptEnabled = true
            webView.loadUrl(intent.dataString)

        } catch (e: InflateException) {
            Timber.e(e)
            Toast.makeText(this, R.string.web_view_not_found, Toast.LENGTH_LONG).show()
            finish()
        }
    }


    private fun setAppBarColor(themeColor: Int) {
        val foregroundColor = ColorUtil.getForegroundWhiteOrBlack(themeColor)
        toolbar.setBackgroundColor(themeColor)
        toolbar.setTitleTextColor(foregroundColor)
        toolbar.setSubtitleTextColor(foregroundColor)

        swipeRefreshLayout.setColorSchemeColors(themeColor, ColorUtil.getClosestAccentColor(themeColor))
        if (Utils.ANDROID_LOLLIPOP) {
            window.statusBarColor = ColorUtil.getDarkenedColorForStatusBar(themeColor)
        }
    }


    private fun showLoading() {
        swipeRefreshLayout.isRefreshing = true
    }


    private fun hideLoading() {
        swipeRefreshLayout.isRefreshing = false
    }
}
