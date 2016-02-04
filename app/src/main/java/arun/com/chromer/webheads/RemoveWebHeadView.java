package arun.com.chromer.webheads;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;
import timber.log.Timber;

/**
 * Created by Arun on 03/02/2016.
 */
public class RemoveWebHeadView extends View {

    private static final int SIZE_DP = 72;
    private static WindowManager sWindowManager;
    private static RemoveWebHeadView ourInstance;
    private WindowManager.LayoutParams mWindowParams;
    private int mDispWidth;
    private int mDispHeight;
    private Paint mBgPaint;
    private Spring mScaleSpring;

    private SpringSystem mSpringSystem;

    private boolean mHidden;

    private RemoveWebHeadView(Context context, WindowManager windowManager) {
        super(context);
        sWindowManager = windowManager;

        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        setDisplayMetrics();

        mWindowParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
        mWindowParams.x = 0;
        mWindowParams.y = Util.dpToPx(20);


        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setShadowLayer(4.0f, 1.0f, 2.0f, 0x85000000);


        mSpringSystem = SpringSystem.create();
        mScaleSpring = mSpringSystem.createSpring();
        mScaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                setScaleX(value);
                setScaleY(value);
            }
        });
    }

    public static RemoveWebHeadView get(Context context, WindowManager windowManager) {
        if (ourInstance != null)
            return ourInstance;
        else {
            Timber.d("Creating new instance");
            ourInstance = new RemoveWebHeadView(context, windowManager);
            return ourInstance;
        }
    }

    private void setDisplayMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(metrics);
        mDispWidth = metrics.widthPixels;
        mDispHeight = metrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Util.dpToPx(SIZE_DP + 10);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.4), mBgPaint);
        setAlpha(0f);
    }

    public WindowManager.LayoutParams getWindowParams() {
        return mWindowParams;
    }

    public void hide() {
        if (!mHidden) {
            mScaleSpring.setEndValue(0.0f);
            mHidden = true;
        }
    }

    public void reveal() {
        setAlpha(1f);
        if (mHidden) {
            mScaleSpring.setEndValue(1);
            mHidden = false;
        }
    }
}
