package arun.com.chromer.activities.settings.preferences.widgets;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mikepenz.iconics.IconicsDrawable;

import timber.log.Timber;

/**
 * Created by Arun on 23/06/2016.
 */
class PreferenceIconLayoutHelper {

    @ColorInt
    private static final int CHECKED_COLOR = Color.parseColor("#757575");
    @ColorInt
    private static final int UNCHECKED_COLOR = Color.parseColor("#C5C5C5");

    private PreferenceIconLayoutHelper() {
        throw new AssertionError();
    }

    /**
     * Applies layout changes on the preference icon view so that it does not look overly big.
     *
     * @param holder  the preference view holder
     * @param checked if the preference is enabled
     */
    static void applyLayoutChanges(@NonNull PreferenceViewHolder holder, boolean checked) {
        try {
            final LinearLayout iconFrame = (LinearLayout) holder.findViewById(android.support.v7.preference.R.id.icon_frame);
            final ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);

            if (iconFrame.getMinimumWidth() != 0) {
                iconFrame.setMinimumWidth(0);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            applyIconTint(imageView, checked);
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
    }

    /**
     * Finds the icon view and attempts to tint it based on the checked state of the preference
     *
     * @param imageView The image view of icon
     * @param checked   Whether the preference is enabled
     */
    private static void applyIconTint(@NonNull ImageView imageView, boolean checked) {
        final Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            if (drawable instanceof IconicsDrawable) {
                // Just redraw with the correct color
                imageView.setImageDrawable(((IconicsDrawable) drawable).color(checked ? CHECKED_COLOR : UNCHECKED_COLOR));
            } else {
                final Drawable wrap = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrap, checked ? CHECKED_COLOR : UNCHECKED_COLOR);
                imageView.setImageDrawable(drawable);
            }
        }
    }
}
