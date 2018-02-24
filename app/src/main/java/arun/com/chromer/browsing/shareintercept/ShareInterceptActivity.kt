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

package arun.com.chromer.browsing.shareintercept

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.SafeIntent
import arun.com.chromer.util.Utils
import javax.inject.Inject

@SuppressLint("GoogleAppIndexingApiWarning")
class ShareInterceptActivity : BaseActivity() {

    @Inject
    lateinit var tabsManager: DefaultTabsManager

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val safeIntent = SafeIntent(intent)
        try {
            val action = safeIntent.action
            var text: String? = null
            when (action) {
                Intent.ACTION_SEND -> if (safeIntent.hasExtra(Intent.EXTRA_TEXT)) {
                    text = intent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.toString()
                }
                Intent.ACTION_PROCESS_TEXT -> text = safeIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
            }
            findAndOpenLink(text)
        } catch (exception: Exception) {
            invalidLink()
        } finally {
            finish()
        }
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutRes(): Int = 0


    private fun findAndOpenLink(receivedText: String?) {
        var text: String? = receivedText ?: return
        val urls = Utils.findURLs(text)
        if (!urls.isEmpty()) {
            // use only the first link
            val url = urls[0]
            openLink(url)
        } else {
            // No urls were found, so lets do a google search with the text received.
            text = Constants.G_SEARCH_URL + text!!.replace(" ", "+")
            openLink(text)
        }
    }

    private fun openLink(url: String?) {
        if (url != null) {
            tabsManager.openUrl(this, website = Website(url))
        } else invalidLink()
        finish()
    }

    private fun invalidLink() {
        Toast.makeText(this, getString(R.string.invalid_link), Toast.LENGTH_SHORT).show()
        finish()
    }
}
