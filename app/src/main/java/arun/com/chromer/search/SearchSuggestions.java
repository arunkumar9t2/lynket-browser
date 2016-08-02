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

    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext;
    private SuggestionTask mSuggestionTask;
    private SuggestionsCallback mSuggestionsCallback = new SuggestionsCallback() {
        @Override
        public void onFetchSuggestions(@NonNull List<String> suggestions) {
            // no op
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

    /**
     * Hits the suggest API with the given string as query parameter. Parses the xml from the API and
     * notifies the client with a list of strings(suggestions).
     */
    private class SuggestionTask extends AsyncTask<Void, Void, String> {
        /**
         * Query to fetch suggestions for.
         */
        private final String mQuery;

        SuggestionTask(@NonNull String query) {
            mQuery = query;
        }

        @Override
        protected String doInBackground(Void... voids) {
            final List<String> suggestions = new LinkedList<>();
            final String suggestUrl = SEARCH_URL + mQuery;
            HttpURLConnection connection = null;
            try {
                final URL url = new URL(suggestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                final InputStream inputStream = connection.getInputStream();
                final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                final XmlPullParser xmlParser = factory.newPullParser();
                xmlParser.setInput(inputStream, null);

                int suggestionsCount = 0;
                int eventType = xmlParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT && suggestionsCount < 6 && !isCancelled()) {
                    if (eventType == XmlPullParser.START_TAG && xmlParser.getName().equalsIgnoreCase("suggestion")) {
                        final String suggestion = xmlParser.getAttributeValue(0);
                        suggestions.add(suggestion);
                        suggestionsCount++;
                    }
                    eventType = xmlParser.next();
                }
                if (!isCancelled()) {
                    postCallbackOnUiThread(suggestions);
                } else {
                    Timber.d("Skipped callback due to cancelled task");
                }
            } catch (IOException | XmlPullParserException e) {
                Timber.e(e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        private void postCallbackOnUiThread(final List<String> suggestions) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mSuggestionsCallback.onFetchSuggestions(suggestions);
                }
            });
        }
    }

    public interface SuggestionsCallback {
        @UiThread
        void onFetchSuggestions(@NonNull List<String> suggestions);
    }
}
