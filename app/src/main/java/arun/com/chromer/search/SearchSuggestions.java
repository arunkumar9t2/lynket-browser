package arun.com.chromer.search;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import arun.com.chromer.util.Utils;
import timber.log.Timber;

/**
 * Created by Arun on 02/08/2016.
 * Helper class to fetch search suggestions for a given string. Fetched suggestions can retrieved by
 * providing a callback instance.
 */
public class SearchSuggestions {
    private static final String BASE = "http://suggestqueries.google.com/complete/search?";
    private static final String CLIENT = "client=toolbar";
    private static final String SEARCH_URL = BASE + CLIENT + "&q=";

    private static final int MAX_SUGGESTIONS = 6;

    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext;
    private SuggestionTask mSuggestionTask;
    private SuggestionsCallback mSuggestionsCallback = new SuggestionsCallback() {
        @Override
        public void onFetchSuggestions(@NonNull List<SuggestionItem> suggestions) {

        }
    };

    public SearchSuggestions(@NonNull final Context context, @Nullable SuggestionsCallback suggestionsCallback) {
        mContext = context.getApplicationContext();

        if (suggestionsCallback != null) {
            mSuggestionsCallback = suggestionsCallback;
        }
    }

    /**
     * Clears any suggestions task that are in process and dispatches a new fetcher task.
     *
     * @param query String to fetch suggestions for.
     */
    public synchronized void fetchForQuery(@NonNull String query) {
        cancelLastFetch();
        mSuggestionTask = new SuggestionTask(query);
        mSuggestionTask.execute();
    }

    private void cancelLastFetch() {
        if (mSuggestionTask != null) {
            mSuggestionTask.cancel(true);
        }
    }

    public int getMaxSuggestions() {
        return MAX_SUGGESTIONS;
    }

    /**
     * Hits the suggest API with the given string as query parameter. Parses the xml from the API and
     * notifies the client with a list of strings(suggestions).
     */
    private class SuggestionTask extends AsyncTask<Void, Void, String> {
        /**
         * Query to fetch suggestions for.
         */
        private final String mQuery;
        private final List<SuggestionItem> mSuggestions;

        SuggestionTask(@NonNull String query) {
            mQuery = query;
            mSuggestions = new LinkedList<>();
        }

        @Override
        protected void onPreExecute() {
            addCopySuggestion();
        }

        @Override
        protected String doInBackground(Void... voids) {
            final String suggestUrl = SEARCH_URL.concat(mQuery).replace(" ", "+");
            HttpURLConnection connection = null;
            try {
                if (Utils.isNetworkAvailable(mContext)) {
                    final URL url = new URL(suggestUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    final InputStream inputStream = connection.getInputStream();
                    final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(false);
                    final XmlPullParser xmlParser = factory.newPullParser();
                    xmlParser.setInput(inputStream, null);

                    int eventType = xmlParser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT && mSuggestions.size() < MAX_SUGGESTIONS && !isCancelled()) {
                        if (eventType == XmlPullParser.START_TAG && xmlParser.getName().equalsIgnoreCase("suggestion")) {
                            final String suggestion = xmlParser.getAttributeValue(0);
                            mSuggestions.add(new SuggestionItem(suggestion));
                        }
                        eventType = xmlParser.next();
                    }
                } else {
                    Timber.e("Suggestion fetching skipped due to network");
                }

                postCallbackOnUiThread();
            } catch (IOException | XmlPullParserException e) {
                Timber.e(e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        private void addCopySuggestion() {
            final String copyText = Utils.getClipBoardText(mContext);
            if (copyText != null && copyText.length() > 0) {
                mSuggestions.add(new SuggestionItem(copyText, SuggestionItem.COPY));
            }
        }

        private void postCallbackOnUiThread() {
            if (!isCancelled()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mSuggestionsCallback.onFetchSuggestions(mSuggestions);
                    }
                });
            } else {
                Timber.d("Skipped callback due to cancelled task");
            }
        }
    }

    public interface SuggestionsCallback {
        @UiThread
        void onFetchSuggestions(@NonNull List<SuggestionItem> suggestions);
    }
}
