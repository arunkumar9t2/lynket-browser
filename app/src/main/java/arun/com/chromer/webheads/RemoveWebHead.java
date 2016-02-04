package arun.com.chromer.webheads;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;
import timber.log.Timber;

/**
 * Created by Arun on 03/02/2016.
 */
public class RemoveWebHead extends FrameLayout {


    private static WindowManager sWindowManager;
    private static RemoveWebHead ourInstance;

    private WindowManager.LayoutParams mWindowParams;

    private int mDispWidth;
    private int mDispHeight;

    private Paint mBgPaint;

    private Spring mScaleSpring;
    private SpringSystem mSpringSystem;

    private boolean mHidden;

    private RemoveHeadCircle mRemoveHeadCircle;

    private RemoveWebHead(Context context, WindowManager windowManager) {
        super(context);
        sWindowManager = windowManager;

        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        setDisplayMetrics();

        setVisibility(INVISIBLE);
        mHidden = true;

        mWindowParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
        mWindowParams.x = 0;
        mWindowParams.y = Util.dpToPx(20);


        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.md_red_700));
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setShadowLayer(4.0f, 1.0f, 2.0f, 0x85000000);

        mRemoveHeadCircle = new RemoveHeadCircle(context);
        addView(mRemoveHeadCircle);

        setUpSprings();
    }

    public static RemoveWebHead get(Context context, WindowManager windowManager) {
        if (ourInstance != null)
            return ourInstance;
        else {
            Timber.d("Creating new instance");
            ourInstance = new RemoveWebHead(context, windowManager);
            return ourInstance;
        }
    }

    private void setUpSprings() {
        mSpringSystem = SpringSystem.create();
        mScaleSpring = mSpringSystem.createSpring();
        mScaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                mRemoveHeadCircle.setScaleX(value);
                mRemoveHeadCircle.setScaleY(value);
            }
        });
    }

    private void setDisplayMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(metrics);
        mDispWidth = metrics.widthPixels;
        mDispHeight = metrics.heightPixels;
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
        setVisibility(VISIBLE);
        if (mHidden) {
            mScaleSpring.setEndValue(1);
            mHidden = false;
        }
    }
}
