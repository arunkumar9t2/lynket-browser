package arun.com.chromer.webheads.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.Util;
import arun.com.chromer.webheads.physics.MovementTracker;

/**
 * Web head object which adds draggable and gesture functionality.
 */
@SuppressLint("ViewConstructor")
public class WebHead extends BaseWebHead implements SpringListener {

    private static final float TOUCH_DOWN_SCALE = 0.9f;
    private static final float TOUCH_UP_SCALE = 1f;
    /**
     * Coordinate of remove web head that we can lock on to.
     */
    private static int[] sTrashLockCoordinate;
    /**
     * Touch slop of the device
     */
    private final int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    /**
     * Gesture detector to recognize fling and click on web heads
     */
    private final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetectorListener());
    private final SpringConfig FLING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(50, 7);
    private final SpringConfig DRAG_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(0, 1.8);
    private final SpringConfig SNAP_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(100, 7);
    /**
     * Minimum horizontal velocity that we need to move the web head from one end of the screen
     * to another
     */
    private final int MINIMUM_HORIZONTAL_FLING_VELOCITY;
    /**
     * True when being dragged, otherwise false
     */
    private boolean mDragging;
    /**
     * True when attached to remove view, otherwise false
     */
    private boolean mWasRemoveLocked;
    /**
     * True when click was detected, and false on new touch event
     */
    private boolean mWasClicked;
    //-------------------------------------------------------------------------------------------
    private float posX, posY;
    private int initialDownX, initialDownY;
    /**
     * The interaction listener that clients can provide to listen for events on webhead
     */
    private WebHeadInteractionListener mInteractionListener;
    /**
     * True when fling detected and false on new touch event
     */
    private boolean mWasFlung;
    private SpringSystem mSpringSystem;
    //-------------------------------------------------------------------------------------------
    private Spring mXSpring, mYSpring, mScaleSpring;
    private MovementTracker mMovementTracker;
    /**
     * True when touched down and false otherwise
     */
    private boolean mScaledDown;

    /**
     * Inits the web head and attaches to the system window. It is assumed that draw over other apps permission is
     * granted for 6.0+.
     *
     * @param context  Service
     * @param url      Url the web head will carry
     * @param listener for listening to events on the webhead
     */
    public WebHead(@NonNull Context context, @NonNull String url, @Nullable WebHeadInteractionListener listener) {
        super(context, url);
        mInteractionListener = listener;

        setupSprings();

        int scaledScreenWidthDp = (int) (getResources().getConfiguration().screenWidthDp * 5.5);
        MINIMUM_HORIZONTAL_FLING_VELOCITY = Util.dpToPx(scaledScreenWidthDp);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void onGlobalLayout() {
                mMovementTracker = new MovementTracker(10, sDispHeight, sDispWidth, getWidth());
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setupSprings() {
        mSpringSystem = SpringSystem.create();
        mYSpring = mSpringSystem.createSpring();
        mYSpring.addListener(this);
        mXSpring = mSpringSystem.createSpring();
        mXSpring.addListener(this);
        mScaleSpring = mSpringSystem.createSpring();
        mScaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float howMuch = (float) spring.getCurrentValue();
                mContentGroup.setScaleX(howMuch);
                mContentGroup.setScaleY(howMuch);
            }
        });
        mScaleSpring.setCurrentValue(0.0f, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Don't react to any touch event and consume it when we are being destroyed
        if (mDestroyed) return true;
        try {
            // Reset gesture flag on each event
            mWasFlung = false;
            mWasClicked = false;

            // Let gesture detector intercept events, needed for fling and click
            mGestureDetector.onTouchEvent(event);

            if (mWasClicked) return true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handleTouchDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    handleMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (handleTouchUp())
                        return true;
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            String msg = "NPE on web heads " + e.getMessage();
            if (BuildConfig.DEBUG) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
            destroySelf(true);
        }
        return true;
    }

    private void handleTouchDown(@NonNull MotionEvent event) {
        mMovementTracker.onDown();

        initialDownX = mWindowParams.x;
        initialDownY = mWindowParams.y;

        posX = event.getRawX();
        posY = event.getRawY();

        touchDown();
    }

    /**
     * Responsible for moving the web heads around and for locking/unlocking the web head to
     * remove view.
     *
     * @param event the touch event
     */
    private void handleMove(@NonNull MotionEvent event) {
        mMovementTracker.addMovement(event);

        float offsetX = event.getRawX() - posX;
        float offsetY = event.getRawY() - posY;

        if (Math.hypot(offsetX, offsetY) > mTouchSlop) {
            mDragging = true;
        }

        if (mDragging) {
            getRemoveWebHead().reveal();

            mUserManuallyMoved = true;

            int x = (int) (initialDownX + offsetX);
            int y = (int) (initialDownY + offsetY);

            if (isNearRemoveCircle(x, y)) {
                getRemoveWebHead().grow();
                touchUp();

                mXSpring.setSpringConfig(SNAP_CONFIG);
                mYSpring.setSpringConfig(SNAP_CONFIG);

                mXSpring.setEndValue(trashLockCoOrd()[0]);
                mYSpring.setEndValue(trashLockCoOrd()[1]);
            } else {
                getRemoveWebHead().shrink();

                mXSpring.setSpringConfig(DRAG_CONFIG);
                mYSpring.setSpringConfig(DRAG_CONFIG);

                mXSpring.setCurrentValue(x);
                mYSpring.setCurrentValue(y);

                touchDown();
            }
        }
    }

    private boolean handleTouchUp() {
        if (mWasRemoveLocked) {
            // If head was locked onto a remove bubble before, then kill ourselves
            destroySelf(true);
            return true;
        }
        mDragging = false;

        mMovementTracker.onUp();

        // If we were not flung, go to nearest side and rest there
        if (!mWasFlung) {
            // stickToWall();
        }

        touchUp();

        // hide remove view
        RemoveWebHead.disappear();
        return false;
    }

    /**
     * Returns the coordinate where the web head should lock to the remove web heads.
     * Calculated once and reused there after.
     *
     * @return array of x and y.
     */
    private int[] trashLockCoOrd() {
        if (sTrashLockCoordinate == null) {
            int[] removeCentre = getRemoveWebHead().getCenterCoordinates();
            int offset = getWidth() / 2;
            int x = removeCentre[0] - offset;
            int y = removeCentre[1] - offset;
            sTrashLockCoordinate = new int[]{x, y};
        }
        return sTrashLockCoordinate;
    }

    /**
     * Used to determine if the web head is in vicinity of remove web head view.
     *
     * @param x Current x position of web head
     * @param y Current y position of web head
     * @return true if near, false other wise
     */
    private boolean isNearRemoveCircle(int x, int y) {
        int[] p = getRemoveWebHead().getCenterCoordinates();
        int rX = p[0];
        int rY = p[1];

        int offset = getWidth() / 2;
        x += offset;
        y += offset;

        if (dist(rX, rY, x, y) < RemoveWebHead.MAGNETISM_THRESHOLD) {
            mWasRemoveLocked = true;
            return true;
        } else {
            mWasRemoveLocked = false;
            return false;
        }
    }

    private float dist(double x1, double y1, double x2, double y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public void reveal() {
        mScaleSpring.setEndValue(TOUCH_UP_SCALE);
        mScaledDown = false;
    }

    private void touchDown() {
        if (!mScaledDown) {
            mScaleSpring.setEndValue(TOUCH_DOWN_SCALE);
            mScaledDown = true;
        }
    }

    private void touchUp() {
        if (mScaledDown) {
            mScaleSpring.setEndValue(TOUCH_UP_SCALE);
            mScaledDown = false;
        }
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        mWindowParams.x = (int) mXSpring.getCurrentValue();
        mWindowParams.y = (int) mYSpring.getCurrentValue();
        updateView();
        checkBounds();
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    private void checkBounds() {
        int x = mWindowParams.x;
        int y = mWindowParams.y;

        int width = getWidth();

        if (x + width >= sDispWidth) {
            mXSpring.setSpringConfig(FLING_CONFIG);
            mXSpring.setEndValue(sScreenBounds.right);
        }
        if (x - width <= 0) {
            mXSpring.setSpringConfig(FLING_CONFIG);
            mXSpring.setEndValue(sScreenBounds.left);
        }
        if (y + width >= sDispHeight) {
            mYSpring.setSpringConfig(FLING_CONFIG);
            mYSpring.setEndValue(sScreenBounds.bottom);
        }
        if (y - width <= 0) {
            mYSpring.setSpringConfig(FLING_CONFIG);
            mYSpring.setEndValue(sScreenBounds.top);
        }

        int minimumVelocityToReachSides = Util.dpToPx(100);
        if (!mWasRemoveLocked
                && Math.abs(mXSpring.getVelocity()) < minimumVelocityToReachSides
                && Math.abs(mYSpring.getVelocity()) < minimumVelocityToReachSides
                && !mDragging) {
            stickToWall();
        }
    }

    private void stickToWall() {

    }

    @Override
    public void destroySelf(boolean receiveCallback) {
        super.destroySelf(receiveCallback);
        if (mInteractionListener != null && receiveCallback) {
            mInteractionListener.onWebHeadDestroy(this, isLastWebHead());
        }
        mInteractionListener = null;
        mSpringSystem.removeAllListeners();
        mXSpring.destroy();
        mYSpring.destroy();
    }

    public interface WebHeadInteractionListener {
        void onWebHeadClick(@NonNull WebHead webHead);

        void onWebHeadDestroy(@NonNull WebHead webHead, boolean isLastWebHead);
    }

    /**
     * A gesture listener class to monitor standard fling and click events on the web head view.
     */
    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mWasClicked = true;
            if (Preferences.webHeadsCloseOnOpen(getContext()) && mContentGroup != null) {
                if (mWindowParams.x < sDispWidth / 2) {
                    mContentGroup.setPivotX(0);
                } else {
                    mContentGroup.setPivotX(mContentGroup.getWidth());
                }
                mContentGroup.animate()
                        .scaleX(0.0f)
                        .scaleY(0.0f)
                        .alpha(0.5f)
                        .withLayer()
                        .setDuration(300)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                sendCallback();
                            }
                        })
                        .start();
            } else sendCallback();
            return true;
        }

        private void sendCallback() {
            RemoveWebHead.disappear();
            if (mInteractionListener != null) mInteractionListener.onWebHeadClick(WebHead.this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mDragging = false;
            velocityX = Math.max(Math.abs(velocityX), MINIMUM_HORIZONTAL_FLING_VELOCITY);

            float[] adjustedVelocities = mMovementTracker.getAdjustedVelocities(velocityX, velocityY);

            if (adjustedVelocities == null) {
                float[] down = new float[]{e1.getRawX(), e1.getRawY()};
                float[] up = new float[]{e2.getRawX(), e2.getRawY()};
                adjustedVelocities = MovementTracker.adjustVelocities(down, up, velocityX, velocityY);
            }

            if (adjustedVelocities != null) {
                mWasFlung = true;

                mXSpring.setSpringConfig(DRAG_CONFIG);
                mYSpring.setSpringConfig(DRAG_CONFIG);

                mXSpring.setVelocity(adjustedVelocities[0]);
                mYSpring.setVelocity(adjustedVelocities[1]);
                return true;
            }
            return false;
        }
    }
}
