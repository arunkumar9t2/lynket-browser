package arun.com.chromer.activities.browsing.article;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import arun.com.chromer.activities.CustomTabActivity;
import timber.log.Timber;
import xyz.klinker.android.article.ArticleActivity;

import static android.content.Intent.EXTRA_TEXT;
import static arun.com.chromer.shared.Constants.ACTION_MINIMIZE;

public class ChromerArticleActivity extends ArticleActivity {
    private String baseUrl = "";
    private BroadcastReceiver minimizeReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerMinimizeReceiver();
    }

    @Override
    protected void onArticleLoadingFailed(Throwable throwable) {
        super.onArticleLoadingFailed(throwable);
        // Start custom tab activity on the same task, silently killing this activity.
        final Intent customTabActivity = new Intent(this, CustomTabActivity.class);
        customTabActivity.setData(getIntent().getData());
        startActivity(customTabActivity);
        finish();
    }

    private void registerMinimizeReceiver() {
        minimizeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(ACTION_MINIMIZE) && intent.hasExtra(EXTRA_TEXT)) {
                    final String url = intent.getStringExtra(EXTRA_TEXT);
                    if (baseUrl.equalsIgnoreCase(url)) {
                        try {
                            Timber.d("Minimized %s", url);
                            moveTaskToBack(true);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(minimizeReceiver, new IntentFilter(ACTION_MINIMIZE));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(minimizeReceiver);
        super.onDestroy();
    }
}
