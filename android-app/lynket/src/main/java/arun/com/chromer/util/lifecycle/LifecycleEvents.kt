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

package arun.com.chromer.util.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import dev.arunkumar.android.dagger.activity.PerActivity
import dev.arunkumar.android.dagger.fragment.PerFragment
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Qualifier

open class LifecycleEvents constructor(lifecycleOwner: LifecycleOwner) : LifecycleObserver {

  private val lifecycleEventRelay = PublishRelay.create<Lifecycle.Event>()

  val lifecycles: Observable<Lifecycle.Event> = lifecycleEventRelay

  init {
    lifecycleOwner.lifecycle.addObserver(this)
  }

  @OnLifecycleEvent(ON_CREATE)
  fun onCreate() {
    lifecycleEventRelay.accept(ON_CREATE)
  }

  @OnLifecycleEvent(ON_RESUME)
  fun onResume() {
    lifecycleEventRelay.accept(ON_RESUME)
  }

  @OnLifecycleEvent(ON_START)
  fun onStart() {
    lifecycleEventRelay.accept(ON_START)
  }

  @OnLifecycleEvent(ON_PAUSE)
  fun onPause() {
    lifecycleEventRelay.accept(ON_PAUSE)
  }

  @OnLifecycleEvent(ON_STOP)
  fun onStop() {
    lifecycleEventRelay.accept(ON_STOP)
  }

  @OnLifecycleEvent(ON_DESTROY)
  fun onDestroy() {
    lifecycleEventRelay.accept(ON_DESTROY)
  }

  val created: Observable<Lifecycle.Event> = lifecycleEventRelay.filter { it == ON_CREATE }
  val resumes: Observable<Lifecycle.Event> = lifecycleEventRelay.filter { it == ON_RESUME }
  val starts: Observable<Lifecycle.Event> = lifecycleEventRelay.filter { it == ON_START }
  val pauses: Observable<Lifecycle.Event> = lifecycleEventRelay.filter { it == ON_PAUSE }
  val stops: Observable<Lifecycle.Event> = lifecycleEventRelay.filter { it == ON_STOP }
  val destroys: Observable<Lifecycle.Event> = lifecycleEventRelay.filter { it == ON_DESTROY }
}

@Qualifier
annotation class ActivityLifecycle

@Qualifier
annotation class FragmentLifcecycle

@PerActivity
class ActivityLifecycleEvents
@Inject
constructor(@ActivityLifecycle lifecycleOwner: LifecycleOwner) : LifecycleEvents(lifecycleOwner)

@PerFragment
class FragmentLifecycle
@Inject
constructor(@FragmentLifcecycle lifecycleOwner: LifecycleOwner) : LifecycleEvents(lifecycleOwner)
