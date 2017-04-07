/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer.util;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Created by Arun on 12/06/2016.
 */
public class ColorUtil {

    public final static int[] ACCENT_COLORS = new int[]{
            Color.parseColor("#FF1744"),
            Color.parseColor("#F50057"),
            Color.parseColor("#D500F9"),
            Color.parseColor("#651FFF"),
            Color.parseColor("#3D5AFE"),
            Color.parseColor("#2979FF"),
            Color.parseColor("#00B0FF"),
            Color.parseColor("#00E5FF"),
            Color.parseColor("#1DE9B6"),
            Color.parseColor("#00E676"),
            Color.parseColor("#76FF03"),
            Color.parseColor("#C6FF00"),
            Color.parseColor("#FFEA00"),
            Color.parseColor("#FFC400"),
            Color.parseColor("#FF9100"),
            Color.parseColor("#FF3D00")
    };

    private final static int[] ACCENT_COLORS_700 = new int[]{
            Color.parseColor("#D32F2F"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#7B1FA2"),
            Color.parseColor("#6200EA"),
            Color.parseColor("#304FFE"),
            Color.parseColor("#2962FF"),
            Color.parseColor("#0091EA"),
            Color.parseColor("#00B8D4"),
            Color.parseColor("#00BFA5"),
            Color.parseColor("#00C853"),
            Color.parseColor("#64DD17"),
            Color.parseColor("#AEEA00"),
            Color.parseColor("#FFD600"),
            Color.parseColor("#FFAB00"),
            Color.parseColor("#FF6D00"),
            Color.parseColor("#DD2C00"),
            Color.parseColor("#455A64")
    };

    @NonNull
    public static List<Palette.Swatch> getSwatchListFromPalette(@NonNull Palette palette) {
        final List<Palette.Swatch> swatchList = new LinkedList<>();
        Palette.Swatch prominentSwatch = palette.getDominantSwatch();
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        Palette.Swatch vibrantDarkSwatch = palette.getDarkVibrantSwatch();
        Palette.Swatch vibrantLightSwatch = palette.getLightVibrantSwatch();
        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        Palette.Swatch mutedDarkSwatch = palette.getDarkMutedSwatch();
        Palette.Swatch mutedLightSwatch = palette.getLightMutedSwatch();

        swatchList.add(prominentSwatch);
        swatchList.add(vibrantSwatch);
        swatchList.add(vibrantDarkSwatch);
        swatchList.add(vibrantLightSwatch);
        swatchList.add(mutedSwatch);
        swatchList.add(mutedDarkSwatch);
        swatchList.add(mutedLightSwatch);
        return swatchList;
    }

    private static double colorDifference(@ColorInt int a, @ColorInt int b) {
        double aLab[] = new double[3];
        double bLab[] = new double[3];
        ColorUtils.colorToLAB(a, aLab);
        ColorUtils.colorToLAB(b, bLab);
        return ColorUtils.distanceEuclidean(aLab, bLab);
    }

    @ColorInt
    public static int getClosestAccentColor(@ColorInt int color) {
        final SortedMap<Double, Integer> set = new TreeMap<>();
        color = (0xFFFFFF - color) | 0xFF000000;
        for (int i = 0; i < ACCENT_COLORS_700.length; i++) {
            set.put(colorDifference(color, ACCENT_COLORS_700[i]), i);
        }
        return ACCENT_COLORS_700[set.get(set.firstKey())];
    }

    @ColorInt
    public static int getBestFaviconColor(@Nullable Palette palette) {
        if (palette != null) {
            final List<Palette.Swatch> sortedSwatch = getSwatchListFromPalette(palette);
            // We want the vibrant color but we will avoid it if it is the most prominent one.
            // Instead we will choose the next prominent color
            int vibrantColor = palette.getVibrantColor(NO_COLOR);
            int prominentColor = sortedSwatch.get(0) != null ? sortedSwatch.get(0).getRgb() : NO_COLOR;
            if (vibrantColor == NO_COLOR) {
                int darkVibrantColor = palette.getDarkVibrantColor(NO_COLOR);
                if (darkVibrantColor != NO_COLOR) {
                    return darkVibrantColor;
                } else {
                    int mutedColor = palette.getMutedColor(NO_COLOR);
                    if (mutedColor != NO_COLOR) {
                        return mutedColor;
                    } else {
                        return prominentColor;
                    }
                }
            } else return vibrantColor;
        }
        return NO_COLOR;
    }

    /**
     * Returns white or black based on color luminance
     *
     * @param backgroundColor the color to get foreground for
     * @return White for darker colors and black for ligher colors
     */
    @ColorInt
    public static int getForegroundWhiteOrBlack(@ColorInt int backgroundColor) {
        double l = ColorUtils.calculateLuminance(backgroundColor);
        if (l > 0.179) {
            return Color.BLACK;
        } else
            return Color.WHITE;
    }


    @ColorInt
    public static int getBestColorFromPalette(@Nullable Palette palette) {
        if (palette == null) {
            return NO_COLOR;
        }
        int vibrantColor = palette.getVibrantColor(NO_COLOR);
        if (vibrantColor != NO_COLOR) {
            return vibrantColor;
        } else {
            int darkVibrantColor = palette.getDarkVibrantColor(NO_COLOR);
            if (darkVibrantColor != NO_COLOR) {
                return darkVibrantColor;
            } else {
                return palette.getDarkMutedColor(NO_COLOR);
            }
        }
    }

    @NonNull
    public static Drawable getRippleDrawableCompat(final @ColorInt int color) {
        if (Utils.isLollipopAbove()) {
            return new RippleDrawable(ColorStateList.valueOf(color),
                    null,
                    null
            );
        }
        int translucentColor = ColorUtils.setAlphaComponent(color, 0x44);
        StateListDrawable stateListDrawable = new StateListDrawable();
        int[] states = new int[]{android.R.attr.state_pressed};
        stateListDrawable.addState(states, new ColorDrawable(translucentColor));
        return stateListDrawable;
    }
}
