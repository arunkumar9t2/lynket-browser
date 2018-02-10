package arun.com.chromer.data.apps

import arun.com.chromer.data.apps.store.AppStore
import arun.com.chromer.data.common.App
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by arunk on 10-02-2018.
 */
@Singleton
class MockAppSystemStore @Inject constructor() : AppStore {

    override fun removeIncognito(packageName: String): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getApp(packageName: String): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveApp(app: App): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPackageBlacklisted(packageName: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPackageBlacklisted(packageName: String): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPackageIncognito(packageName: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPackageIncognito(packageName: String): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPackageColorSync(packageName: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPackageColor(packageName: String): Observable<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPackageColor(packageName: String, color: Int): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeBlacklist(packageName: String): Observable<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInstalledApps(): Observable<App> {
        val app = App("App", "Package", true, true, 0)
        return Observable.just(app)
    }
}