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

package arun.com.chromer.settings;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.settings.browsingmode.BrowsingModeActivity;
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity;
import arun.com.chromer.settings.lookandfeel.LookAndFeelActivity;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.shared.base.activity.SubActivity;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsGroupActivity extends SubActivity implements SettingsGroupAdapter.GroupItemClickListener {
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.settings_list_view)
  RecyclerView settingsListView;
  @BindView(R.id.set_default_card)
  CardView setDefaultCard;
  @BindView(R.id.set_default_image)
  ImageView setDefaultImage;
  private SettingsGroupAdapter adapter;
  private BroadcastReceiver closeReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    adapter = new SettingsGroupAdapter(this);
    settingsListView.setLayoutManager(new LinearLayoutManager(this));
    settingsListView.setAdapter(adapter);
    settingsListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    adapter.setGroupItemClickListener(this);

    setDefaultImage.setImageDrawable(new IconicsDrawable(this)
      .icon(CommunityMaterial.Icon.cmd_auto_fix)
      .color(Color.WHITE)
      .sizeDp(24));

    setDefaultCard.setOnClickListener(v -> {
      final String defaultBrowser = Utils.getDefaultBrowserPackage(getApplicationContext());
      if (defaultBrowser.equalsIgnoreCase("android")
        || defaultBrowser.startsWith("org.cyanogenmod")
        || defaultBrowser.equalsIgnoreCase("com.huawei.android.internal.app")) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_URL)));
      } else {
        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + defaultBrowser));
        Toast.makeText(SettingsGroupActivity.this,
          Utils.getAppNameWithPackage(getApplicationContext(), defaultBrowser)
            + " "
            + getString(R.string.default_clear_msg), Toast.LENGTH_LONG).show();
        startActivity(intent);
      }
    });
    updateDefaultBrowserCard();
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateDefaultBrowserCard();
  }

  @Override
  protected void onDestroy() {
    adapter.cleanUp();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
    super.onDestroy();
  }

  private void updateDefaultBrowserCard() {
    if (!Utils.isDefaultBrowser(this)) {
      setDefaultCard.setVisibility(View.VISIBLE);
    } else
      setDefaultCard.setVisibility(View.GONE);
  }

  @Override
  public void onGroupItemClicked(int position, View view) {
    switch (position) {
      case 0:
        startActivity(new Intent(this, BrowsingModeActivity.class));
        break;
      case 1:
        startActivity(new Intent(this, LookAndFeelActivity.class));
        break;
      case 2:
        startActivity(new Intent(this, BrowsingOptionsActivity.class));
        break;
    }
  }
}
