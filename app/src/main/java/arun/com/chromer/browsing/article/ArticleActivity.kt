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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.browsing.customtabs.CustomTabActivity
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.activity.ActivityComponent

class ArticleActivity : BaseArticleActivity() {
    private var baseUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseUrl = intent.dataString
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onArticleLoadingFailed(throwable: Throwable?) {
        super.onArticleLoadingFailed(throwable)
        // Loading failed, try to go back to normal url tab if it exists, else start a new normal
        // rendering tab.
        finish()
        Toast.makeText(this, R.string.article_loading_failed, Toast.LENGTH_SHORT).show()
        tabsManager.openBrowsingTab(this, Website(intent.dataString), true, false, tabsManager.browsingActivitiesName)
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

}
