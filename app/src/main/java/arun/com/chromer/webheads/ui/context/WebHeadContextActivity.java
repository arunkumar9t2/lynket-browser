package arun.com.chromer.webheads.ui.context;

import android.content.Intent;
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
            final Intent intent = new Intent(Constants.ACTION_CLOSE_WEBHEAD_BY_URL);
            intent.putExtra(Constants.EXTRA_KEY_WEBSITE, webSite);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
