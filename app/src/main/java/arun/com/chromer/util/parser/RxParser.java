/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.util.parser;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import com.chimbori.crux.urls.CruxURL;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by Arunkumar on 26-01-2017.
 */
public class RxParser {
    private RxParser() {

    }

    public static Observable<Pair<String, Article>> parseUrl(@Nullable String url) {
        return Observable.just(url).map(URL_TO_ARTICLE_PAIR_MAPPER);
    }

    public static Article parseUrlSync(@Nullable String url) {
        return Observable.just(url).map(URL_TO_ARTICLE_PAIR_MAPPER).toBlocking().first().second;
    }

    /**
     * Converts the given URL to its extracted article metadata form. The extraction is not performed
     * if the given url is not a proper web url.
     */
    private static final Func1<String, Pair<String, Article>> URL_TO_ARTICLE_PAIR_MAPPER = url -> {
        Article article = null;
        try {
            final String expanded = WebsiteUtilities.unShortenUrl(url);
            final CruxURL candidateUrl = CruxURL.parse(expanded);
            if (candidateUrl.resolveRedirects().isLikelyArticle()) {
                String webSiteString = WebsiteUtilities.headString(candidateUrl.toString());

                article = ArticleExtractor
                        .with(expanded, webSiteString)
                        .extractMetadata()
                        .article();

                //noinspection UnusedAssignment
                webSiteString = null;
            }
        } catch (Exception | OutOfMemoryError e) {
            Timber.e(e);
            Observable.error(e);
        }
        return new Pair<>(url, article);
    };
}
