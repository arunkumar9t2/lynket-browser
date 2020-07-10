package arun.com.chromer.bubbles.system

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import arun.com.chromer.browsing.icons.WebsiteIconsProvider
import arun.com.chromer.bubbles.FloatingBubble
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.shared.Constants
import com.jakewharton.rxrelay2.PublishRelay
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import hu.akarnokd.rxjava.interop.RxJavaInterop
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class BubbleLoadData(
    val website: Website,
    val fromMinimize: Boolean,
    val fromAmp: Boolean,
    val incognito: Boolean,
    val contextRef: WeakReference<Context?> = WeakReference(null),
    val icon: Bitmap? = null,
    val color: Int = Constants.NO_COLOR
)

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("CheckResult")
@Singleton
class NativeFloatingBubble
@Inject
constructor(
    private val schedulerProvider: SchedulerProvider,
    private val websiteRepository: WebsiteRepository,
    private val bubbleNotificationUtil: BubbleNotificationUtil,
    private val websiteIconsProvider: WebsiteIconsProvider
) : FloatingBubble {

  private val loadQueue = PublishRelay.create<BubbleLoadData>()

  init {
    loadQueue.toFlowable(BUFFER)
        .observeOn(schedulerProvider.pool)
        .flatMap(::showBubble)
        .subscribeBy(onError = Timber::e)
  }

  override fun openBubble(
      website: Website,
      fromMinimize: Boolean,
      fromAmp: Boolean,
      incognito: Boolean,
      context: Context?,
      color: Int
  ) = loadQueue.accept(BubbleLoadData(
      website = website,
      fromMinimize = fromMinimize,
      fromAmp = fromAmp,
      incognito = incognito,
      contextRef = WeakReference(context),
      color = color
  ))

  private fun showBubble(bubbleLoadData: BubbleLoadData): Flowable<BubbleLoadData> {
    return bubbleNotificationUtil.showBubbles(bubbleLoadData)
        .delay(1, TimeUnit.SECONDS, schedulerProvider.pool) // Avoid notification throttling
        .toFlowable()
        .flatMap { bubbleData ->
          RxJavaInterop.toV2Flowable(websiteRepository.getWebsite(bubbleData.website.url))
              .subscribeOn(schedulerProvider.io)
              .observeOn(schedulerProvider.pool)
              .flatMapSingle { website ->
                websiteIconsProvider.getBubbleIconAndColor(website)
                    .map { iconData ->
                      bubbleData.copy(
                          website = website,
                          icon = iconData.icon,
                          color = iconData.color
                      )
                    }
              }.onErrorReturnItem(bubbleData)
        }.flatMapSingle(bubbleNotificationUtil::showBubbles)
  }
}