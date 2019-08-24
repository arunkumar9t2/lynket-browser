package arun.com.chromer.home

import androidx.lifecycle.LifecycleOwner
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.lifecycle.ActivityLifecycle
import arun.com.chromer.util.lifecycle.LifecycleEvents
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import hu.akarnokd.rxjava.interop.RxJavaInterop
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerActivity
class TabsLifecycleObserver
@Inject
constructor(
        @ActivityLifecycle
        lifecycleOwner: LifecycleOwner,
        private val tabsManager: TabsManager,
        private val schedulerProvider: SchedulerProvider
) : LifecycleEvents(lifecycleOwner) {
    fun activeTabs(): Observable<List<TabsManager.Tab>> = starts.flatMap {
        Observable.interval(750, TimeUnit.MILLISECONDS)
                .flatMapSingle {
                    RxJavaInterop.toV2Single(tabsManager.getActiveTabs())
                }.startWith(RxJavaInterop.toV2Single(tabsManager.getActiveTabs()).toObservable())
                .distinctUntilChanged()
                .takeUntil(stops)
                .compose(schedulerProvider.poolToUi())
    }
}