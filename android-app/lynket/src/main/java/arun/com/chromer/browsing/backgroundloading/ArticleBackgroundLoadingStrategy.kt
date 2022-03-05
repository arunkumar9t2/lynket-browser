package arun.com.chromer.browsing.backgroundloading

import android.net.Uri
import arun.com.chromer.browsing.article.ArticlePreloader
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleBackgroundLoadingStrategy
@Inject
constructor(private val articlePreloader: ArticlePreloader) : BackgroundLoadingStrategy {

  override fun prepare(url: String) {
    articlePreloader.preloadArticle(Uri.parse(url)) { success ->
      Timber.d("Article mode preloading for $url: $success")
    }
  }
}