/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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

package arun.com.chromer.webheads.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

import arun.com.chromer.settings.Preferences;
import arun.com.chromer.util.Utils;

/**
 * A simple circle view that fills the canvas with a circle fill color. Padding is given to pre L
 * devices to accommodate shadows if needed.
 */
public class CircleView extends View {
    /**
     * Paint used to draw the circle background
     */
    final Paint mBgPaint;

    @ColorInt
    private int mColor;

    public CircleView(Context context) {
        this(context, null, 0);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL);
        mColor = Preferences.get(getContext()).webHeadColor();
        mBgPaint.setColor(mColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float outerRadius;
        if (Utils.isLollipopAbove()) {
            outerRadius = getMeasuredWidth() / 2;
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredWidth() / 2,
                    outerRadius,
                    mBgPaint);
        } else {
            outerRadius = (float) (getMeasuredWidth() / 2.4);
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredWidth() / 2,
                    outerRadius,
                    mBgPaint);
        }
    }

    @ColorInt
    public int getColor() {
        return mColor;
    }

    public void setColor(@ColorInt int color) {
        mColor = color;
        mBgPaint.setColor(color);
        invalidate();
    }
}
