package arun.com.chromer.webheads.helper;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.chimbori.crux.Article;
import com.chimbori.crux.CandidateURL;
import com.chimbori.crux.Extractor;

import java.io.IOException;

import arun.com.chromer.Chromer;
import okhttp3.Request;
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
    private final SerializedSubject<String, String> parserSubject = PublishSubject.<String>create().toSerialized();

    private static volatile RxParser INSTANCE = null;
    // We will spoof as an iPad so that websites properly expose their shortcut icon. Even Google.com
    // does not provide bigger icons when we go as Android.
    private static final String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25";
    private static final String CHROMER = "Chromer";

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
                    })
                    .flatMap(new Func1<String, Observable<Pair<String, Article>>>() {
                        @Override
                        public Observable<Pair<String, Article>> call(String url) {
                            return Observable.just(url)
                                    .subscribeOn(Schedulers.computation())
                                    .map(URL_TO_ARTICLE_PAIR_MAPPER);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
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
                    printThread();
                    Article article = null;
                    try {
                        final CandidateURL candidateUrl = new CandidateURL(url);
                        if (candidateUrl.isLikelyArticle()) {
                            final Request request = new Request.Builder()
                                    .url(url)
                                    .header("User-Agent", USER_AGENT)
                                    .header("Referer", CHROMER)
                                    .build();

                            final String stringData = Chromer.getOkHttpClient()
                                    .newCall(request)
                                    .execute()
                                    .body()
                                    .string();

                            article = Extractor.with(candidateUrl, stringData)
                                    .extractMetadata()
                                    .article();
                            Timber.d(article.toString());
                        }
                    } catch (IOException e) {
                        Timber.e(e.getMessage());
                        Observable.error(e);
                    }
                    return new Pair<>(url, article);
                }
            };

    /**
     * Prints the current active thread. Only for debug purposes.
     */
    private static void printThread() {
        Timber.d("Thread: %s", Thread.currentThread().getName());
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
