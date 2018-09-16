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

package arun.com.chromer.settings.browsingoptions

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.preference.SwitchPreferenceCompat
import arun.com.chromer.R
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.preferences.BasePreferenceFragment
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable

class BottomBarPreferenceFragment : BasePreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.bottombar_preferences)
        // setup bottom bar preference
        setupBottomBarPreference()
    }

    private fun setupBottomBarPreference() {
        val bottomBarPreference = findPreference(Preferences.BOTTOM_BAR_ENABLED) as SwitchPreferenceCompat
        bottomBarPreference.icon = IconicsDrawable(activity!!)
                .icon(CommunityMaterial.Icon.cmd_more)
                .color(ContextCompat.getColor(activity!!, R.color.material_dark_light))
                .sizeDp(24)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

    }

    companion object {

        fun newInstance(): BottomBarPreferenceFragment {
            val fragment = BottomBarPreferenceFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
