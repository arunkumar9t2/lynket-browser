package arun.com.chromer.webheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.Util;

/**
 * A simple circle view that uses paint shadow in pre L and system elevation on post L
 */
public class ElevatedCircleView extends View {

    /**
     * Paint used to draw the circle
     */
    private final Paint mBgPaint;

    public ElevatedCircleView(Context context) {
        this(context, null, 0);
    }

    public ElevatedCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElevatedCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(Preferences.webHeadColor(getContext()));

        if (!Util.isLollipopAbove()) {
            float shadowR = context.getResources().getDimension(R.dimen.web_head_shadow_radius);
            float shadowDx = context.getResources().getDimension(R.dimen.web_head_shadow_dx);
            float shadowDy = context.getResources().getDimension(R.dimen.web_head_shadow_dy);
            mBgPaint.setShadowLayer(shadowR, shadowDx, shadowDy, 0x55000000);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Util.isLollipopAbove()) {
            setOutlineProvider(new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    int shapeSize = getMeasuredWidth();
                    outline.setRoundRect(0, 0, shapeSize, shapeSize, shapeSize / 2);
                }
            });
            setClipToOutline(true);
        }
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
        if (Util.isLollipopAbove()) {
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

    public void setColor(@ColorInt int color) {
        mBgPaint.setColor(color);
        invalidate();
    }
}
