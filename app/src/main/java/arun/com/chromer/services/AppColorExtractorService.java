package arun.com.chromer.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arun.com.chromer.model.AppColor;
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
                // No need to extract for our app or android, exit
                // TODO find all exclusions by testing thoroughly
                if (app.equalsIgnoreCase(getPackageName()) | app.equalsIgnoreCase("android"))
                    return;

                try {
                    Drawable iconDrawable = getPackageManager().getApplicationIcon(app);
                    // Convert to bitmap
                    Bitmap iconBitmap = Util.drawableToBitmap(iconDrawable);

                    Palette palette = null;
                    if (iconBitmap != null)
                        palette = Palette.from(iconBitmap).generate();

                    if (palette == null) return; // No use when there are no colors, so exit

                    int extractColor = getPreferredColorFromSwatches(palette);

                    if (extractColor != -1) {
                        Timber.d("Extracted " + extractColor + " for " + app);
                        try {
                            // Save this color to DB
                            AppColor appColor = new AppColor(app, extractColor);
                            appColor.save();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
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