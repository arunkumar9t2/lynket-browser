package arun.com.chromer.webheads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import arun.com.chromer.util.Preferences;
import arun.com.chromer.util.Util;
import timber.log.Timber;

/**
 * Created by Arun on 30/01/2016.
 */
@SuppressLint("ViewConstructor")
public class WebHead extends FrameLayout {

    private static final int STACKING_GAP_DP = 6;

    private static WindowManager sWindowManager;

    private static int WEB_HEAD_COUNT = 0;

    private final String mUrl;

    private final GestureDetector mGestDetector = new GestureDetector(getContext(), new GestureTapListener());

    private final int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private float posX;
    private float posY;

    private int initialDownX, initialDownY;

    private WindowManager.LayoutParams mWindowParams;

    private int mDispHeight, mDispWidth;

    private boolean mDragging;

    private Spring mScaleSpring, mWallAttachSpring, mXSpring, mYSpring;
    private SpringSystem mSpringSystem;

    private RemoveWebHead mRemoveWebHead;
    private WebHeadCircle contentView;

    private boolean mWasRemoveLocked;

    private boolean mDimmed;

    private boolean mUserManuallyMoved;

    private WebHeadInteractionListener mInteractionListener;

    private boolean isBeingDestroyed;

    private static final double MAGNETISM_THRESHOLD = Util.dpToPx(120);

    private static Point mCentreLockPoint;

    public WebHead(Context context, String url, WindowManager windowManager) {
        super(context);
        mUrl = url;
        sWindowManager = windowManager;

        init(context, url, windowManager);

        WEB_HEAD_COUNT++;

        Timber.d("Created %d webheads", WEB_HEAD_COUNT);
    }


    public WindowManager.LayoutParams getWindowParams() {
        return mWindowParams;
    }

    private void init(Context context, String url, WindowManager windowManager) {
        // Getting an instance of remove view
        mRemoveWebHead = RemoveWebHead.get(context, windowManager);

        contentView = new WebHeadCircle(context, url);
        addView(contentView);

        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        setDisplayMetrics();
        setSpawnLocation();
        setUpSprings();
    }

