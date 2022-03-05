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
