package arun.com.chromer.preferences.widgets;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

/**
 * A helper preference view without weird margin on preference icon. Attempts to alter properties on
 * icon frame and icon itself so that it looks better
 */
public class IconCheckboxPreference extends CheckBoxPreference {
    private View mCheckboxView;
    private boolean mHideCheckBox;

    public IconCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IconCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IconCheckboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconCheckboxPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        IconLayoutHelper.applyLayoutChanges(holder);
        mCheckboxView = holder.findViewById(android.support.v7.preference.R.id.checkbox);
        if (mHideCheckBox && mCheckboxView != null) {
            mCheckboxView.setVisibility(View.GONE);
        }
    }

    public void hideCheckbox() {
        mHideCheckBox = true;
    }

    public void showCheckbox() {
        mHideCheckBox = false;
    }
}