    private void setUpSprings() {
        mSpringSystem = SpringSystem.create();

        mScaleSpring = mSpringSystem.createSpring();
        mScaleSpring.addListener(new ScaleSpringListener());

        mWallAttachSpring = mSpringSystem.createSpring();
        mWallAttachSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                if (!mDragging) {
                    mWindowParams.x = (int) spring.getCurrentValue();
                    sWindowManager.updateViewLayout(WebHead.this, mWindowParams);
                }
            }
        });

        mYSpring = mSpringSystem.createSpring();
        mYSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                mWindowParams.y = (int) spring.getCurrentValue();
                sWindowManager.updateViewLayout(WebHead.this, mWindowParams);
            }
        });

        mXSpring = mSpringSystem.createSpring();
        mXSpring.addListener(new SimpleSpringListener() {
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

    @SuppressLint("RtlHardcoded")
    private void setSpawnLocation() {
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = Preferences.webHeadsSpawnLocation(getContext()) == 1 ? mDispWidth : 0;
        mWindowParams.y = mDispHeight / 3;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Don't react to any touch event when we are being destroyed
        if (isBeingDestroyed) return super.onTouchEvent(event);

        if (mRemoveWebHead == null) initRemoveView();

        mGestDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialDownX = mWindowParams.x;
                initialDownY = mWindowParams.y;

                posX = event.getRawX();
                posY = event.getRawY();

                // Shrink on touch
                setTouchingScale();

                // transparent on touch
                setTouchingAlpha();
                break;
            case MotionEvent.ACTION_UP:
                mDragging = false;

                if (mWasRemoveLocked) {
                    // If head was locked onto a remove bubble before, then kill ourselves
                    destroySelf(true);
                    return true;
                }

                // Expand on release
                setReleaseScale();

                // opaque on release
                setReleaseAlpha();

                // Go to the nearest side and rest there
                stickToWall();

                // show remove view
                mRemoveWebHead.hide();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.hypot(event.getRawX() - posX, event.getRawY() - posY) > mTouchSlop) {
                    mDragging = true;
                }

                if (mDragging) {
                    move(event);
                    mRemoveWebHead.reveal();
                }
            default:
                break;
        }
        return true;
    }

    private void initRemoveView() {
        Context context = getContext();
        if (context != null && sWindowManager != null) {
            mRemoveWebHead = RemoveWebHead.get(context, sWindowManager);
            Timber.d("Remove view was null, created again");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Spring scale animation when getting attached to window
        mScaleSpring.setCurrentValue(0);
        mScaleSpring.setEndValue(1f);
    }

    private void stickToWall() {
        int x = mWindowParams.x;
        int dispCentre = mDispWidth / 2;

        mWallAttachSpring.setCurrentValue(x, true);

        if ((x + (getWidth() / 2)) >= dispCentre) {
            // move to right wall
            mWallAttachSpring.setEndValue(mDispWidth);
        } else {
            // move to left wall
            mWallAttachSpring.setEndValue(0);
        }
    }

    private void move(MotionEvent event) {
        if (initialDownX == 0) {
            mWindowParams.x = (int) (initialDownX + (event.getRawX() - posX));
        } else {
            mWindowParams.x = (int) (initialDownX + (event.getRawX() - posX)) - getWidth();
        }
        mWindowParams.y = (int) (initialDownY + (event.getRawY() - posY));


        mUserManuallyMoved = true;

        if (isNearRemoveCircle()) {
            mRemoveWebHead.grow();
            setReleaseAlpha();
            setReleaseScale();
            mXSpring.setEndValue(getCentreLockPoint().x);
            mYSpring.setEndValue(getCentreLockPoint().y);
        } else {
            mXSpring.setCurrentValue(mWindowParams.x);
            mYSpring.setCurrentValue(mWindowParams.y);
            mRemoveWebHead.shrink();
            setTouchingAlpha();
            setTouchingScale();
            sWindowManager.updateViewLayout(this, mWindowParams);
        }
    }

    public void moveSelfToStackDistance() {
        if (!mUserManuallyMoved) {
            mYSpring.setCurrentValue(mWindowParams.y);
            mYSpring.setEndValue(mWindowParams.y + Util.dpToPx(STACKING_GAP_DP));
        }
    }

    public void dim() {
        if (!mDimmed) {
            contentView.setAlpha(0.3f);
            mDimmed = true;
        }
    }

    public void bright() {
        if (mDimmed) {
            contentView.setAlpha(1f);
            mDimmed = false;
        }
    }

    private void setReleaseScale() {
        mScaleSpring.setEndValue(1f);
    }

    private void setReleaseAlpha() {
        if (!mDimmed)
            contentView.setAlpha(1f);
    }

    private void setTouchingAlpha() {
        if (!mDimmed)
            contentView.setAlpha(0.7f);
    }

    private void setTouchingScale() {
        mScaleSpring.setEndValue(0.8f);
    }

    private boolean isNearRemoveCircle() {
        Point p = mRemoveWebHead.getCenterCoordinates();
        int rX = p.x;
        int rY = p.y;

        int offset = getWidth() / 2;
        int x = mWindowParams.x + offset;
        int y = mWindowParams.y + +offset + (offset / 2);

        if (getEuclideanDistance(rX, rY, x, y) < MAGNETISM_THRESHOLD) {
            mWasRemoveLocked = true;
            return true;
        } else {
            mWasRemoveLocked = false;
            return false;
        }
    }

    private double getEuclideanDistance(int x1, int y1, int x2, int y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }

    public void destroySelf(boolean shouldReceiveCallback) {
        isBeingDestroyed = true;

        if (mInteractionListener != null && shouldReceiveCallback) {
            mInteractionListener.onWebHeadDestroy(this, isLastWebHead());
        }

        WEB_HEAD_COUNT--;

        Timber.d("%d Webheads remaining", WEB_HEAD_COUNT);

        mWallAttachSpring.setAtRest();
        mWallAttachSpring.destroy();
        mWallAttachSpring = null;

        mScaleSpring.setAtRest();
        mScaleSpring.destroy();
        mScaleSpring = null;

        mYSpring.setAtRest();
        mYSpring.destroy();
        mYSpring = null;

        mXSpring.setAtRest();
        mXSpring.destroy();
        mXSpring = null;

        mRemoveWebHead.hide();
        mRemoveWebHead = null;

        mSpringSystem = null;

        setWebHeadInteractionListener(null);

        removeView(contentView);

        contentView = null;
        sWindowManager.removeView(this);
    }

    private boolean isLastWebHead() {
        return WEB_HEAD_COUNT - 1 == 0;
    }

    public void setWebHeadInteractionListener(WebHeadInteractionListener listener) {
        mInteractionListener = listener;
    }

    public String getUrl() {
        return mUrl;
    }

    private Point getCentreLockPoint() {
        if (mCentreLockPoint == null) {
            Point removeCentre = mRemoveWebHead.getCenterCoordinates();
            int offset = getWidth() / 2;
            int x = removeCentre.x - offset;
            int y = removeCentre.y - offset - (offset / 2) - (offset / 4);
            mCentreLockPoint = new Point(x, y);
        }
        return mCentreLockPoint;
    }

    public interface WebHeadInteractionListener {
        void onWebHeadClick(WebHead webHead);

        void onWebHeadDestroy(WebHead webHead, boolean isLastWebHead);
    }

    private class ScaleSpringListener extends SimpleSpringListener {

        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            contentView.setScaleX(value);
            contentView.setScaleY(value);
        }
    }

    private class GestureTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mInteractionListener != null) mInteractionListener.onWebHeadClick(WebHead.this);

            if (mRemoveWebHead != null) mRemoveWebHead.hide();
            return super.onSingleTapConfirmed(e);
        }

    }

}
