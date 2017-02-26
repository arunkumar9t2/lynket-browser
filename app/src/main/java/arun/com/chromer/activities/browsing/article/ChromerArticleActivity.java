package arun.com.chromer.activities.browsing.article;

import android.content.Intent;

import arun.com.chromer.activities.CustomTabActivity;
import xyz.klinker.android.article.ArticleActivity;

public class ChromerArticleActivity extends ArticleActivity {

    @Override
    protected void onArticleLoadingFailed(Throwable throwable) {
        super.onArticleLoadingFailed(throwable);
        // Start custom tab activity on the same task, silently killing this activity.
        final Intent customTabActivity = new Intent(this, CustomTabActivity.class);
        customTabActivity.setData(getIntent().getData());
        startActivity(customTabActivity);
        finish();
    }
}
