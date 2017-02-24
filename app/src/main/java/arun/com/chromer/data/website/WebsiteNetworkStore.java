package arun.com.chromer.data.website;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.chimbori.crux.articles.Article;

import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.parser.RxParser;
import arun.com.chromer.util.RxUtils;
import rx.Observable;
import rx.functions.Func1;

/**
 * Network store which freshly parses website data for a given URL.
 */
class WebsiteNetworkStore implements WebSiteStore {
    @SuppressWarnings("FieldCanBeLocal")
    private final Context context;

    WebsiteNetworkStore(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Observable<WebSite> getWebsite(@NonNull String url) {
        return RxParser.parseUrl(url)
                .flatMap(new Func1<Pair<String, Article>, Observable<WebSite>>() {
                    @Override
                    public Observable<WebSite> call(Pair<String, Article> urlArticlePair) {
                        if (urlArticlePair.second != null) {
                            return Observable.just(WebSite.fromArticle(urlArticlePair.second));
                        } else {
                            return Observable.just(new WebSite(urlArticlePair.first));
                        }
                    }
                }).compose(RxUtils.<WebSite>applySchedulers());
    }

    @NonNull
    @Override
    public Observable<WebSite> saveWebsite(@NonNull WebSite webSite) {
        return Observable.empty();
    }
}
