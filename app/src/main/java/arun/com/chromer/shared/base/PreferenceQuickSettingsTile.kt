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

package arun.com.chromer.shared.base

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.N)
/**
 * Created by arunk on 09-02-2018.
 */
abstract class PreferenceQuickSettingsTile : TileService() {
    /**
     * Called when the tile is added to the Quick Settings.
     */
    override fun onTileAdded() {
        Timber.d("Tile added")
        updateTile()
    }

    /**
     * Called when this tile begins listening for events.
     */
    override fun onStartListening() {
        Timber.d("Start listening")
        updateTile()
    }

    /**
     * Called when the user taps the tile.
     */
    override fun onClick() {
        togglePreference()
        updateTile()
    }

    abstract fun togglePreference()

    /**
     * Called when this tile moves out of the listening state.
     */
    override fun onStopListening() {
        Timber.d("Stop Listening")
    }

    /**
     * Called when the user removes this tile from Quick Settings.
     */
    override fun onTileRemoved() {
        Timber.d("Tile removed")
    }

    // Changes the appearance of the tile.
    private fun updateTile() {
        val tile = qsTile
        if (tile != null) {
            val ampActive = preference()
            val newIcon: Icon
            val newLabel: String
            val newState: Int
            // Change the tile to match the service status.
            if (ampActive) {
                newLabel = activeLabel()
                newIcon = activeIcon()
                newState = Tile.STATE_ACTIVE
            } else {
                newLabel = inActiveLabel()
                newIcon = inActiveIcon()
                newState = Tile.STATE_INACTIVE
            }
            // Change the UI of the tile.
            tile.label = newLabel
            tile.icon = newIcon
            tile.state = newState

            // Need to call updateTile for the tile to pick up changes.
            tile.updateTile()
        }
    }

    abstract fun activeLabel(): String

    abstract fun inActiveIcon(): Icon

    abstract fun activeIcon(): Icon

    abstract fun inActiveLabel(): String

    abstract fun preference(): Boolean
}