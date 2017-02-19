package arun.com.chromer.activities.settings.preferences.widgets;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * A helper preference view without weird margin on preference icon. Attempts to alter properties on
 * icon frame and icon itself so that it looks better
 */
public class IconSwitchPreference extends SwitchPreferenceCompat {
    private boolean hideSwitch;

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
        PreferenceIconLayoutHelper.applyLayoutChanges(holder, isEnabled());
        View switchView = holder.findViewById(android.support.v7.preference.R.id.switchWidget);
        if (hideSwitch && switchView != null) {
            switchView.setVisibility(View.GONE);
        }
    }

    public void hideSwitch() {
        hideSwitch = true;
    }

    public void showSwitch() {
        hideSwitch = false;
    }
}
