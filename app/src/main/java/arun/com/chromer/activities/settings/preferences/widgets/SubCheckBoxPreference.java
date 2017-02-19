package arun.com.chromer.activities.settings.preferences.widgets;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import arun.com.chromer.util.Utils;

/**
 * Created by Arun on 19/06/2016.
 */
public class SubCheckBoxPreference extends CheckBoxPreference {
    public SubCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SubCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SubCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubCheckBoxPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);

        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setPadding(Utils.dpToPx(30), 0, 0, 0);
        summary.setPadding(Utils.dpToPx(30), 0, 0, 0);
    }
}
