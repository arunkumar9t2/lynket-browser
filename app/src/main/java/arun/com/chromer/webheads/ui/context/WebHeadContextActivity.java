package arun.com.chromer.webheads.ui.context;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.webheads.helper.WebSite;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.EXTRA_TEXT;
import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_SHORT;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_WEBHEAD_BY_URL;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBHEAD_DELETED;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBSITE_UPDATED;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;
import static arun.com.chromer.shared.Constants.TEXT_SHARE_INTENT;

public class WebHeadContextActivity extends AppCompatActivity implements WebsiteAdapter.WebSiteAdapterListener {
    @BindView(R.id.web_sites_list)
    RecyclerView websiteListView;
    @BindView(R.id.copy_all)
    TextView copyAll;
    @BindView(R.id.share_all)
    TextView shareAll;
    @BindView(R.id.context_activity_card_view)
    CardView rootCardView;
    private WebsiteAdapter websitesAdapter;
    private final WebHeadEventsReceiver webHeadsEventsReceiver = new WebHeadEventsReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_head_context);
        ButterKnife.bind(this);

        if (getIntent() == null || getIntent().getParcelableArrayListExtra(EXTRA_KEY_WEBSITE) == null) {
            finish();
        }

        final ArrayList<WebSite> webSites = getIntent().getParcelableArrayListExtra(EXTRA_KEY_WEBSITE);

        websitesAdapter = new WebsiteAdapter(this, this);
        websitesAdapter.setWebsites(webSites);

        websiteListView.setLayoutManager(new LinearLayoutManager(this));
        websiteListView.setAdapter(websitesAdapter);

        registerEventsReceiver();
    }

    private void registerEventsReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EVENT_WEBHEAD_DELETED);
        filter.addAction(ACTION_EVENT_WEBSITE_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(webHeadsEventsReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(webHeadsEventsReceiver);
    }

    @Override
    public void onWebSiteItemClicked(@NonNull WebSite webSite) {
        finish();
        DocumentUtils.smartOpenNewTab(this, webSite);
        if (Preferences.get(this).webHeadsCloseOnOpen()) {
            broadcastDeleteWebHead(webSite);
        }
    }

    private void broadcastDeleteWebHead(@NonNull WebSite webSite) {
        final Intent intent = new Intent(ACTION_CLOSE_WEBHEAD_BY_URL);
        intent.putExtra(EXTRA_KEY_WEBSITE, webSite);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onWebSiteDelete(@NonNull final WebSite webSite) {
        final boolean shouldFinish = websitesAdapter.getWebSites().isEmpty();
        if (shouldFinish) {
            rootCardView.setVisibility(GONE);
            broadcastDeleteWebHead(webSite);
            finish();
        } else
            broadcastDeleteWebHead(webSite);
    }

    @Override
    public void onWebSiteShare(@NonNull WebSite webSite) {
        startActivity(Intent.createChooser(TEXT_SHARE_INTENT.putExtra(EXTRA_TEXT, webSite.url), getString(R.string.share)));
    }

    @Override
    public void onWebSiteLongClicked(@NonNull WebSite webSite) {
        copyToClipboard(webSite.safeLabel(), webSite.preferredUrl());
    }

    @OnClick(R.id.copy_all)
    public void onCopyAllClick() {
        copyToClipboard("Websites", getCSVUrls().toString());
    }

    @OnClick(R.id.share_all)
    public void onShareAllClick() {
        final CharSequence[] items = new String[]{
                getString(R.string.comma_seperated),
                getString(R.string.share_all_list)
        };
        new MaterialDialog.Builder(this)
                .title(R.string.choose_share_method)
                .items(items)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView,
                                               int which, CharSequence text) {
                        if (which == 0) {
                            startActivity(Intent.createChooser(TEXT_SHARE_INTENT.putExtra(EXTRA_TEXT, getCSVUrls().toString()), getString(R.string.share_all)));
                        } else {
                            final ArrayList<Uri> webSites = new ArrayList<>();
                            for (WebSite webSite : websitesAdapter.getWebSites()) {
                                try {
                                    webSites.add(Uri.parse(webSite.preferredUrl()));
                                } catch (Exception ignored) {
                                }
                            }
                            final Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, webSites);
                            shareIntent.setType("text/plain");
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_all)));
                        }
                        return false;
                    }
                }).show();
    }

    @NonNull
    private StringBuilder getCSVUrls() {
        final StringBuilder builder = new StringBuilder();
        for (WebSite webSite : websitesAdapter.getWebSites()) {
            builder.append(webSite.preferredUrl()).append(',');
        }
        return builder;
    }

    private void copyToClipboard(String label, String url) {
        final ClipData clip = ClipData.newPlainText(label, url);
        final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(clip);
        Toast.makeText(this, getString(R.string.copied) + " " + url, LENGTH_SHORT).show();
    }

    /**
     * This receiver is responsible for receiving events from web head service.
     */
    private class WebHeadEventsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_EVENT_WEBHEAD_DELETED:
                    final WebSite webSite = intent.getParcelableExtra(EXTRA_KEY_WEBSITE);
                    if (webSite != null) {
                        websitesAdapter.delete(webSite);
                    }
                    break;
                case ACTION_EVENT_WEBSITE_UPDATED:
                    final WebSite web = intent.getParcelableExtra(EXTRA_KEY_WEBSITE);
                    if (web != null) {
                        websitesAdapter.update(web);
                    }
                    break;
            }
        }
    }
}
