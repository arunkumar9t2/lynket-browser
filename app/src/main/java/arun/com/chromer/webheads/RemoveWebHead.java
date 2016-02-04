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

    private boolean mGrew;

    private RemoveWebHead(Context context, WindowManager windowManager) {
        super(context);
        sWindowManager = windowManager;

        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        setDisplayMetrics();

        setVisibility(INVISIBLE);
        mHidden = true;

        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;

        int offset = calculateXOffset();
        mWindowParams.x = (mDispWidth / 2) - offset;
        mWindowParams.y = mDispHeight - Util.dpToPx(20);


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

    public static void destroy() {
        if (ourInstance != null) {
            ourInstance.destroySelf();
        }
    }

    private void destroySelf() {
        mScaleSpring.setAtRest();
        mScaleSpring.destroy();
        mScaleSpring = null;

        mBgPaint = null;
        removeView(mRemoveHeadCircle);
        mRemoveHeadCircle = null;

        mWindowParams = null;

        mSpringSystem = null;

        ourInstance = null;

        sWindowManager.removeView(this);
        Timber.d("Remove view detached and killed");
    }

    private int calculateXOffset() {
        int sizePx = Util.dpToPx(RemoveHeadCircle.REMOVE_HEAD_DP + RemoveHeadCircle.EXTRA_DP);

        // The radius were given as width/2.4, so lets calculate multiply by 2/2.4 which is 0.83
        int offset = (int) (sizePx / 2.4);
        return offset;
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

    public void grow() {
        if (!mGrew) {
            mScaleSpring.setCurrentValue(1f);
            mScaleSpring.setEndValue(1.1f);
            mGrew = true;
        }
    }

    public void shrink() {
        if (mGrew) {
            mScaleSpring.setEndValue(1);
            mGrew = false;
        }
    }
}
