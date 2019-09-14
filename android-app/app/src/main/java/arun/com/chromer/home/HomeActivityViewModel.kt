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
import arun.com.chromer.settings.RxPreferences
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import dev.arunkumar.android.result.asResult
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import dev.arunkumar.android.viewmodel.RxViewModel
import dev.arunkumar.common.result.Result
import io.reactivex.Observable
import io.reactivex.functions.Function3
import javax.inject.Inject

@SuppressLint("CheckResult")
class HomeActivityViewModel
@Inject
constructor(
        private val application: Application,
        private val rxPreferences: RxPreferences,
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
        historyRepository.recents()
                .asResult()
                .compose(schedulerProvider.ioToUi())
                .subscribe(recentsLiveData::setValue)
    }

    private fun bindProviderInfo() {
        Observable.combineLatest(
                rxPreferences.customTabProviderPref.observe(),
                rxPreferences.incognitoPref.observe(),
                rxPreferences.webviewPref.observe(),
                Function3 { customTabProvider: String, isIncognito: Boolean, isWebView: Boolean ->
                    if (customTabProvider.isEmpty() || isIncognito || isWebView) {
                        CustomTabProviderInfo(
                                iconUri = ApplicationIcon.createUri(Constants.SYSTEM_WEBVIEW),
                                providerDescription = StringResource(
                                        R.string.tab_provider_status_message_home,
                                        resourceArgs = listOf(R.string.system_webview)
                                ),
                                providerReason = if (isIncognito)
                                    StringResource(R.string.provider_web_view_incognito_reason)
                                else StringResource(0),
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
                }).compose(schedulerProvider.poolToUi())
                .untilCleared()
                .subscribe(providerInfoLiveData::setValue)
    }
}