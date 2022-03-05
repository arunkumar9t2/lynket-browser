/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.settings.browsingmode;

import static com.mikepenz.community_material_typeface_library.CommunityMaterial.Icon;

import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.RxPreferences;
import arun.com.chromer.util.ColorUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import dev.arunkumar.android.dagger.activity.PerActivity;

/**
 * Created by Arunkumar on 19-02-2017.
 */
@PerActivity
class BrowsingModeAdapter extends RecyclerView.Adapter<BrowsingModeAdapter.BrowsingModeViewHolder> {
  public static final int SLIDE_OVER = 0;
  public static final int WEB_HEADS = 1;
  public static final int NATIVE_BUBBLES = 2;
  private final List<String> settingsItems = new ArrayList<>();
  private final RxPreferences rxPreferences;
  private BrowsingModeClickListener browsingModeClickListener = (position, view) -> {
  };

  @Inject
  BrowsingModeAdapter(@NonNull final Application application, final RxPreferences rxPreferences) {
    setHasStableIds(true);
    this.rxPreferences = rxPreferences;
    settingsItems.add(application.getString(R.string.browsing_mode_slide_over));
    settingsItems.add(application.getString(R.string.browsing_mode_web_heads));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      settingsItems.add(application.getString(R.string.browsing_mode_native_bubbles));
    }
  }

  @Override
  public BrowsingModeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new BrowsingModeViewHolder(
      LayoutInflater.from(parent.getContext()).inflate(
        R.layout.activity_browsing_mode_item_template,
        parent,
        false
      ),
      rxPreferences
    );
  }

  @Override
  public void onBindViewHolder(final BrowsingModeViewHolder holder, int position) {
    holder.bind(settingsItems.get(position));
    holder.itemView.setOnClickListener(v -> {
      if (holder.getAdapterPosition() != RecyclerView.NO_POSITION)
        browsingModeClickListener.onModeClicked(holder.getAdapterPosition(), holder.itemView);
    });
  }

  @Override
  public long getItemId(int position) {
    return settingsItems.get(position).hashCode();
  }

  @Override
  public int getItemCount() {
    return settingsItems.size();
  }

  void setBrowsingModeClickListener(@NonNull BrowsingModeClickListener browsingModeClickListener) {
    this.browsingModeClickListener = browsingModeClickListener;
  }

  void cleanUp() {
    browsingModeClickListener = (position, view) -> {
    };
    settingsItems.clear();
  }

  interface BrowsingModeClickListener {
    void onModeClicked(int position, final View view);
  }

  static class BrowsingModeViewHolder extends RecyclerView.ViewHolder {
    private final RxPreferences rxPreferences;
    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.browsing_mode_selector)
    ImageView selector;
    @BindView(R.id.browsing_mode_root)
    CardView browsingModeRoot;

    BrowsingModeViewHolder(View itemView, final RxPreferences rxPreferences) {
      super(itemView);
      this.rxPreferences = rxPreferences;
      ButterKnife.bind(this, itemView);
    }

    void bind(@NonNull final String item) {
      title.setText(item);
      final int position = getAdapterPosition();
      final boolean webHeads = Preferences.get(selector.getContext()).webHeads();
      final boolean nativeBubbles = rxPreferences.getNativeBubbles().get();
      browsingModeRoot.setForeground(ColorUtil.getRippleDrawableCompat(Color.parseColor("#42ffffff")));
      switch (position) {
        case SLIDE_OVER:
          icon.setImageDrawable(new IconicsDrawable(icon.getContext())
            .icon(Icon.cmd_open_in_app)
            .color(Color.WHITE)
            .sizeDp(24));
          selector.setImageDrawable(new IconicsDrawable(selector.getContext())
            .icon(webHeads || nativeBubbles
              ? Icon.cmd_checkbox_blank_circle_outline
              : Icon.cmd_checkbox_marked_circle)
            .color(Color.WHITE)
            .sizeDp(24));
          title.setTextColor(Color.WHITE);
          subtitle.setTextColor(Color.WHITE);
          subtitle.setText(R.string.browsing_mode_slide_over_explanation);
          browsingModeRoot.setCardBackgroundColor(ContextCompat.getColor(browsingModeRoot.getContext(), R.color.md_light_blue_A700));
          break;
        case WEB_HEADS:
          icon.setImageDrawable(new IconicsDrawable(icon.getContext())
            .icon(Icon.cmd_chart_bubble)
            .color(Color.WHITE)
            .sizeDp(24));
          selector.setImageDrawable(new IconicsDrawable(selector.getContext())
            .icon(!webHeads
              ? Icon.cmd_checkbox_blank_circle_outline
              : Icon.cmd_checkbox_marked_circle)
            .color(Color.WHITE)
            .sizeDp(24));
          title.setTextColor(Color.WHITE);
          subtitle.setTextColor(Color.WHITE);
          subtitle.setText(R.string.browsing_mode_web_heads_explanation);
          browsingModeRoot.setCardBackgroundColor(ContextCompat.getColor(browsingModeRoot.getContext(), R.color.md_green_700));
          break;
        case NATIVE_BUBBLES:
          final int materialDarkColor = ContextCompat.getColor(icon.getContext(), R.color.material_dark_color);
          icon.setImageDrawable(new IconicsDrawable(icon.getContext())
            .icon(Icon.cmd_android_head)
            .color(materialDarkColor)
            .sizeDp(24));
          selector.setImageDrawable(new IconicsDrawable(selector.getContext())
            .icon(!nativeBubbles
              ? Icon.cmd_checkbox_blank_circle_outline
              : Icon.cmd_checkbox_marked_circle)
            .color(materialDarkColor)
            .sizeDp(24));
          title.setTextColor(materialDarkColor);
          subtitle.setTextColor(ColorUtils.setAlphaComponent(materialDarkColor, (int) (0.8 * 255)));
          subtitle.setText(R.string.browsing_mode_native_bubbles_explanation);
          browsingModeRoot.setCardBackgroundColor(ContextCompat.getColor(browsingModeRoot.getContext(), R.color.android_10_color));
          break;
      }
    }
  }
}
