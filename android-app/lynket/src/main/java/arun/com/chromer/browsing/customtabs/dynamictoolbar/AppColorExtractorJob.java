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

package arun.com.chromer.browsing.customtabs.dynamictoolbar;

import static android.content.pm.PackageManager.GET_META_DATA;
import static arun.com.chromer.shared.Constants.EXTRA_PACKAGE_NAME;
import static arun.com.chromer.shared.Constants.NO_COLOR;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.Lynket;
import arun.com.chromer.R;
import arun.com.chromer.data.apps.AppRepository;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;
import timber.log.Timber;


public class AppColorExtractorJob extends JobIntentService {

  public static final int JOB_ID = 112;

  @Inject
  AppRepository appRepository;

  public AppColorExtractorJob() {
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    ((Lynket) getApplication()).getAppComponent().inject(this);
    final String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
    if (packageName != null) {
      if (isValidPackage(packageName))
        return;
      if (!extractColorFromResources(packageName)) {
        extractColorFromAppIcon(packageName);
      }
    }
  }

  private boolean extractColorFromResources(@NonNull final String packageName) {
    try {
      int color;
      final Resources resources = getPackageManager().getResourcesForApplication(packageName);
      // Try to extract appcompat primary color value
      int appCompatId = resources.getIdentifier("colorPrimary", "attr", packageName);
      if (appCompatId > 0) {
        // Successful, let's get the themed value of this attribute
        color = getThemedColor(resources, appCompatId, packageName);
        if (color != NO_COLOR) {
          saveColorToDb(packageName, color);
          return true;
        }
      }
      // If above was not successful, then attempt to get lollipop colorPrimary attribute
      int lollipopAttrId = resources.getIdentifier("android:colorPrimary", "attr", packageName);
      if (lollipopAttrId > 0) {
        // Found
        color = getThemedColor(resources, lollipopAttrId, packageName);
        if (color != NO_COLOR) {
          saveColorToDb(packageName, color);
          return true;
        }
      }
      return false;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return false;
    }
  }

  private int getThemedColor(@Nullable Resources resources, int attributeId, @NonNull String packageName)
    throws PackageManager.NameNotFoundException {
    if (resources == null || attributeId == 0) return -1;
    // Create dummy theme
    final Resources.Theme tempTheme = resources.newTheme();
    // Need the theme id to apply the theme, so let's get it.
    int themeId = getPackageManager().getPackageInfo(packageName, GET_META_DATA).applicationInfo.theme;
    // Apply the theme
    tempTheme.applyStyle(themeId, false);
    // Attempt to get styled values now
    final TypedArray array = tempTheme.obtainStyledAttributes(new int[]{attributeId});
    // Styled color
    int color = array.getColor(0, NO_COLOR);
    array.recycle();
    if (color == ContextCompat.getColor(this, R.color.md_grey_100)
      || color == ContextCompat.getColor(this, R.color.md_grey_900)) {
      color = NO_COLOR;
    }
    return color;
  }

  private void extractColorFromAppIcon(@NonNull final String packageName) {
    try {
      final Bitmap iconBitmap = Utils.drawableToBitmap(getPackageManager().getApplicationIcon(packageName));
      final Palette palette = Palette.from(iconBitmap)
        .clearFilters()
        .generate();
      int extractColor = getPreferredColorFromSwatches(palette);
      if (extractColor != NO_COLOR) {
        Timber.d("Extracted %d for %s", extractColor, packageName);
        try {
          saveColorToDb(packageName, extractColor);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private boolean isValidPackage(String app) {
    return app.equalsIgnoreCase(getPackageName()) || app.equalsIgnoreCase("android") || app.isEmpty();
  }

  private int getPreferredColorFromSwatches(Palette palette) {
    final List<Palette.Swatch> swatchList = ColorUtil.getSwatchListFromPalette(palette);
    final Palette.Swatch prominentSwatch = Collections.max(swatchList,
      (swatch1, swatch2) -> {
        int a = swatch1 == null ? 0 : swatch1.getPopulation();
        int b = swatch2 == null ? 0 : swatch2.getPopulation();
        return a - b;
      });
    if (prominentSwatch != null)
      return prominentSwatch.getRgb();
    else return -1;
  }

  private void saveColorToDb(final String packageName, @ColorInt int extractedColor) {
    appRepository.setPackageColor(packageName, extractedColor).subscribe();
  }

}
