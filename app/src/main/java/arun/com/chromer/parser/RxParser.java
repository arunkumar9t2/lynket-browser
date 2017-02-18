package arun.com.chromer.parser;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.Extractor;
import com.chimbori.crux.urls.CandidateURL;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import timber.log.Timber;

/**
 * Created by Arunkumar on 26-01-2017.
 */
public class RxParser {
    // Our parser subject
    private final SerializedSubject<String, String> parserSubject = PublishSubject.<String>create().toSerialized();
    // Singleton
    private static volatile RxParser INSTANCE = null;
    // No that determines no of pages that can be concurrently parsed.
    private static final int MAX_CONCURRENT_PARSING = 4;

    // Reference to subscription that we create.
    private Subscription parseSubscription;
    // To get notified about parse completion event..
    private OnParseListener onParseListener = new OnParseListener() {
        @Override
        public void onUrlParsed(@NonNull String url, @Nullable Article article) {
            // No-op
        }
    };

    /**
     * Private constructor. Use {@link #getInstance()}
     */
    private RxParser() {

    }

    /**
     * Used to get a thread safe instance of {@link RxParser}
     *
     * @return RxParser instance.
     */
    public static RxParser getInstance() {
        if (INSTANCE == null) {
            synchronized (RxParser.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RxParser();
                }
            }
        }
        INSTANCE.init();
        return INSTANCE;
    }

    /**
     * Initializes the Subject that receive urls to process via {@link #parse(String)}.
     * We only subscribe if no subscription exist currently.
     */
    private void init() {
        if (parseSubscription == null || parseSubscription.isUnsubscribed()) {
            Timber.d("Creating new subscription");
            parseSubscription = parserSubject
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String url) {
                            return url != null;
                        }
                    }).onBackpressureBuffer()
                    .flatMap(new Func1<String, Observable<Pair<String, Article>>>() {
                        @Override
                        public Observable<Pair<String, Article>> call(String url) {
                            return Observable.just(url)
                                    .subscribeOn(Schedulers.computation())
                                    .map(URL_TO_ARTICLE_PAIR_MAPPER);
                        }
                    }, MAX_CONCURRENT_PARSING).observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.e(throwable);
                        }
                    })
                    .doOnNext(new Action1<Pair<String, Article>>() {
                        @Override
                        public void call(Pair<String, Article> articlePair) {
                            onParseListener.onUrlParsed(articlePair.first, articlePair.second);
                        }
                    }).subscribe();
        }
    }

    public void parse(@NonNull String url) {
        parserSubject.onNext(url);
    }

    public void setOnParseListener(@Nullable OnParseListener onParseListener) {
        if (onParseListener == null) {
            onParseListener = new OnParseListener() {
                @Override
                public void onUrlParsed(@NonNull String url, @Nullable Article article) {

                }
            };
        }
        this.onParseListener = onParseListener;
    }

    /**
     * Used to clear current active subscription.
     */
    public void unsubscribe() {
        if (parseSubscription != null && !parseSubscription.isUnsubscribed()) {
            parseSubscription.unsubscribe();
        }
    }

    /**
     * Converts the given URL to its extracted article metadata form. The extraction is not performed
     * if the given url is not a proper web url.
     */
    private static final Func1<String, Pair<String, Article>> URL_TO_ARTICLE_PAIR_MAPPER =
            new Func1<String, Pair<String, Article>>() {
                @Override
                public Pair<String, Article> call(final String url) {
                    Article article = null;
                    try {
                        final String expanded = WebsiteDownloader.unShortenUrl(url);
                        final CandidateURL candidateUrl = new CandidateURL(expanded);
                        if (candidateUrl.resolveRedirects().isLikelyArticle()) {
                            String webSiteString = WebsiteDownloader.htmlString(candidateUrl.toString());
                            article = Extractor.with(url, webSiteString)
                                    .extractMetadata()
                                    .article();
                            // Dispose string
                            webSiteString = null;
                            Timber.d("Fetched %s in %s", url, getThreadName());
                        }
                    } catch (Exception | OutOfMemoryError e) {
                        Timber.e(e.getMessage());
                        Observable.error(e);
                    }
                    return new Pair<>(url, article);
                }
            };

    private static void printThread() {
        Timber.d("Thread: %s", getThreadName());
    }

    private static String getThreadName() {
        return Thread.currentThread().getName();
    }

    public static Observable<Pair<String, Article>> parseUrl(@Nullable String url) {
        return Observable.just(url).map(URL_TO_ARTICLE_PAIR_MAPPER);
    }

    public static Article parseUrlSync(@Nullable String url) {
        return Observable.just(url).map(URL_TO_ARTICLE_PAIR_MAPPER).toBlocking().first().second;
    }

    public interface OnParseListener {
        @MainThread
        void onUrlParsed(final @NonNull String url, final @Nullable Article article);
    }
}
