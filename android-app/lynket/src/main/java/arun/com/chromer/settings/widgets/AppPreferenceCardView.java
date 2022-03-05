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

package arun.com.chromer.settings.widgets;

import static android.widget.ImageView.ScaleType.CENTER;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;
import arun.com.chromer.util.glide.GlideApp;
import arun.com.chromer.util.glide.appicon.ApplicationIcon;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AppPreferenceCardView extends CardView {
  private static final int CUSTOM_TAB_PROVIDER = 0;
  private static final int SECONDARY_BROWSER = 1;
  private static final int FAVORITE_SHARE = 2;

  @BindView(R.id.app_preference_icon)
  public ImageView icon;
  @BindView(R.id.app_preference_category)
  public TextView categoryTextView;
  @BindView(R.id.app_preference_selection)
  public TextView appNameTextView;

  private Unbinder unbinder;

  private String category;
  private String appName;
  private String appPackage;
  private int preferenceType;

  public AppPreferenceCardView(Context context) {
    super(context);
    init(null, 0);
  }

  public AppPreferenceCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  public AppPreferenceCardView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs, defStyle);
  }

  private void init(AttributeSet attrs, int defStyle) {
    // Load attributes
    final TypedArray a = getContext().obtainStyledAttributes(
      attrs, R.styleable.AppPreferenceCardView, defStyle, 0);
    if (!a.hasValue(R.styleable.AppPreferenceCardView_preferenceType)) {
      throw new IllegalArgumentException("Must specify app:preferenceType in xml");
    }

    preferenceType = a.getInt(R.styleable.AppPreferenceCardView_preferenceType, 0);
    setInitialValues();
    a.recycle();
    addView(LayoutInflater.from(getContext()).inflate(R.layout.widget_app_preference_cardview_content, this, false));
    unbinder = ButterKnife.bind(this);
  }

  private void setInitialValues() {
    switch (preferenceType) {
      case CUSTOM_TAB_PROVIDER:
        category = getResources().getString(R.string.default_provider);
        final String customTabProvider = Preferences.get(getContext()).customTabPackage();
        if (customTabProvider != null) {
          appName = Utils.getAppNameWithPackage(getContext(), customTabProvider);
          appPackage = customTabProvider;
        } else {
          appName = getResources().getString(R.string.not_found);
          appPackage = null;
        }
        break;
      case SECONDARY_BROWSER:
        category = getResources().getString(R.string.choose_secondary_browser);
        final String secondaryBrowser = Preferences.get(getContext()).secondaryBrowserPackage();
        if (secondaryBrowser != null) {
          appName = Utils.getAppNameWithPackage(getContext(), secondaryBrowser);
          appPackage = secondaryBrowser;
        } else {
          appName = getResources().getString(R.string.not_set);
          appPackage = null;
        }
        break;
      case FAVORITE_SHARE:
        category = getResources().getString(R.string.fav_share_app);
        final String favSharePackage = Preferences.get(getContext()).favSharePackage();
        if (favSharePackage != null) {
          appName = Utils.getAppNameWithPackage(getContext(), favSharePackage);
          appPackage = favSharePackage;
        } else {
          appName = getResources().getString(R.string.not_set);
          appPackage = null;
        }
        break;
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    updateUI();
  }

  private void updateUI() {
    appNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.material_dark_color));
    categoryTextView.setText(category);
    appNameTextView.setText(appName);
    applyIcon();
  }

  private void applyIcon() {
    if (Utils.isPackageInstalled(getContext(), appPackage)) {
      icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
      GlideApp.with(getContext())
        .load(ApplicationIcon.Companion.createUri(appPackage))
        .fitCenter()
        .listener(new RequestListener<Drawable>() {
          @Override
          public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
          }

          @Override
          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            Palette.from(Utils.drawableToBitmap(resource))
              .clearFilters()
              .generate(palette -> {
                int bestColor = ColorUtil.getBestColorFromPalette(palette);
                final Drawable foreground = ColorUtil.getRippleDrawableCompat(bestColor);
                AppPreferenceCardView.this.setForeground(foreground);
              });
            return false;
          }
        })
        .into(icon);
    } else {
      icon.setScaleType(CENTER);
      switch (preferenceType) {
        case CUSTOM_TAB_PROVIDER:
          icon.setImageDrawable(new IconicsDrawable(getContext())
            .icon(CommunityMaterial.Icon.cmd_comment_alert_outline)
            .colorRes(R.color.error)
            .sizeDp(30));
          appNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
          break;
        case SECONDARY_BROWSER:
          icon.setImageDrawable(new IconicsDrawable(getContext())
            .icon(CommunityMaterial.Icon.cmd_open_in_app)
            .colorRes(R.color.material_dark_light)
            .sizeDp(30));
          break;
        case FAVORITE_SHARE:
          icon.setImageDrawable(new IconicsDrawable(getContext())
            .icon(CommunityMaterial.Icon.cmd_share_variant)
            .colorRes(R.color.material_dark_light)
            .sizeDp(30));
          break;
      }
    }
  }

  public void updatePreference(@Nullable final ComponentName componentName) {
    final String flatComponent = componentName == null ? null : componentName.flattenToString();
    switch (preferenceType) {
      case CUSTOM_TAB_PROVIDER:
        if (componentName != null) {
          Preferences.get(getContext()).customTabPackage(componentName.getPackageName());
        }
        break;
      case SECONDARY_BROWSER:
        Preferences.get(getContext()).secondaryBrowserComponent(flatComponent);
        break;
      case FAVORITE_SHARE:
        Preferences.get(getContext()).favShareComponent(flatComponent);
        break;
    }
    refreshState();
  }

  public void refreshState() {
    setInitialValues();
    updateUI();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    unbinder.unbind();
  }
}
