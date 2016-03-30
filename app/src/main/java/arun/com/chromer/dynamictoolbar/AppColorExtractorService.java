package arun.com.chromer.dynamictoolbar;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arun.com.chromer.db.AppColor;
import arun.com.chromer.util.Util;
import timber.log.Timber;


public class AppColorExtractorService extends IntentService {
    public AppColorExtractorService() {
        super(AppColorExtractorService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String app = intent.getStringExtra("app");
            if (app != null) {
                // TODO find all exclusions by testing thoroughly
                if (app.equalsIgnoreCase(getPackageName()) | app.equalsIgnoreCase("android"))
                    return;

                if (extractColorFromResources(app)) {
                    Timber.d("Successful extraction from resources");
                } else extractColorFromAppIcon(app);
            }
        }
    }

    private boolean extractColorFromResources(String app) {
        try {
            int color;

            Resources resources = getPackageManager().getResourcesForApplication(app);

            // Try to extract appcompat primary color value
            int appCompatId = resources.getIdentifier("colorPrimary", "attr", app);
            if (appCompatId > 0) {
                // Successful, let's get the themed value of this attribute
                color = getThemedColor(resources, appCompatId, app);
                if (color != -1) {
                    saveColorToDb(app, color);
                    return true;
                }
            }

            // If above was not successful, then attempt to get lollipop colorPrimary attribute
            int lollipopAttrId = resources.getIdentifier("android:colorPrimary", "attr", app);
            if (lollipopAttrId > 0) {
                // Found
                color = getThemedColor(resources, lollipopAttrId, app);
                if (color != -1) {
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

    private int getThemedColor(Resources resources, int attributeId, String app) throws PackageManager.NameNotFoundException {
        if (resources == null || attributeId == 0 || app == null) return -1;

        // Create dummy theme
        Resources.Theme tempTheme = resources.newTheme();
        // Need the theme id to apply the theme, so let's get it.
        int themeId = getPackageManager().getPackageInfo(app, PackageManager.GET_META_DATA).applicationInfo.theme;
        // Apply the theme
        tempTheme.applyStyle(themeId, false);

        // Attempt to get styled values now
        TypedArray array = tempTheme.obtainStyledAttributes(new int[]{attributeId});

        // Styled color
        int color = array.getColor(0, -1);

        array.recycle();
        return color;
    }

    private void extractColorFromAppIcon(String app) {
        try {
            Drawable iconDrawable = getPackageManager().getApplicationIcon(app);
            // Convert to bitmap
            Bitmap iconBitmap = Util.drawableToBitmap(iconDrawable);

            Palette palette = Palette.from(iconBitmap).generate();

            //noinspection ConstantConditions
            if (palette == null) return; // No use when there are no colors, so exit

            int extractColor = getPreferredColorFromSwatches(palette);

            if (extractColor != -1) {
                Timber.d("Extracted %d for %s", extractColor, app);
                try {
                    // Save this color to DB
                    saveColorToDb(app, extractColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveColorToDb(String app, int extractColor) {
        AppColor appColor = new AppColor(app, extractColor);
        appColor.save();
    }

    private int getPreferredColorFromSwatches(Palette palette) {
        List<Palette.Swatch> swatchList = getSwatchList(palette);
        Palette.Swatch prominentSwatch = Collections.max(swatchList,
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

    private List<Palette.Swatch> getSwatchList(Palette palette) {
        List<Palette.Swatch> swatchList = new ArrayList<>();

        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        Palette.Swatch vibrantDarkSwatch = palette.getDarkVibrantSwatch();
        Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        Palette.Swatch mutedDarkSwatch = palette.getDarkMutedSwatch();
        Palette.Swatch mutedLightSwatch = palette.getLightMutedSwatch();

        swatchList.add(vibrantSwatch);
        swatchList.add(vibrantDarkSwatch);
        swatchList.add(vibrantLightSwatch);
        swatchList.add(mutedSwatch);
        swatchList.add(mutedDarkSwatch);
        swatchList.add(mutedLightSwatch);
        return swatchList;
    }
}