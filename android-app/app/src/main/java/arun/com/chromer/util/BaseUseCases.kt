package arun.com.chromer.util

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ObservableUseCase<Request, Response> {
  fun build(request: Request): Observable<Response>
}

interface SimpleObservableUseCase<Response> {
  fun build(): Observable<Response>
}

interface SingleUseCase<Request, Response> {
  fun build(request: Request): Single<Response>
}

interface SimpleSingeUseCase<Response> {
  fun build(): Single<Response>
}

interface CompletableUseCase<Request> {
  fun build(request: Request): Completable
}