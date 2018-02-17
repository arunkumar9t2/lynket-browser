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

package arun.com.chromer.browsing.incognito

import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.support.annotation.RequiresApi
import arun.com.chromer.R
import arun.com.chromer.shared.base.PreferenceQuickSettingsTile
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable

/**
 * Created by Arun on 09/09/2016.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
class IncognitoTile : PreferenceQuickSettingsTile() {
    override fun togglePreference() = preferences.fullIncognitoMode(!preferences.fullIncognitoMode())

    override fun activeLabel(): String = label()

    override fun inActiveIcon(): Icon = icon()

    override fun activeIcon(): Icon = icon()

    override fun inActiveLabel(): String = label()

    override fun preference(): Boolean = preferences.fullIncognitoMode()

    private fun label() = getString(R.string.incognito_mode)
    private fun icon() = Icon.createWithBitmap(IconicsDrawable(this)
            .icon(CommunityMaterial.Icon.cmd_incognito)
            .color(Color.WHITE)
            .sizeDp(24).toBitmap())
}
