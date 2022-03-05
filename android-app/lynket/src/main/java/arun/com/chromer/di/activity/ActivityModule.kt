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
package arun.com.chromer.di.activity

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.lifecycle.ActivityLifecycle
import com.bumptech.glide.RequestManager
import dagger.Module
import dagger.Provides
import dev.arunkumar.android.dagger.activity.PerActivity

@Module
class ActivityModule {
  @Provides
  fun glideRequests(activity: Activity): RequestManager {
    return GlideApp.with(activity)
  }

  @Provides
  @PerActivity
  @ActivityLifecycle
  fun owner(activity: Activity) = activity as LifecycleOwner
}
