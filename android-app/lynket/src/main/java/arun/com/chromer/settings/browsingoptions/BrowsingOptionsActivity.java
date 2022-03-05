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

package arun.com.chromer.settings.browsingoptions;

import static arun.com.chromer.shared.Constants.TEXT_SHARE_INTENT;
import static arun.com.chromer.shared.Constants.WEB_INTENT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.browsing.providerselection.ProviderSelectionActivity;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.widgets.AppPreferenceCardView;
import arun.com.chromer.shared.base.Snackable;
import arun.com.chromer.shared.base.activity.BaseActivity;
import arun.com.chromer.shared.views.IntentPickerSheetView;
import arun.com.chromer.util.HtmlCompat;
import arun.com.chromer.util.RxEventBus;
import arun.com.chromer.util.ServiceManager;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrowsingOptionsActivity extends BaseActivity implements Snackable, SharedPreferences.OnSharedPreferenceChangeListener {
  @BindView(R.id.bottom_bar_action_list)
  public RecyclerView recyclerView;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.coordinatorLayout)
  CoordinatorLayout coordinatorLayout;
  @BindView(R.id.customtab_preference_view)
  AppPreferenceCardView customTabPreferenceView;
  @BindView(R.id.browser_preference_view)
  AppPreferenceCardView browserPreferenceView;
  @BindView(R.id.favshare_preference_view)
  AppPreferenceCardView favSharePreferenceView;
  @BindView(R.id.bottomsheet)
  BottomSheetLayout bottomSheetLayout;
  @BindView(R.id.error)
  TextView error;
  @Inject
  RxEventBus eventBus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.behaviour_fragment_container, BehaviorPreferenceFragment.newInstance())
      .replace(R.id.web_head_fragment_container, WebHeadOptionsFragment.newInstance())
      .replace(R.id.bottom_bar_preference_fragment_container, BottomBarPreferenceFragment.Companion.newInstance())
      .commit();
    initBottomActions();
  }

  @Override
  protected int getLayoutRes() {
    return R.layout.activity_browsing_options;
  }

  @Override
  protected void onResume() {
    super.onResume();
    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    showHideErrorView();
    customTabPreferenceView.refreshState();
  }

  private void initBottomActions() {
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(new BottomActionsAdapter(this));
  }

  private void showHideErrorView() {
    if (!Preferences.get(this).webHeads()) {
      error.setVisibility(View.VISIBLE);
    } else {
      error.setVisibility(View.GONE);
    }
  }


  @Override
  protected void onPause() {
    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @OnClick({R.id.customtab_preference_view, R.id.browser_preference_view, R.id.favshare_preference_view})
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.customtab_preference_view:
        startActivity(new Intent(this, ProviderSelectionActivity.class));
        break;
      case R.id.browser_preference_view:
        final IntentPickerSheetView browserPicker = new IntentPickerSheetView(this,
          WEB_INTENT,
          R.string.choose_secondary_browser,
          activityInfo -> {
            bottomSheetLayout.dismissSheet();
            browserPreferenceView.updatePreference(activityInfo.componentName);
            snack(String.format(getString(R.string.secondary_browser_success), activityInfo.label));
          });
        browserPicker.setFilter(IntentPickerSheetView.selfPackageExcludeFilter(this));
        showPicker(browserPicker);
        break;
      case R.id.favshare_preference_view:
        final IntentPickerSheetView favSharePicker = new IntentPickerSheetView(this,
          TEXT_SHARE_INTENT,
          R.string.choose_fav_share_app,
          activityInfo -> {
            bottomSheetLayout.dismissSheet();
            favSharePreferenceView.updatePreference(activityInfo.componentName);
            snack(String.format(getString(R.string.fav_share_success), activityInfo.label));
          });
        favSharePicker.setFilter(IntentPickerSheetView.selfPackageExcludeFilter(this));
        showPicker(favSharePicker);
        break;
    }
  }

  private void refreshCustomTabBindings() {
    ServiceManager.refreshCustomTabBindings(getApplicationContext());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (Preferences.WEB_HEAD_ENABLED.equalsIgnoreCase(key)) {
      showHideErrorView();
    }
  }

  private void showPicker(final IntentPickerSheetView browserPicker) {
    new Handler().postDelayed(() -> bottomSheetLayout.showWithSheetView(browserPicker), 150);
  }

  @Override
  public void snack(@NonNull String message) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void snackLong(@NonNull String message) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void inject(@NonNull ActivityComponent activityComponent) {
    activityComponent.inject(this);
  }

  public static class ProviderChanged {
  }

  public static class BottomActionsAdapter extends RecyclerView.Adapter<BottomActionsAdapter.BottomActionHolder> {
    private static final String NEW_TAB = "NEW_TAB";
    private static final String SHARE = "SHARE";
    private static final String MINIMIZE = "MINIMIZE";
    private static final String ARTICLE = "ARTICLE";
    private static final String TABS = "TABS";
    private final Context context;
    private final List<String> items = new LinkedList<>();

    public BottomActionsAdapter(Context context) {
      this.context = context;
      items.add(NEW_TAB);
      items.add(SHARE);
      if (Utils.ANDROID_LOLLIPOP) {
        items.add(TABS);
        items.add(MINIMIZE);
      }
      items.add(ARTICLE);
    }

    @Override
    public BottomActionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new BottomActionHolder(LayoutInflater.from(context).inflate(R.layout.activity_browsing_option_bottom_actions_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(BottomActionHolder holder, int position) {
      final int iconColor = ContextCompat.getColor(context, R.color.colorAccentLighter);
      switch (items.get(position)) {
        case NEW_TAB:
          holder.icon.setImageDrawable(new IconicsDrawable(context)
            .icon(CommunityMaterial.Icon.cmd_plus)
            .color(iconColor)
            .sizeDp(18));
          holder.action.setText(HtmlCompat.fromHtml(context.getString(R.string.new_tab_action_explanation)));
          break;
        case SHARE:
          holder.icon.setImageDrawable(new IconicsDrawable(context)
            .icon(CommunityMaterial.Icon.cmd_share_variant)
            .color(iconColor)
            .sizeDp(18));
          holder.action.setText(HtmlCompat.fromHtml(context.getString(R.string.share_action_explanation)));
          break;
        case MINIMIZE:
          holder.icon.setImageDrawable(new IconicsDrawable(context)
            .icon(CommunityMaterial.Icon.cmd_arrow_down)
            .color(iconColor)
            .sizeDp(18));
          holder.action.setText(HtmlCompat.fromHtml(context.getString(R.string.minimize_action_explanation)));
          break;
        case ARTICLE:
          holder.icon.setImageDrawable(new IconicsDrawable(context)
            .icon(CommunityMaterial.Icon.cmd_file_document)
            .color(iconColor)
            .sizeDp(18));
          holder.action.setText(HtmlCompat.fromHtml(context.getString(R.string.bottom_bar_article_mode_explanation)));
          break;
        case TABS:
          holder.icon.setImageDrawable(new IconicsDrawable(context)
            .icon(CommunityMaterial.Icon.cmd_view_agenda)
            .color(iconColor)
            .sizeDp(18));
          holder.action.setText(HtmlCompat.fromHtml(context.getString(R.string.bottom_bar_tabs_explanation)));
          break;
      }
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    static class BottomActionHolder extends RecyclerView.ViewHolder {
      @BindView(R.id.bottom_action)
      TextView action;
      @BindView(R.id.bottom_icon)
      ImageView icon;

      BottomActionHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
      }
    }
  }
}
