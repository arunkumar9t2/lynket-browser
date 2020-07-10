package arun.com.chromer.search.provider

import android.net.Uri
import android.util.Patterns.WEB_URL
import androidx.core.net.toUri
import arun.com.chromer.settings.RxPreferences
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class SearchProvider(
    val name: String,
    val iconUri: Uri,
    val searchUrlPrefix: String
) {
  fun getSearchUrl(text: String): String {
    return if (WEB_URL.matcher(text).matches()) {
      when {
        !text.toLowerCase(Locale.getDefault()).matches("^\\w+://.*".toRegex()) -> "http://$text"
        else -> text
      }
    } else searchUrlPrefix + text.replace(" ", "+")
  }
}

@Singleton
class SearchProviders
@Inject
constructor(
    rxPreferences: RxPreferences,
    schedulerProvider: SchedulerProvider
) {

  val availableProviders: Single<List<SearchProvider>> = Single.fromCallable {
    listOf(
        GOOGLE_SEARCH_PROVIDER,
        SearchProvider(
            name = DUCKDUCKGO,
            iconUri = "https://lh3.googleusercontent.com/8GiPaoaCopqI1AiBajxwx91ndKDeeAI-p2w7hDZlG7yi6KoXJ5bzWA0VteFpTAB5uhM=s192-rw".toUri(),
            searchUrlPrefix = "https://duckduckgo.com/?q="
        ),
        SearchProvider(
            name = BING,
            iconUri = "https://lh3.googleusercontent.com/0aRIOVqPu3KKUh6FFSmo1jkQMIeTqgGvHNo4mHl_NUzJxGGd2m0jaUoRdhGcgaa-ug=s192-rw".toUri(),
            searchUrlPrefix = "https://www.bing.com/search?q="
        ),
        SearchProvider(
            name = QWANT,
            iconUri = "https://lh3.googleusercontent.com/gZM93E0coPblwJysaGbAVgTRXPld0ZDRtrbmclDqWWrPJLKIjyVB9XKqOX8OM9_3GJI=s192-rw".toUri(),
            searchUrlPrefix = "https://www.qwant.com/?q="
        )
    )
  }

  val selectedProvider: Observable<SearchProvider> = rxPreferences
      .searchEngine
      .observe()
      .observeOn(schedulerProvider.pool)
      .switchMap { selectedEngine ->
        availableProviders
            .toObservable()
            .flatMapIterable { it }
            .filter { it.name == selectedEngine }
            .first(GOOGLE_SEARCH_PROVIDER)
            .toObservable()
      }.replay(1)
      .refCount()
      .observeOn(schedulerProvider.ui)

  companion object {
    const val GOOGLE = "Google"
    const val DUCKDUCKGO = "Duck Duck Go"
    const val BING = "Bing"
    const val QWANT = "Qwant"

    val GOOGLE_SEARCH_PROVIDER = SearchProvider(
        name = GOOGLE,
        iconUri = "https://cdn3.iconfinder.com/data/icons/google-suits-1/32/1_google_search_logo_engine_service_suits-256.png".toUri(),
        searchUrlPrefix = "https://www.google.com/search?q="
    )
  }
}