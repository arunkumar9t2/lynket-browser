package arun.com.chromer.webheads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import java.net.MalformedURLException;
import java.net.URL;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 30/01/2016.
 */
@SuppressLint("ViewConstructor")
public class WebHead extends View {

    private static final int WEB_HEAD_SIZE_DP = 48;
    private static WindowManager sWindowManager;
    private final String mUrl;
    private final GestureDetector mGestDetector = new GestureDetector(getContext(), new GestureSingleTap());
    private float posX;
    private float posY;
    private WindowManager.LayoutParams mWindowParams;
    private int mDispHeight, mDispWidth;
    private boolean mDragging;
    private WebHeadClickListener mClickListener;
    private Spring mScaleSpring, mWallAttachSpring;
    private Paint mBgPaint;

    public WebHead(Context context, String url, WindowManager windowManager) {
        super(context);
        mUrl = url;
        sWindowManager = windowManager;
        init();
    }


    public WindowManager.LayoutParams getWindowParams() {
        return mWindowParams;
    }

    private void init() {
        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        setDisplayMetrics();
        setSpawnLocation();
        setUpSprings();

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.web_head_bg));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setShadowLayer(4.0f, 1.0f, 2.0f, 0x85000000);
    }

    private void setUpSprings() {
        SpringSystem springSystem = SpringSystem.create();

        mScaleSpring = springSystem.createSpring();
        mScaleSpring.addListener(new ScaleSpringListener());

        mWallAttachSpring = springSystem.createSpring();
        mWallAttachSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                mWindowParams.x = (int) spring.getCurrentValue();
                sWindowManager.updateViewLayout(WebHead.this, mWindowParams);
            }
        });
    }

    private void setDisplayMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(metrics);
        mDispWidth = metrics.widthPixels;
        mDispHeight = metrics.heightPixels;
    }


    private void setSpawnLocation() {
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = 0;
        mWindowParams.y = mDispHeight / 3;
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
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextSize(Util.dpToPx(18));
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);

        String indicator = getUrlIndicator();
        if (indicator != null) drawTextInCanvasCentre(canvas, textPaint, indicator);
    }

    private String getUrlIndicator() {
        String result = "x";
        if (mUrl != null) {
            try {
                URL url = new URL(mUrl);
                String host = url.getHost();
                if (host != null) {
                    if (host.startsWith("www")) {
                        String[] splits = host.split("\\.");
                        if (splits.length > 1) result = String.valueOf(splits[1].charAt(0));
                        else result = String.valueOf(splits[0].charAt(0));
                    } else
                        result = String.valueOf(host.charAt(0));
                }
            } catch (MalformedURLException e) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        mGestDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragging = true;

                // Shrink on touch
                mScaleSpring.setEndValue(0.7f);
                break;
            case MotionEvent.ACTION_UP:
                // Expand on release
                mScaleSpring.setEndValue(1f);

                stickToWall();
                mDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDragging) move(x - posX, y - posY);
            default:
                break;
        }
        posX = x;
        posY = y;
        return true;
    }

    private void stickToWall() {
        int x = mWindowParams.x;
        int dispCentre = mDispWidth / 2;
        mWallAttachSpring.setCurrentValue(x);

        if ((x + (getWidth() / 2)) >= dispCentre) {
            // move to right wall
            mWallAttachSpring.setEndValue(mDispWidth);
        } else {
            // move to left wall
            mWallAttachSpring.setEndValue(0);
        }
    }

    private void move(float deltaX, float deltaY) {
        mWindowParams.x += deltaX;
        mWindowParams.y += deltaY;

        // update wall attach spring here
        mWallAttachSpring.setCurrentValue(mWindowParams.x, true);

        sWindowManager.updateViewLayout(this, mWindowParams);
    }

    public void setOnWebHeadClickListener(WebHeadClickListener listener) {
        mClickListener = listener;
    }

    public String getUrl() {
        return mUrl;
    }

    public interface WebHeadClickListener {
        void onClick(WebHead webHead);
    }

    private class ScaleSpringListener extends SimpleSpringListener {

        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            //float scale = 1f - (value * 0.5f);
            // TODO To investigate why scaling is not working
            setScaleX(value);
            setScaleY(value);
        }
    }

    private class GestureSingleTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mClickListener != null) mClickListener.onClick(WebHead.this);
            return super.onSingleTapConfirmed(e);
        }
    }

}
