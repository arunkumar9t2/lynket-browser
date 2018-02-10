package arun.com.chromer.data.apps.store

import android.app.Application
import android.content.Intent
import android.content.pm.ResolveInfo
import arun.com.chromer.data.common.App
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.Utils
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by arunk on 10-02-2018.
 */
@Singleton
class AppSystemStore
@Inject
constructor(
        private val application: Application
) : AppStore {
    override fun getApp(packageName: String): Observable<App> = Observable.empty()

    override fun saveApp(app: App): Observable<App> = Observable.empty()

    override fun isPackageBlacklisted(packageName: String): Boolean = false

    override fun setPackageBlacklisted(packageName: String): Observable<App> = Observable.empty()

    override fun isPackageIncognito(packageName: String): Boolean = false

    override fun setPackageIncognito(packageName: String): Observable<App> = Observable.empty()

    override fun getPackageColorSync(packageName: String): Int = Constants.NO_COLOR

    override fun getPackageColor(packageName: String): Observable<Int> = Observable.just(Constants.NO_COLOR)

    override fun setPackageColor(packageName: String, color: Int): Observable<App> = Observable.empty()

    override fun removeBlacklist(packageName: String): Observable<App> = Observable.empty()

    override fun getInstalledApps(): Observable<App> {
        val appComparator = App.BlackListComparator()
        val pm = application.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return Observable.fromCallable<List<ResolveInfo>> { pm.queryIntentActivities(intent, 0) }
                .flatMapIterable { resolveInfos -> resolveInfos }
                .filter { resolveInfo -> resolveInfo != null && !resolveInfo.activityInfo.packageName.equals(application.packageName, ignoreCase = true) }
                .map { resolveInfo ->
                    Utils.createApp(application, resolveInfo.activityInfo.packageName)
                }.distinct()
                .sorted { app1, app2 -> appComparator.compare(app1, app2) }
    }
}