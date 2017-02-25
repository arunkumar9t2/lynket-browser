package arun.com.chromer.parser;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.Extractor;
import com.chimbori.crux.urls.CandidateURL;

import arun.com.chromer.util.Utils;
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
            final CandidateURL candidateUrl = new CandidateURL(expanded);
            if (candidateUrl.resolveRedirects().isLikelyArticle()) {
                String webSiteString = WebsiteUtilities.headString(candidateUrl.toString());

                article = Extractor
                        .with(url, webSiteString)
                        .extractMetadata()
                        .article();

                //noinspection UnusedAssignment
                webSiteString = null;
                Utils.printThread();
            }
        } catch (Exception | OutOfMemoryError e) {
            Timber.e(e.getMessage());
            Observable.error(e);
        }
        return new Pair<>(url, article);
    };
}
