/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.util.parser

import androidx.core.util.Pair
import arun.com.chromer.util.parser.WebsiteUtilities.headString
import com.chimbori.crux.articles.Article
import com.chimbori.crux.articles.ArticleExtractor
import com.chimbori.crux.urls.CruxURL
import org.jsoup.Jsoup
import rx.Observable
import timber.log.Timber

/**
 * Created by Arunkumar on 26-01-2017.
 */
object RxParser {
  /**
   * Converts the given URL to its extracted article metadata form. The extraction is not performed
   * if the given url is not a proper web url.
   */
  private val URL_TO_METADATA_MAPPER: (String) -> Pair<String, Article?> = { url: String ->
    var article: Article? = null
    try {
      val expanded = WebsiteUtilities.unShortenUrl(url)
      val candidateUrl = CruxURL.parse(expanded)
      if (candidateUrl.resolveRedirects().isLikelyArticle) {
        // We only need the head tag for meta data.
        var webSiteString = headString(candidateUrl.toString())
        article = ArticleExtractor
          .with(expanded, webSiteString)
          .extractMetadata()
          .article()
        @Suppress("UNUSED_VALUE")
        webSiteString = null
      }
    } catch (e: Exception) {
      Timber.e(e)
    } catch (e: OutOfMemoryError) {
      Timber.e(e)
    }
    Pair(url, article)
  }


  private val URL_TO_WEB_ARTICLE_PAIR_MAPPER = { url: String ->
    var article: Article? = null
    try {
      val cruxURL = CruxURL.parse(url)
      val isArticle = cruxURL.resolveRedirects().isLikelyArticle
      if (isArticle) {
        val document = Jsoup.connect(cruxURL.toString()).get()
        article = ArticleExtractor.with(cruxURL.toString(), document)
          .extractMetadata()
          .extractContent()
          .article()
      }
    } catch (e: Exception) {
      Timber.e(e)
    } catch (e: OutOfMemoryError) {
      Timber.e(e)
    }
    Pair(url, article)
  }

  fun parseUrl(url: String?): Observable<Pair<String, Article?>> {
    return Observable.just<String>(url).map(URL_TO_METADATA_MAPPER)
  }

  fun parseArticle(url: String?): Observable<Pair<String, Article?>> {
    return Observable.just<String>(url).map(URL_TO_WEB_ARTICLE_PAIR_MAPPER)
  }
}
