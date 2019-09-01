package arun.com.chromer.util.epoxy

import com.airbnb.epoxy.DiffResult
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.OnModelBuildFinishedListener
import io.reactivex.Observable

fun EpoxyController.buildEvents(): Observable<DiffResult> = Observable.create { emitter ->
    val buildListener = OnModelBuildFinishedListener { result: DiffResult ->
        emitter.onNext(result)
    }.also(::addModelBuildListener)
    emitter.setCancellable { removeModelBuildListener(buildListener) }
}

fun EpoxyController.intercepts(): Observable<List<EpoxyModel<*>>> = Observable.create { emitter ->
    val interceptor = EpoxyController.Interceptor { models ->
        emitter.onNext(models)
    }.also(::addInterceptor)
    emitter.setCancellable { removeInterceptor(interceptor) }
}