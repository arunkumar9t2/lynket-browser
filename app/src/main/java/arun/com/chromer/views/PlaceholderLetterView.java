/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Style.FILL;
import static arun.com.chromer.util.ColorUtil.PLACEHOLDER_COLORS;

public class PlaceholderLetterView extends View {
    private Paint paint;
    private String placeHolder = null;
    final RectF frame = new RectF();
    private Paint textPaint;

    public PlaceholderLetterView(Context context) {
        super(context);
        init();
    }

    public PlaceholderLetterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlaceholderLetterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setStyle(FILL);
        final int color = PLACEHOLDER_COLORS[new Random().nextInt(PLACEHOLDER_COLORS.length)];
        paint.setColor(color);

        final float shadwR = Utils.dpToPx(1.8);
        final float shadwDx = Utils.dpToPx(0.1);
        final float shadwDy = Utils.dpToPx(1.2);
        final float textSize = Utils.dpToPx(20);

        paint.setShadowLayer(shadwR, shadwDx, shadwDy, 0x75000000);

        textPaint = new Paint(ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextSize(textSize);
        textPaint.setColor(ColorUtil.getForegroundWhiteOrBlack(color));
        textPaint.setStyle(FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(placeHolder)) {
            return;
        }
        final int size = getWidth();
        final int corner = Utils.dpToPx(2);
        frame.left = 0;
        frame.top = 0;
        frame.right = size;
        frame.bottom = size;
        canvas.drawRoundRect(frame, corner, corner, paint);

        drawTextInCanvasCentre(canvas, textPaint, Utils.getFirstLetter(placeHolder).toUpperCase());
    }

    private void drawTextInCanvasCentre(Canvas canvas, Paint paint, String character) {
        final int cH = canvas.getClipBounds().height();
        final int cW = canvas.getClipBounds().width();
        final Rect rect = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(character, 0, character.length(), rect);
        final float x = cW / 2f - rect.width() / 2f - rect.left;
        final float y = cH / 2f + rect.height() / 2f - rect.bottom;
        canvas.drawText(character, x, y, paint);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        // Make is square
        setMeasuredDimension(size, size);
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
        invalidate();
    }
}
