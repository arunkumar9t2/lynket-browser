package arun.com.chromer.preferences.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import timber.log.Timber;

/**
 * A helper preference view without weird margin on preference icon. Attempts to alter properties on
 * icon frame and icon itself so that it looks better
 */
public class IconSwitchPreference extends SwitchPreferenceCompat {
    private View mSwitchView;
    private boolean mHideIcon;

    public IconSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IconSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IconSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconSwitchPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        try {
            LinearLayout iconFrame = (LinearLayout) holder.findViewById(android.support.v7.preference.R.id.icon_frame);
            iconFrame.setMinimumWidth(0);
            ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
        mSwitchView = holder.findViewById(android.support.v7.preference.R.id.switchWidget);
        if (mHideIcon && mSwitchView != null) {
            mSwitchView.setVisibility(View.GONE);
        }
    }

    @Nullable
    public View getSwitchView() {
        return mSwitchView;
    }

    public void hideSwitch() {
        mHideIcon = true;
    }

    public void showSwitch() {
        mHideIcon = false;
    }
}
