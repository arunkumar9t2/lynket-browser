package arun.com.chromer.activities.settings.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

import arun.com.chromer.R;

/**
 * Created by Arun on 16/04/2016.
 */
public class ColorPreference extends Preference {
    private View colorIndicator;
    @ColorInt
    private int color = 0;

    private int DEFAULT_COLOR = 0;

    public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPreference(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setWidgetLayoutResource(R.layout.widget_color_preference);

        DEFAULT_COLOR = ContextCompat.getColor(getContext(), R.color.colorPrimary);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference);
            int colorRes = ta.getResourceId(R.styleable.ColorPreference_color, 0);
            if (colorRes != 0) {
                color = ContextCompat.getColor(getContext(), colorRes);
            }
            ta.recycle();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            color = this.getPersistedInt(DEFAULT_COLOR);
        } else {
            color = (int) defaultValue;
            persistInt(color);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_COLOR);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        colorIndicator = holder.findViewById(R.id.color_preview);
        PreferenceIconLayoutHelper.applyLayoutChanges(holder, isEnabled());
        invalidate();
    }

    private void invalidate() {
        if (colorIndicator != null) {
            colorIndicator.setBackgroundColor(color);
        }
    }

    @ColorInt
    public int getColor() {
        return getPersistedInt(color);
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        persistInt(color);
        setSummary(colorHexValue());
        invalidate();
    }

    private String colorHexValue() {
        //noinspection PointlessBitwiseExpression
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public void refreshSummary() {
        setSummary(colorHexValue());
    }
}
