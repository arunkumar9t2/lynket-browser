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

package arun.com.chromer.browsing.article;

import android.net.Uri;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.webarticle.WebArticleRepository;
import arun.com.chromer.util.SchedulerProvider;

@Singleton
public class ArticlePreloader {

  private final WebArticleRepository webArticleRepository;

  @Inject
  ArticlePreloader(WebArticleRepository webArticleRepository) {
    this.webArticleRepository = webArticleRepository;
  }

  public void preloadArticle(@NonNull Uri uri, @Nullable final ArticlePreloadListener listener) {
    webArticleRepository.getWebArticle(uri.toString())
      .compose(SchedulerProvider.applyIoSchedulers())
      .doOnError(throwable -> {
        if (listener != null) {
          listener.onComplete(false);
        }
      })
      .subscribe(webArticle -> {
        if (listener != null) {
          listener.onComplete(true);
        }
      });
  }

  public interface ArticlePreloadListener {
    /**
     * Called when preload process has been completed. Can rely on {@param success} to know
     * whether it was success or not.
     *
     * @param success true if success, else false.
     */
    @MainThread
    void onComplete(boolean success);
  }
}
