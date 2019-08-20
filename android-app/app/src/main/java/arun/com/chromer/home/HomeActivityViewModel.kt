package arun.com.chromer.home

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import arun.com.chromer.R
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.extenstions.StringResource
import arun.com.chromer.extenstions.appName
import arun.com.chromer.home.epoxycontroller.model.CustomTabProviderInfo
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity.ProviderChanged
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import dev.arunkumar.android.result.asResult
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import dev.arunkumar.android.viewmodel.RxViewModel
import dev.arunkumar.common.result.Result
import hu.akarnokd.rxjava.interop.RxJavaInterop
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@SuppressLint("CheckResult")
class HomeActivityViewModel
@Inject
constructor(
        private val application: Application,
        private val rxEventBus: RxEventBus,
        private val preferences: Preferences,
        private val schedulerProvider: SchedulerProvider,
        private val historyRepository: HistoryRepository
) : RxViewModel() {

    val providerInfoLiveData = MutableLiveData<CustomTabProviderInfo>()
    val recentsLiveData = MutableLiveData<Result<List<Website>>>()

    init {
        start()
    }

    private fun start() {
        bindProviderInfo()
        bindRecentsInfo()
    }

    private fun bindRecentsInfo() {
        RxJavaInterop.toV2Flowable(historyRepository.recents())
                .asResult()
                .compose(schedulerProvider.ioToUi())
                .subscribe(recentsLiveData::setValue)
    }

    private fun bindProviderInfo() {
        Observable
                .mergeArray(
                        Observable.just(ProviderChanged()),
                        RxJavaInterop.toV2Observable(rxEventBus.filteredEvents<ProviderChanged>())
                ).flatMapSingle {
                    Single.fromCallable {
                        val customTabProvider: String? = preferences.customTabPackage()
                        val isIncognito = preferences.fullIncognitoMode()
                        val isWebView = preferences.useWebView()
                        if (customTabProvider == null || isIncognito || isWebView) {
                            CustomTabProviderInfo(
                                    iconUri = ApplicationIcon.createUri(Constants.SYSTEM_WEBVIEW),
                                    providerDescription = StringResource(
                                            R.string.tab_provider_status_message_home,
                                            resourceArgs = listOf(R.string.system_webview)
                                    ),
                                    providerReason = StringResource(R.string.provider_web_view_incognito_reason),
                                    allowChange = !isIncognito
                            )
                        } else {
                            val appName = application.appName(customTabProvider)
                            CustomTabProviderInfo(
                                    iconUri = ApplicationIcon.createUri(customTabProvider),
                                    providerDescription = StringResource(
                                            R.string.tab_provider_status_message_home,
                                            listOf(appName)
                                    ),
                                    providerReason = StringResource(0)
                            )
                        }
                    }
                }.untilCleared()
                .compose(schedulerProvider.poolToUi())
                .subscribe(providerInfoLiveData::setValue)
    }
}