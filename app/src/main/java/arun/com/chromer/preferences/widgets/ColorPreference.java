package arun.com.chromer.preferences.widgets;

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
    private View mColorIndicator;

    @ColorInt
    private int mColor = 0;

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
        setWidgetLayoutResource(R.layout.preference_color_widget);

        DEFAULT_COLOR = ContextCompat.getColor(getContext(), R.color.colorPrimary);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference);
            int colorRes = ta.getResourceId(R.styleable.ColorPreference_color, 0);
            if (colorRes != 0) {
                mColor = ContextCompat.getColor(getContext(), colorRes);
            }
            ta.recycle();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mColor = this.getPersistedInt(DEFAULT_COLOR);
        } else {
            mColor = (int) defaultValue;
            persistInt(mColor);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_COLOR);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mColorIndicator = holder.findViewById(R.id.color_preview);
        invalidate();
    }

    private void invalidate() {
        if (mColorIndicator != null) {
            mColorIndicator.setBackgroundColor(mColor);
        }
    }

    @ColorInt
    public int getColor() {
        return getPersistedInt(mColor);
    }

    public void setColor(@ColorInt int color) {
        mColor = color;
        persistInt(color);
        setSummary(colorHexValue());
        invalidate();
    }

    private String colorHexValue() {
        //noinspection PointlessBitwiseExpression
        return String.format("#%06X", (0xFFFFFF & mColor));
    }

    public void refreshSummary() {
        setSummary(colorHexValue());
    }
}
