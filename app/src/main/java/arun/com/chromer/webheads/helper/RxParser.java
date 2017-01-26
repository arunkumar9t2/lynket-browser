package arun.com.chromer.webheads.helper;

import android.support.annotation.NonNull;

import com.chimbori.crux.Article;
import com.chimbori.crux.CandidateURL;
import com.chimbori.crux.Extractor;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by Arunkumar on 26-01-2017.
 */
public class RxParser {
    private final PublishSubject<String> parserSubject = PublishSubject.create();

    private static volatile RxParser INSTANCE = null;
    // We will spoof as an iPad so that websites properly expose their shortcut icon.
    private static final String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";

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
        return INSTANCE;
    }

    /**
     * Converts the given URL to its extracted article form. The extraction is not performed if the
     * given url is not a proper web url.
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
                            .build();
                    final String stringData = new OkHttpClient().newCall(request).execute().body().string();
                    article = Extractor.with(candidateUrl, stringData).extractMetadata().article();
                }
            } catch (IOException e) {
                Timber.e(e.getMessage());
                Observable.error(e);
            }
            return article;
        }
    };

    private static void printThread() {
        Timber.d("Thread: %s", Thread.currentThread().getName());
    }

    private RxParser() {
        parserSubject.flatMap(new Func1<String, Observable<Article>>() {
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
                Timber.d(article.url);
            }
        }).subscribe();
    }

    public void parse(@NonNull String url, @NonNull Runnable runnable) {
        parserSubject.onNext(url);
    }
}
