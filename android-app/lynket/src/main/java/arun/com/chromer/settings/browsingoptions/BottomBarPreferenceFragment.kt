/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.settings.browsingoptions

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.SwitchPreference
import arun.com.chromer.R
import arun.com.chromer.settings.Preferences.BOTTOM_BAR_ENABLED
import arun.com.chromer.settings.Preferences.MINIMIZE_BEHAVIOR_PREFERENCE
import arun.com.chromer.settings.preferences.BasePreferenceFragment
import arun.com.chromer.settings.widgets.IconListPreference
import arun.com.chromer.util.Utils
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable

class BottomBarPreferenceFragment : BasePreferenceFragment(),
  SharedPreferences.OnSharedPreferenceChangeListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.bottombar_preferences)
    setupBottomBarPreference()
    setupMinimizePreference()
  }


  private fun setupBottomBarPreference() {
    val bottomBarPreference = findPreference(BOTTOM_BAR_ENABLED) as SwitchPreference
    bottomBarPreference.icon = IconicsDrawable(requireContext())
      .icon(CommunityMaterial.Icon.cmd_more)
      .color(ContextCompat.getColor(requireContext(), R.color.material_dark_light))
      .sizeDp(24)
  }


  private fun setupMinimizePreference() {
    with(findPreference(MINIMIZE_BEHAVIOR_PREFERENCE) as IconListPreference) {
      icon = IconicsDrawable(requireContext())
        .icon(CommunityMaterial.Icon.cmd_arrow_down)
        .color(ContextCompat.getColor(requireContext(), R.color.material_dark_light))
        .sizeDp(24)
      isVisible = Utils.isLollipopAbove()
    }
    updatePreferenceSummary(MINIMIZE_BEHAVIOR_PREFERENCE)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
    updatePreferenceSummary(key)
  }

  companion object {
    fun newInstance(): BottomBarPreferenceFragment {
      return BottomBarPreferenceFragment()
    }
  }
}
