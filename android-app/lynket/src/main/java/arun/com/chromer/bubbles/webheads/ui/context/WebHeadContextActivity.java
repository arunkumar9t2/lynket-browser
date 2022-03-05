/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.bubbles.webheads.ui.context;

import static android.content.Intent.EXTRA_TEXT;
import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_SHORT;
import static arun.com.chromer.shared.Constants.ACTION_CLOSE_WEBHEAD_BY_URL;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBHEAD_DELETED;
import static arun.com.chromer.shared.Constants.ACTION_EVENT_WEBSITE_UPDATED;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBSITE;
import static arun.com.chromer.shared.Constants.TEXT_SHARE_INTENT;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.shared.base.activity.BaseActivity;
import arun.com.chromer.tabs.TabsManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebHeadContextActivity extends BaseActivity implements WebsiteAdapter.WebSiteAdapterListener {
  private final WebHeadEventsReceiver webHeadsEventsReceiver = new WebHeadEventsReceiver();
  @BindView(R.id.web_sites_list)
  RecyclerView websiteListView;
  @BindView(R.id.copy_all)
  TextView copyAll;
  @BindView(R.id.share_all)
  TextView shareAll;
  @BindView(R.id.context_activity_card_view)
  CardView rootCardView;
  @Inject
  TabsManager tabsManager;
  private WebsiteAdapter websitesAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);

    if (getIntent() == null || getIntent().getParcelableArrayListExtra(EXTRA_KEY_WEBSITE) == null) {
      finish();
    }
    final ArrayList<Website> websites = getIntent().getParcelableArrayListExtra(EXTRA_KEY_WEBSITE);

    websitesAdapter = new WebsiteAdapter(this, this);
    websitesAdapter.setWebsites(websites);

    websiteListView.setLayoutManager(new LinearLayoutManager(this));
    websiteListView.setAdapter(websitesAdapter);

    registerEventsReceiver();
  }

  @Override
  protected int getLayoutRes() {
    return R.layout.activity_web_head_context;
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
  public void onWebSiteItemClicked(@NonNull Website website) {
    finish();
    tabsManager.openUrl(this, website, true, true, false, false, false);
    if (Preferences.get(this).webHeadsCloseOnOpen()) {
      broadcastDeleteWebHead(website);
    }
  }

  private void broadcastDeleteWebHead(@NonNull Website website) {
    final Intent intent = new Intent(ACTION_CLOSE_WEBHEAD_BY_URL);
    intent.putExtra(EXTRA_KEY_WEBSITE, website);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }

  @Override
  public void onWebSiteDelete(@NonNull final Website website) {
    final boolean shouldFinish = websitesAdapter.getWebsites().isEmpty();
    if (shouldFinish) {
      rootCardView.setVisibility(GONE);
      broadcastDeleteWebHead(website);
      finish();
    } else
      broadcastDeleteWebHead(website);
  }

  @Override
  public void onWebSiteShare(@NonNull Website website) {
    startActivity(Intent.createChooser(TEXT_SHARE_INTENT.putExtra(EXTRA_TEXT, website.url), getString(R.string.share)));
  }

  @Override
  public void onWebSiteLongClicked(@NonNull Website website) {
    copyToClipboard(website.safeLabel(), website.preferredUrl());
  }

  @OnClick(R.id.copy_all)
  public void onCopyAllClick() {
    copyToClipboard("Websites", getCSVUrls().toString());
  }

  @OnClick(R.id.share_all)
  public void onShareAllClick() {
    final CharSequence[] items = new String[]{
      getString(R.string.comma_separated),
      getString(R.string.new_line_separated),
      getString(R.string.share_all_list)
    };
    new MaterialDialog.Builder(this)
      .title(R.string.choose_share_method)
      .items(items)
      .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> {
        if (which == 0) {
          startActivity(Intent.createChooser(TEXT_SHARE_INTENT.putExtra(EXTRA_TEXT, getCSVUrls().toString()), getString(R.string.share_all)));
        } else if (which == 1) {
          startActivity(Intent.createChooser(TEXT_SHARE_INTENT.putExtra(EXTRA_TEXT, getNSVUrls().toString()), getString(R.string.share_all)));
        } else {
          final ArrayList<Uri> webSites = new ArrayList<>();
          for (Website website : websitesAdapter.getWebsites()) {
            try {
              webSites.add(Uri.parse(website.preferredUrl()));
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
      }).show();
  }

  @NonNull
  private StringBuilder getCSVUrls() {
    final StringBuilder builder = new StringBuilder();
    final List<Website> websites = websitesAdapter.getWebsites();
    final int size = websites.size();
    for (int i = 0; i < size; i++) {
      builder.append(websites.get(i).preferredUrl());
      if (i != size - 1) {
        builder.append(',')
          .append(' ');
      }
    }
    return builder;
  }

  @NonNull
  private StringBuilder getNSVUrls() {
    final StringBuilder builder = new StringBuilder();
    final List<Website> websites = websitesAdapter.getWebsites();
    final int size = websites.size();
    for (int i = 0; i < size; i++) {
      builder.append(websites.get(i).preferredUrl());
      if (i != size - 1) {
        builder.append('\n');
      }
    }
    return builder;
  }

  private void copyToClipboard(String label, String url) {
    final ClipData clip = ClipData.newPlainText(label, url);
    final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    cm.setPrimaryClip(clip);
    Toast.makeText(this, getString(R.string.copied) + " " + url, LENGTH_SHORT).show();
  }

  @Override
  public void inject(@NonNull ActivityComponent activityComponent) {
    activityComponent.inject(this);
  }

  /**
   * This receiver is responsible for receiving events from web head service.
   */
  private class WebHeadEventsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case ACTION_EVENT_WEBHEAD_DELETED:
          final Website website = intent.getParcelableExtra(EXTRA_KEY_WEBSITE);
          if (website != null) {
            websitesAdapter.delete(website);
          }
          break;
        case ACTION_EVENT_WEBSITE_UPDATED:
          final Website web = intent.getParcelableExtra(EXTRA_KEY_WEBSITE);
          if (web != null) {
            websitesAdapter.update(web);
          }
          break;
      }
    }
  }
}
