package arun.com.chromer.webheads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import arun.com.chromer.util.Util;
import timber.log.Timber;

/**
 * Created by Arun on 03/02/2016.
 */
@SuppressLint("ViewConstructor")
public class RemoveWebHead extends FrameLayout {

    private static WindowManager sWindowManager;
    private static RemoveWebHead ourInstance;

    private WindowManager.LayoutParams mWindowParams;

    private int mDispWidth;
    private int mDispHeight;

    private Spring mScaleSpring;
    private SpringSystem mSpringSystem;

    private boolean mHidden;

    private RemoveHeadCircle mRemoveHeadCircle;

    private boolean mGrew;

    private Point mCentrePoint;

    @SuppressLint("RtlHardcoded")
    private RemoveWebHead(Context context, WindowManager windowManager) {
        super(context);
        sWindowManager = windowManager;

        mRemoveHeadCircle = new RemoveHeadCircle(context);
        addView(mRemoveHeadCircle);

        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        setDisplayMetrics();

        setVisibility(INVISIBLE);
        mHidden = true;

        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;

        int offset = getOffset();
        mWindowParams.x = (mDispWidth / 2) - offset;
        mWindowParams.y = mDispHeight - (mDispHeight / 6) - offset;

        setUpSprings();

        sWindowManager.addView(this, mWindowParams);
    }

    public static RemoveWebHead get(Context context) {
        if (ourInstance != null)
            return ourInstance;
        else {
            Timber.d("Creating new instance of remove web head");
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            ourInstance = new RemoveWebHead(context, windowManager);
            return ourInstance;
        }
    }

    public static void destroy() {
        if (ourInstance != null) {
            ourInstance.destroySelf();
        }
    }

    public static void hideSelf() {
        if (ourInstance != null) {
            ourInstance.hide();
        }
    }

    private void destroySelf() {
        mScaleSpring.setAtRest();
        mScaleSpring.destroy();
        mScaleSpring = null;

        removeView(mRemoveHeadCircle);
        mRemoveHeadCircle = null;

        mWindowParams = null;

        mSpringSystem = null;

        sWindowManager.removeView(this);

        mCentrePoint = null;

        ourInstance = null;
        Timber.d("Remove view detached and killed");
    }

    private int getOffset() {
        int sizePx = Util.dpToPx(RemoveHeadCircle.REMOVE_HEAD_DP + RemoveHeadCircle.EXTRA_DP);
        return (sizePx / 2);
    }

    public Point getCenterCoordinates() {
        if (mCentrePoint == null) {
            int offset = getWidth() / 2;
            int rX = getWindowParams().x + offset;
            int rY = getWindowParams().y + offset + (offset / 2);
            mCentrePoint = new Point(rX, rY);
        }
        return mCentrePoint;
    }

    private void setUpSprings() {
        mSpringSystem = SpringSystem.create();
        mScaleSpring = mSpringSystem.createSpring();

        SpringConfig scaleSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(100, 9);
        mScaleSpring.setSpringConfig(scaleSpringConfig);
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

    private WindowManager.LayoutParams getWindowParams() {
        return mWindowParams;
    }

    private void hide() {
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
            mScaleSpring.setCurrentValue(1f, true);
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
