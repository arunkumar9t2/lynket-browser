package arun.com.chromer.preferences.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

/**
 * A helper preference view without weird margin on preference icon. Attempts to alter properties on
 * icon frame and icon itself so that it looks better
 */
public class IconListPreference extends ListPreference {
    private View mSwitchView;
    private boolean mHideIcon;

    public IconListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IconListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IconListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconListPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        IconLayoutHelper.applyLayoutChanges(holder);
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
