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

package arun.com.chromer.blacklist

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SwitchCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.util.ServiceManager
import arun.com.chromer.util.Utils
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_blacklist.*
import kotlinx.android.synthetic.main.activity_blacklist_content.*
import javax.inject.Inject

class BlacklistManagerActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener, Snackable {
    private var blacklistAdapter: BlacklistAdapter? = null

    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var blacklisAdapter: BlacklistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()

        appRecyclerView.layoutManager = LinearLayoutManager(this)
        appRecyclerView.adapter = blacklistAdapter
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }


    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        swipeRefreshLayout.apply {
            setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.color.colorPrimaryDarker)
            setOnRefreshListener {

            }
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_blacklist
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.blacklist_menu, menu)
        val menuItem = menu.findItem(R.id.blacklist_switch_item)
        if (menuItem != null) {
            val blackListSwitch = menuItem.actionView.findViewById<SwitchCompat>(R.id.blacklist_switch)
            if (blackListSwitch != null) {
                val blackListActive = preferences.blacklist() && Utils.canReadUsageStats(this)
                Preferences.get(this).blacklist(blackListActive)
                blackListSwitch.isChecked = Preferences.get(this).blacklist()
                blackListSwitch.setOnCheckedChangeListener(this)
            }
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun requestUsagePermission() {
        MaterialDialog.Builder(this)
                .title(R.string.permission_required)
                .content(R.string.usage_permission_explanation_blacklist)
                .positiveText(R.string.grant)
                .onPositive { _, _ -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (isChecked && !Utils.canReadUsageStats(applicationContext)) {
            buttonView.isChecked = false
            requestUsagePermission()
        } else {
            snack(if (isChecked) getString(R.string.blacklist_on) else getString(R.string.blacklist_off))
            Preferences.get(this).blacklist(isChecked)
            ServiceManager.takeCareOfServices(applicationContext)
        }
    }

    override fun snack(message: String) {
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun snackLong(message: String) {
        Snackbar.make(coordinatorLayout!!, message, Snackbar.LENGTH_LONG).show()
    }
}
