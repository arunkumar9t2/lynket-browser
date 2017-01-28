package arun.com.chromer.webheads.helper;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    // We will spoof as an iPad so that websites properly expose their shortcut icon. Even Google
    // does not provide bigger icons when we go as Android.
    private static final String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25";
    private static final String CHROMER = "Chromer";

    // Reference to subscription that we create.
    private Subscription parseSubscription;
    // To get notified about parse completion event..
    private OnParseListener onParseListener = new OnParseListener() {
        @Override
        public void onParseComplete(@NonNull String url, @NonNull Article article) {
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
            parseSubscription = parserSubject.flatMap(new Func1<String, Observable<Article>>() {
                @Override
                public Observable<Article> call(String url) {
                    return Observable.just(url)
                            .subscribeOn(Schedulers.computation())
                            .map(URL_TO_ARTICLE_MAPPER)
                            .filter(new Func1<Article, Boolean>() {
                                @Override
                                public Boolean call(Article article) {
                                    return article != null;
                                }
                            });
                }
            }).observeOn(AndroidSchedulers.mainThread()).doOnNext(new Action1<Article>() {
                @Override
                public void call(Article article) {
                    onParseListener.onParseComplete(article.url, article);
                }
            }).subscribe();
        }
    }

    /**
     * Used to clear current active subscription.
     */
    private void unsubscribe() {
        if (parseSubscription != null && !parseSubscription.isUnsubscribed()) {
            parseSubscription.unsubscribe();
        }
    }

    /**
     * Converts the given URL to its extracted article metadata form. The extraction is not performed
     * if the given url is not a proper web url.
     */
    private static final Func1<String, Article> URL_TO_ARTICLE_MAPPER = new Func1<String, Article>() {
        @Override
        public Article call(final String url) {
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
                    final String stringData = Chromer.getOkHttpClient().newCall(request).execute().body().string();
                    article = Extractor.with(candidateUrl, stringData).extractMetadata().article();
                }
            } catch (IOException e) {
                Timber.e(e.getMessage());
                Observable.error(e);
            }
            return article;
        }
    };

    /**
     * Prints the current active thread. Only for debug purposes.
     */
    private static void printThread() {
        Timber.d("Thread: %s", Thread.currentThread().getName());
    }

    public void parse(@NonNull String url) {
        parserSubject.onNext(url);
    }

    public void setOnParseListener(@Nullable OnParseListener onParseListener) {
        if (onParseListener == null) {
            onParseListener = new OnParseListener() {
                @Override
                public void onParseComplete(@NonNull String url, @NonNull Article article) {

                }
            };
        }
        this.onParseListener = onParseListener;
    }


    public interface OnParseListener {
        @MainThread
        void onParseComplete(@NonNull String url, @NonNull Article article);
    }
}
