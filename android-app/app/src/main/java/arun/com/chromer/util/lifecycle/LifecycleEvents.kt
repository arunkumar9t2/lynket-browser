package arun.com.chromer.util.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.di.scopes.PerFragment
import com.jakewharton.rxrelay2.PublishRelay
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

  val created = lifecycleEventRelay.filter { it == ON_CREATE }
  val resumes = lifecycleEventRelay.filter { it == ON_RESUME }
  val starts = lifecycleEventRelay.filter { it == ON_START }
  val pauses = lifecycleEventRelay.filter { it == ON_PAUSE }
  val stops = lifecycleEventRelay.filter { it == ON_STOP }
  val destroys = lifecycleEventRelay.filter { it == ON_DESTROY }
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