package arun.com.chromer.webheads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import java.net.URL;

import arun.com.chromer.R;
import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 04/02/2016.
 */
@SuppressLint("ViewConstructor")
public class WebHeadCircle extends View {

    private static final int WEB_HEAD_SIZE_DP = 56;
    private final String mUrl;
    private final Paint mBgPaint;
    private final Paint textPaint;

    public WebHeadCircle(Context context, String mUrl) {
        super(context);
        this.mUrl = mUrl;

        int webHeadsColor = Preferences.webHeadColor(context);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(webHeadsColor);
        mBgPaint.setStyle(Paint.Style.FILL);

        float shadwR = context.getResources().getDimension(R.dimen.web_head_shadow_radius);
        float shadwDx = context.getResources().getDimension(R.dimen.web_head_shadow_dx);
        float shadwDy = context.getResources().getDimension(R.dimen.web_head_shadow_dy);

        mBgPaint.setShadowLayer(shadwR, shadwDx, shadwDy, 0x75000000);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextSize(Util.dpToPx(20));
        textPaint.setColor(Util.getForegroundTextColor(webHeadsColor));
        textPaint.setStyle(Paint.Style.FILL);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Util.dpToPx(WEB_HEAD_SIZE_DP + 10);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.4), mBgPaint);

        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        String indicator = getUrlIndicator();
        if (indicator != null) drawTextInCanvasCentre(canvas, textPaint, indicator);
    }

    private String getUrlIndicator() {
        String result = "x";
        if (mUrl != null) {
            try {
                URL url = new URL(mUrl);
                String host = url.getHost();
                if (host != null && host.length() != 0) {
                    if (host.startsWith("www")) {
                        String[] splits = host.split("\\.");
                        if (splits.length > 1) result = String.valueOf(splits[1].charAt(0));
                        else result = String.valueOf(splits[0].charAt(0));
                    } else
                        result = String.valueOf(host.charAt(0));
                }
            } catch (Exception e) {
                return result;
            }
        }
        return result.toUpperCase();
    }

    private void drawTextInCanvasCentre(Canvas canvas, Paint paint, String text) {
        int cH = canvas.getClipBounds().height();
        int cW = canvas.getClipBounds().width();
        Rect rect = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), rect);
        float x = cW / 2f - rect.width() / 2f - rect.left;
        float y = cH / 2f + rect.height() / 2f - rect.bottom;
        canvas.drawText(text, x, y, paint);
    }
}
