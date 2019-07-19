/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.browsing.amp.qs

import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import arun.com.chromer.R
import arun.com.chromer.shared.base.PreferenceQuickSettingsTile

@RequiresApi(api = Build.VERSION_CODES.N)
class AmpTile : PreferenceQuickSettingsTile() {
    override fun togglePreference() = preferences.ampMode(!preferences.ampMode())

    override fun activeLabel(): String = label()

    override fun inActiveIcon(): Icon = icon()

    override fun activeIcon(): Icon = icon()

    override fun inActiveLabel(): String = label()

    override fun preference(): Boolean = preferences.ampMode()

    private fun label() = getString(R.string.amp_mode)
    private fun icon() = Icon.createWithResource(this, R.drawable.ic_action_amp_icon)
}
