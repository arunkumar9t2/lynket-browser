package arun.com.chromer.preferences.widgets;

import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import timber.log.Timber;

/**
 * Created by Arun on 23/06/2016.
 */
class IconLayoutHelper {

    private IconLayoutHelper() {
        throw new AssertionError();
    }

    public static void applyLayoutChanges(@NonNull PreferenceViewHolder holder) {
        try {
            LinearLayout iconFrame = (LinearLayout) holder.findViewById(android.support.v7.preference.R.id.icon_frame);
            iconFrame.setMinimumWidth(0);
            ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
    }
}
