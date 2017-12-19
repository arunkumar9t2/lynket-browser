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

package arun.com.chromer.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;

import timber.log.Timber;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Created by Arun on 03/09/2016.
 */
public class DocumentUtils {

    @TargetApi(LOLLIPOP)
    public static ActivityManager.RecentTaskInfo getTaskInfoFromTask(ActivityManager.AppTask task) {
        ActivityManager.RecentTaskInfo info = null;
        try {
            info = task.getTaskInfo();
        } catch (Exception e) {
            Timber.d("Failed to retrieve task info: %s", e.toString());
        }
        return info;
    }

}
