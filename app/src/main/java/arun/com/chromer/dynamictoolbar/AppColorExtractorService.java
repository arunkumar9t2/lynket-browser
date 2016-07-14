package arun.com.chromer.dynamictoolbar;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.db.AppColor;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Util;
import timber.log.Timber;


public class AppColorExtractorService extends IntentService {

    public AppColorExtractorService() {
        super(AppColorExtractorService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String app = intent.getStringExtra("app");
            if (app != null) {
                if (app.equalsIgnoreCase(getPackageName())
                        | app.equalsIgnoreCase("android")
                        | app.isEmpty())
                    return;

                if (extractColorFromResources(app)) {
                } else extractColorFromAppIcon(app);
            }
        }
    }

    private boolean extractColorFromResources(final String app) {
        try {
            int color;

            final Resources resources = getPackageManager().getResourcesForApplication(app);
            // Try to extract appcompat primary color value
            int appCompatId = resources.getIdentifier("colorPrimary", "attr", app);
            if (appCompatId > 0) {
                // Successful, let's get the themed value of this attribute
                color = getThemedColor(resources, appCompatId, app);
                if (color != Constants.NO_COLOR) {
                    saveColorToDb(app, color);
                    return true;
                }
            }
            // If above was not successful, then attempt to get lollipop colorPrimary attribute
            int lollipopAttrId = resources.getIdentifier("android:colorPrimary", "attr", app);
            if (lollipopAttrId > 0) {
                // Found
                color = getThemedColor(resources, lollipopAttrId, app);
                if (color != Constants.NO_COLOR) {
                    saveColorToDb(app, color);
                    return true;
                }
            }
            // If we reached here, then both attempt failed
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getThemedColor(@Nullable Resources resources, int attributeId, @Nullable String app) throws PackageManager.NameNotFoundException {
        if (resources == null || attributeId == 0 || app == null) return -1;

        // Create dummy theme
        final Resources.Theme tempTheme = resources.newTheme();
        // Need the theme id to apply the theme, so let's get it.
        int themeId = getPackageManager().getPackageInfo(app, PackageManager.GET_META_DATA).applicationInfo.theme;
        // Apply the theme
        tempTheme.applyStyle(themeId, false);
        // Attempt to get styled values now
        final TypedArray array = tempTheme.obtainStyledAttributes(new int[]{attributeId});
        // Styled color
        int color = array.getColor(0, Constants.NO_COLOR);
        array.recycle();
        if (color == ContextCompat.getColor(this, R.color.md_grey_100)
                || color == ContextCompat.getColor(this, R.color.md_grey_900)) {
            color = Constants.NO_COLOR;
        }
        return color;
    }

    private void extractColorFromAppIcon(final String app) {
        try {
            final Bitmap iconBitmap = Util.drawableToBitmap(getPackageManager().getApplicationIcon(app));
            final Palette palette = Palette.from(iconBitmap)
                    .clearFilters()
                    .generate();

            //noinspection ConstantConditions
            int extractColor = getPreferredColorFromSwatches(palette);
            if (extractColor != Constants.NO_COLOR) {
                Timber.d("Extracted %d for %s", extractColor, app);
                try {
                    saveColorToDb(app, extractColor);
                    iconBitmap.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveColorToDb(final String app, @ColorInt int extractColor) {
        final AppColor appColor = new AppColor(app, extractColor);
        appColor.save();
    }

    private int getPreferredColorFromSwatches(Palette palette) {
        final List<Palette.Swatch> swatchList = ColorUtil.getSwatchListFromPalette(palette);
        final Palette.Swatch prominentSwatch = Collections.max(swatchList,
                new Comparator<Palette.Swatch>() {
                    @Override
                    public int compare(Palette.Swatch swatch1, Palette.Swatch swatch2) {
                        int a = swatch1 == null ? 0 : swatch1.getPopulation();
                        int b = swatch2 == null ? 0 : swatch2.getPopulation();
                        return a - b;
                    }
                });
        if (prominentSwatch != null)
            return prominentSwatch.getRgb();
        else return -1;
    }

}