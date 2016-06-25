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
import android.view.animation.OvershootInterpolator;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.webheads.physics.MovementTracker;

/**
 * Web head object which adds draggable and gesture functionality.
 */
@SuppressLint("ViewConstructor")
public class WebHeadNew extends BaseWebHead implements SpringListener {

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
    private final SpringConfig FLING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(40, 7);
    private final SpringConfig DRAG_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(0, 1.5);
    private final SpringConfig SNAP_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(100, 7);
    /**
     * Flag to know if web head is being dragged
     */
    private boolean mDragging;
    /**
     * Whether we are currently locked on to remove web head.
     */
    private boolean mWasRemoveLocked;
    private boolean mWasClicked;

    //-------------------------------------------------------------------------------------------
    private float posX, posY;
    private int initialDownX, initialDownY;
    /**
     * The interaction listener that clients can provide to listen for events on webhead
     */
    private WebHeadInteractionListener mInteractionListener;
    /**
     * Minimum horizontal velocity that we need to move the web head from one end of the screen
     * to another
     */
    private int MINIMUM_HORIZONTAL_FLING_VELOCITY;
    /**
     * Flag to know there was a fling operation before.
     */
    private boolean mWasFlung;
    private SpringSystem mSpringSystem;
    private Spring mXSpring, mYSpring;
    private MovementTracker mMovementTracker;
    //-------------------------------------------------------------------------------------------

    public WebHeadNew(@NonNull Context context, @NonNull String url, @Nullable WebHeadInteractionListener listener) {
        super(context, url);
        mInteractionListener = listener;
        setUpSprings();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void onGlobalLayout() {
                mMovementTracker = new MovementTracker(10, sDispHeight, sDispWidth, getWidth());
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setUpSprings() {
        mSpringSystem = SpringSystem.create();
        mContentGroup.setVisibility(INVISIBLE); // To be made visible on reveal
        mYSpring = mSpringSystem.createSpring();
        mYSpring.addListener(this);
        mXSpring = mSpringSystem.createSpring();
        mXSpring.addListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContentGroup.setVisibility(VISIBLE);
        mContentGroup.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .withLayer()
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Don't react to any touch event and consume it when we are being destroyed
        if (mDestroyed) return true;
        try {
            // Reset gesture flag on each event
            mWasFlung = false;
            mWasClicked = false;

            // Let gesture detector intercept events
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

        // hide remove view
        RemoveWebHead.disappear();
        touchUp();
        return false;
    }

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

    private void touchDown() {
     /*   mContentGroup.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .withLayer()
                .setInterpolator(new OvershootInterpolator())
                .start();*/
    }

    private void touchUp() {
      /*  mContentGroup.animate()
                .scaleX(1f)
                .scaleY(1f)
                .withLayer()
                .setInterpolator(new OvershootInterpolator())
                .start();*/
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        mWindowParams.x = (int) mXSpring.getCurrentValue();
        mWindowParams.y = (int) mYSpring.getCurrentValue();
        updateView();
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

    @Override
    public void destroySelf(boolean receiveCallback) {
        super.destroySelf(receiveCallback);
        mInteractionListener = null;
        mSpringSystem.removeAllListeners();
        mXSpring.destroy();
        mYSpring.destroy();
    }

    public interface WebHeadInteractionListener {
        void onWebHeadClick(@NonNull WebHeadNew webHead);

        void onWebHeadDestroy(@NonNull WebHeadNew webHead, boolean isLastWebHead);
    }

    /**
     * A gesture listener class to monitor standard fling and click events on the web head view.
     */
    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mWasClicked = true;

            if (Preferences.webHeadsCloseOnOpen(getContext()) && mContentGroup != null) {
                if (mFavicon != null) {
                    mFavicon.setAlpha(0.0f);
                }
                mContentGroup.animate()
                        .scaleX(0.0f)
                        .scaleY(0.0f)
                        .alpha(0.5f)
                        .withLayer()
                        .setDuration(150)
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
            if (mInteractionListener != null) mInteractionListener.onWebHeadClick(WebHeadNew.this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mDragging = false;
            // Ignore polarity. Velocity tracker's velocity directions are wrong anyways.
            // Timber.d(String.valueOf(MINIMUM_HORIZONTAL_FLING_VELOCITY));
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
