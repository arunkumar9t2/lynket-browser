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

package arun.com.chromer.data.webarticle.stores;

import androidx.annotation.NonNull;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.webarticle.WebArticleStore;
import arun.com.chromer.data.webarticle.model.WebArticle;
import arun.com.chromer.util.parser.RxParser;
import rx.Observable;

/**
 * Network store which freshly parses website data for a given URL.
 */
@Singleton
public class WebArticleNetworkStore implements WebArticleStore {

  @Inject
  WebArticleNetworkStore() {
  }

  @NonNull
  @Override
  public Observable<WebArticle> getWebArticle(@NonNull String url) {
    return RxParser.INSTANCE.parseArticle(url)
      .flatMap(urlArticlePair -> {
        if (urlArticlePair.second != null) {
          return Observable.just(WebArticle.fromArticle(urlArticlePair.second));
        } else {
          return Observable.just(null);
        }
      }).map(webArticle -> {
        if (webArticle != null) {
          // Clean up all the empty strings.
          final Elements rawElements = webArticle.elements;
          for (Iterator<Element> iterator = rawElements.iterator(); iterator.hasNext(); ) {
            final Element element = iterator.next();
            if (element.text().isEmpty()) {
              iterator.remove();
            }
          }
        }
        return webArticle;
      });
  }

  @NonNull
  @Override
  public Observable<WebArticle> saveWebArticle(@NonNull WebArticle webSite) {
    return Observable.empty();
  }
}
