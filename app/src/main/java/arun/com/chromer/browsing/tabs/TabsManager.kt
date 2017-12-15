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

package arun.com.chromer.browsing.tabs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import arun.com.chromer.data.website.model.Website

/**
 * Helper class to manage tabs opened by Chromer. Responsible for managing Chromer's task stack.
 */
interface TabsManager {
    /**
     * Takes a {@param website} and opens in based on user preference. Checks for web heads, amp,
     * article, reordering existing tabs etc.
     */
    fun openUrl(context: Context, website: Website, fromApp: Boolean = true, fromWebHeads: Boolean = false)

    /**
     * Opens the given Uri in a browsing tab.
     */
    fun openBrowsingTab(context: Context, uri: Uri, smart: Boolean = false)

    /**
     * Returns true if it is determined that we already have any of our browsing activity has opened
     * this url.
     */
    fun reOrderTabByUrl(context: Context, website: Website): Boolean

    /**
     * If a task exist with this url already then this method should minimize it a.k.a putting it in
     * the background task.
     */
    fun minimizeTabByUrl(url: String)

    /**
     * Processes incoming intent from preferably external apps (could be us too) and then figures out
     * how to handle the intent and launch a new url.
     */
    fun processIncomingIntent(activity: Activity, intent: Intent)

    /**
     * Opens the given {@param url} irrespective of whether web heads is on.
     */
    fun openWebHeads(context: Context, url: String)


}