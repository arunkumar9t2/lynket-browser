package arun.com.chromer.webheads.ui.context;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.webheads.helper.WebSite;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebHeadContextActivity extends AppCompatActivity implements WebsiteAdapter.InteractionListener {
    @BindView(R.id.web_sites_list)
    RecyclerView mWebSitesList;
    private WebsiteAdapter mWebsiteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_head_context_activity);
        ButterKnife.bind(this);

        if (getIntent() == null || getIntent().getParcelableArrayListExtra(Constants.EXTRA_KEY_WEBSITE) == null) {
            finish();
        }

        final ArrayList<WebSite> webSites = getIntent().getParcelableArrayListExtra(Constants.EXTRA_KEY_WEBSITE);

        mWebsiteAdapter = new WebsiteAdapter(this, this);
        mWebsiteAdapter.setWebsites(webSites);

        mWebSitesList.setLayoutManager(new LinearLayoutManager(this));
        mWebSitesList.setAdapter(mWebsiteAdapter);
    }

    @Override
    public void onWebSiteItemClicked(@NonNull WebSite webSite) {
        finish();
        DocumentUtils.smartOpenNewTab(this, webSite);

        if (Preferences.webHeadsCloseOnOpen(this)) {
            broadcastDeleteWebHead(webSite);
        }
    }

    private void broadcastDeleteWebHead(@NonNull WebSite webSite) {
        final Intent intent = new Intent(Constants.ACTION_CLOSE_WEBHEAD_BY_URL);
        intent.putExtra(Constants.EXTRA_KEY_WEBSITE, webSite);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onWebSiteDelete(@NonNull WebSite webSite) {
        broadcastDeleteWebHead(webSite);

        if (mWebsiteAdapter.getWebSites().isEmpty()) {
            finish();
        }
    }

    @Override
    public void onWebSiteShare(@NonNull WebSite webSite) {
        startActivity(Intent.createChooser(Constants.TEXT_SHARE_INTENT.putExtra(Intent.EXTRA_TEXT, webSite.url), getString(R.string.share)));
    }

    @OnClick(R.id.share_all)
    public void onShareAllClick() {
        final ArrayList<Uri> webSites = new ArrayList<>();
        for (WebSite webSite : mWebsiteAdapter.getWebSites()) {
            String url = webSite.longUrl != null ? webSite.longUrl : webSite.url;
            try {
                webSites.add(Uri.parse(url));
            } catch (Exception ignored) {
            }
        }
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, webSites);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_all)));
    }
}
