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

package arun.com.chromer.browsing.article.qs

import android.graphics.drawable.Icon
import android.os.Build
import android.support.annotation.RequiresApi
import arun.com.chromer.R
import arun.com.chromer.shared.base.PreferenceQuickSettingsTile

@RequiresApi(api = Build.VERSION_CODES.N)
class ArticleTile : PreferenceQuickSettingsTile() {
    override fun togglePreference() = preferences.articleMode(!preferences.articleMode())

    override fun activeLabel(): String = label()

    override fun inActiveIcon(): Icon = icon()

    override fun activeIcon(): Icon = icon()

    override fun inActiveLabel(): String = label()

    override fun preference(): Boolean = preferences.articleMode()

    private fun label() = getString(R.string.article_mode)
    private fun icon() = Icon.createWithResource(this, R.drawable.ic_description_black_24dp)
}
