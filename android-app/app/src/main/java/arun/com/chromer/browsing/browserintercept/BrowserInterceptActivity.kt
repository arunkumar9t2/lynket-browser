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

package arun.com.chromer.browsing.browserintercept

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.SafeIntent
import javax.inject.Inject

@SuppressLint("GoogleAppIndexingApiWarning")
class BrowserInterceptActivity : BaseActivity() {
    @Inject
    lateinit var defaultTabsManager: DefaultTabsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.let {
            val safeIntent = SafeIntent(intent)
            if (safeIntent.data == null) {
                invalidLink()
                return
            }
            defaultTabsManager.processIncomingIntent(this, intent)
        }
        finish()
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutRes() = 0

    private fun invalidLink() {
        Toast.makeText(this, getString(R.string.unsupported_link), LENGTH_SHORT).show()
        finish()
    }
}
