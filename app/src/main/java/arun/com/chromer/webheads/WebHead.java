package arun.com.chromer.webheads;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

/**
 * Created by Arun on 30/01/2016.
 */
public class WebHead extends ImageView {

    private static final int TOUCH_DELAY_MS = 350;
    private static WindowManager sWindowManager;
    float posX, posY;
    float lastDownX, lastDownY;
    private String mUrl;
    private WindowManager.LayoutParams mWindowParams;
    private int mDispHeight, mDispWidth;
    private boolean mDragging;
    private long mLastDownTime;
    private WebHeadClickListener mClickListener;

    private SpringSystem mSpringSystem;

    private Spring mScaleSpring, mWallAttachSpring;

    private GestureDetector mGestDetector = new GestureDetector(getContext(), new GestureSingleTap());

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
    }

    private void setUpSprings() {
        mSpringSystem = SpringSystem.create();

        mScaleSpring = mSpringSystem.createSpring();
        mScaleSpring.addListener(new ScaleSpringListener());

        mWallAttachSpring = mSpringSystem.createSpring();
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
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        mGestDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragging = true;
                mLastDownTime = System.currentTimeMillis();

                lastDownX = event.getX();
                lastDownY = event.getY();

                mScaleSpring.setEndValue(1f);
                break;
            case MotionEvent.ACTION_UP:
                mScaleSpring.setEndValue(0.5f);

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
            float scale = 1f - (value * 0.5f);
            // TODO To investigate why scaling is not working
            WebHead.this.setScaleX(scale);
            WebHead.this.setScaleY(scale);
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
