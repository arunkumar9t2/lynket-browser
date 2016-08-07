package arun.com.chromer.webheads.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.Util;
import arun.com.chromer.webheads.physics.MovementTracker;
import arun.com.chromer.webheads.physics.SpringConfigs;
import timber.log.Timber;

/**
 * Web head object which adds draggable and gesture functionality.
 */
@SuppressLint("ViewConstructor")
public class WebHead extends BaseWebHead implements SpringListener {

    private static final float TOUCH_DOWN_SCALE = 1f;
    private static final float TOUCH_UP_SCALE = 1f;
    /**
     * Coordinate of remove web head that we can lock on to.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static int[] sTrashLockCoordinate;
    /**
     * True when being dragged, otherwise false
     */
    private boolean mDragging;
    /**
     * True when attached to remove view, otherwise false
     */
    private boolean mWasRemoveLocked;
    /**
     * True when fling detected and false on new touch event
     */
    private boolean mWasFlung;
    /**
     * True when click was detected, and false on new touch event
     */
    private boolean mWasClicked;
    /**
     * True when touched down and false otherwise
     */
    private boolean mScaledDown;
    /**
     * Minimum horizontal velocity that we need to move the web head from one end of the screen
     * to another
     */
    private static int MINIMUM_HORIZONTAL_FLING_VELOCITY = 0;
    /**
     * Touch slop of the device
     */
    private final int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    /**
     * Gesture detector to recognize fling and click on web heads
     */
    private final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetectorListener());
    /**
     * Individual springs to control X, Y and scale of the web head
     */
    private Spring mXSpring, mYSpring, mScaleSpring;
    /**
     * Movement tracker instance, that is used to adjust X and Y velocity calculated by {@link #mGestureDetector}.
     * This is needed since sometimes velocities coming from {@link GestureDetectorListener#onFling(MotionEvent, MotionEvent, float, float)}
     * has wrong polarity.
     */
    private final MovementTracker mMovementTracker;

    private float posX, posY;
    private int initialDownX, initialDownY;
    /**
     * The interaction listener that clients can provide to listen for events on webhead.
     */
    private final WebHeadContract mContract;

    /**
     * Inits the web head and attaches to the system window. It is assumed that draw over other apps permission is
     * granted for 6.0+.
     *
     * @param context  Service
     * @param url      Url the web head will carry
     * @param contract for communicating to events on the webhead
     */
    public WebHead(@NonNull Context context, @NonNull String url, @NonNull WebHeadContract contract) {
        super(context, url);
        mContract = contract;
        mMaster = true;
        mMovementTracker = MovementTracker.obtain();
        calcVelocities();
        setupSprings();
    }

    private void calcVelocities() {
        if (MINIMUM_HORIZONTAL_FLING_VELOCITY == 0) {
            int scaledScreenWidthDp = (getResources().getConfiguration().screenWidthDp * 10);
            MINIMUM_HORIZONTAL_FLING_VELOCITY = Util.dpToPx(scaledScreenWidthDp);
        }
    }

    private void setupSprings() {
        mYSpring = mContract.newSpring();
        mYSpring.addListener(this);
        mXSpring = mContract.newSpring();
        mXSpring.addListener(this);
        mScaleSpring = mContract.newSpring();
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
        if (mDestroyed || !mMaster) return true;
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
        mDragging = false;

        mMovementTracker.onDown();

        initialDownX = mWindowParams.x;
        initialDownY = mWindowParams.y;

        posX = event.getRawX();
        posY = event.getRawY();

        if (mMaster) {
            masterDownX = (int) posX;
            masterDownY = (int) posY;
        }

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

                mXSpring.setSpringConfig(SpringConfigs.SNAP);
                mYSpring.setSpringConfig(SpringConfigs.SNAP);

                mXSpring.setEndValue(trashLockCoOrd()[0]);
                mYSpring.setEndValue(trashLockCoOrd()[1]);

            } else {
                getRemoveWebHead().shrink();

                mXSpring.setSpringConfig(SpringConfigs.DRAG);
                mYSpring.setSpringConfig(SpringConfigs.DRAG);

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

        if (!mWasFlung && mUserManuallyMoved) {
            stickToWall();
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
        //if (sTrashLockCoordinate == null) {
        int[] removeCentre = getRemoveWebHead().getCenterCoordinates();
        int offset = getWidth() / 2;
        int x = removeCentre[0] - offset;
        int y = removeCentre[1] - offset;
        sTrashLockCoordinate = new int[]{x, y};
        //}
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
            mContract.onMasterLockedToRemove();
            return true;
        } else {
            mWasRemoveLocked = false;
            mContract.onMasterReleasedFromRemove();
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

    @NonNull
    public Spring getXSpring() {
        return mXSpring;
    }

    @NonNull
    public Spring getYSpring() {
        return mYSpring;
    }

    public void setSpringConfig(@NonNull SpringConfig config) {
        mXSpring.setSpringConfig(config);
        mYSpring.setSpringConfig(config);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        mWindowParams.x = (int) mXSpring.getCurrentValue();
        mWindowParams.y = (int) mYSpring.getCurrentValue();
        updateView();
        if (mMaster) {
            mContract.onMasterWebHeadMoved(mWindowParams.x, mWindowParams.y);
            checkBounds();
            updateBadgeLocation();
        }
    }

    @SuppressLint("RtlHardcoded")
    private void updateBadgeLocation() {
        final LayoutParams params = (LayoutParams) mBadgeView.getLayoutParams();
        if (mWindowParams.x > sDispWidth / 2) {
            params.gravity = Gravity.TOP | Gravity.LEFT;
        } else {
            params.gravity = Gravity.TOP | Gravity.RIGHT;
        }
        mBadgeView.setLayoutParams(params);
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
        // Only check when free
        if (mDragging || sScreenBounds == null || !mMaster) {
            return;
        }

        int x = mWindowParams.x;
        int y = mWindowParams.y;

        int width = getWidth();

        if (x + width >= sDispWidth) {
            mXSpring.setSpringConfig(SpringConfigs.FLING);
            mXSpring.setEndValue(sScreenBounds.right);
        }
        if (x - width <= 0) {
            mXSpring.setSpringConfig(SpringConfigs.FLING);
            mXSpring.setEndValue(sScreenBounds.left);
        }
        if (y + width >= sDispHeight) {
            mYSpring.setSpringConfig(SpringConfigs.FLING);
            mYSpring.setEndValue(sScreenBounds.bottom);
        }
        if (y - width <= 0) {
            mYSpring.setSpringConfig(SpringConfigs.FLING);
            mYSpring.setEndValue(sScreenBounds.top);
        }

        int minimumVelocityToReachSides = Util.dpToPx(100);
        //noinspection StatementWithEmptyBody
        if (!mWasRemoveLocked
                && Math.abs(mXSpring.getVelocity()) < minimumVelocityToReachSides
                && Math.abs(mYSpring.getVelocity()) < minimumVelocityToReachSides
                && !mDragging) {
            // Commenting temporarily TODO investigate if causing any issue
            // stickToWall();
        }
    }

    /**
     * Makes the web head stick to either side of the wall.
     */
    private void stickToWall() {
        if (mWindowParams.x > sDispWidth / 2) {
            mXSpring.setSpringConfig(SpringConfigs.FLING);
            mXSpring.setEndValue(sScreenBounds.right);
            Timber.e("Restored X");
        } else {
            mXSpring.setSpringConfig(SpringConfigs.FLING);
            mXSpring.setEndValue(sScreenBounds.left);
            Timber.e("Restored X");
        }

        if (mWindowParams.y < sScreenBounds.top) {
            mYSpring.setSpringConfig(SpringConfigs.FLING);
            mYSpring.setEndValue(sScreenBounds.top);
            Timber.e("Restored Y");
        } else if (mWindowParams.y > sScreenBounds.bottom) {
            mYSpring.setSpringConfig(SpringConfigs.FLING);
            mYSpring.setEndValue(sScreenBounds.bottom);
            Timber.e("Restored Y");
        }
    }

    /**
     * Method to move the current web head to wherever the last master was.
     */
    public void goToMasterTouchDownPoint() {
        setSpringConfig(SpringConfigs.FLING);
        mXSpring.setEndValue(masterDownX);
        mYSpring.setEndValue(masterDownY);
    }

    @Override
    protected void onMasterChanged(boolean master) {
        final String result = master ? "master" : "slave";
        Timber.d("%s became %s", getUrl(), result);
        if (master) {
            updateBadgeLocation();
        }
    }

    @Override
    protected void onSpawnLocationSet(int x, int y) {
        mXSpring.setCurrentValue(x);
        mYSpring.setCurrentValue(y);
    }

    @Override
    public void destroySelf(final boolean receiveCallback) {
        mDestroyed = true;
        WEB_HEAD_COUNT--;
        destroySprings();
        if (isCurrentlyAtRemoveWeb()) {
            if (Util.isLollipopAbove()) {
                closeWithAnimationL(receiveCallback);
            } else
                closeWithAnimation(receiveCallback);
        } else {
            if (receiveCallback) mContract.onWebHeadDestroyed(this, isLastWebHead());
            super.destroySelf(receiveCallback);
        }
    }

    /**
     * Animates and closes web head for pre L.
     *
     * @param receiveCallback True if clients should be notified
     */
    private void closeWithAnimation(final boolean receiveCallback) {
        final Animator reveal = getRevealInAnimator(mDeleteColor);
        mCircleBackground.clearElevation();
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIndicator.setVisibility(GONE);
                crossFadeFaviconToX();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (receiveCallback)
                            mContract.onWebHeadDestroyed(WebHead.this, isLastWebHead());
                        WebHead.super.destroySelf(receiveCallback);
                    }
                }, 200);
            }
        });
        reveal.setStartDelay(100);
        reveal.start();
    }

    /**
     * Animates and closes the web head. For android L and above so as to use elevation animations.
     *
     * @param receiveCallback True if clients should be notified
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void closeWithAnimationL(final boolean receiveCallback) {
        final Animator reveal = getRevealInAnimator(mDeleteColor);
        mCircleBackground
                .animate()
                .setDuration(50)
                .withLayer()
                .translationZ(0)
                .z(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        reveal.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                crossFadeFaviconToX();
                                mIndicator.setVisibility(GONE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (receiveCallback)
                                            mContract.onWebHeadDestroyed(WebHead.this, isLastWebHead());
                                        WebHead.super.destroySelf(receiveCallback);
                                    }
                                }, 200);
                            }
                        });
                        reveal.setStartDelay(100);
                        reveal.start();
                    }
                });
    }

    /**
     * Helper to know if the web head is currently locked in place with remove web head.
     *
     * @return true if locked, else false.
     */
    private boolean isCurrentlyAtRemoveWeb() {
        int rx = trashLockCoOrd()[0];
        int ry = trashLockCoOrd()[1];

        if (mWindowParams.x == rx && mWindowParams.y == ry) {
            return true;
        } else {
            double dist = dist(mWindowParams.x, mWindowParams.y, rx, ry);
            if (dist < Util.dpToPx(15)) {
                Timber.d("Adjusting positions");
                mWindowParams.x = rx;
                mWindowParams.y = ry;
                updateView();
                return true;
            } else return false;
        }
    }

    private void destroySprings() {
        mXSpring.destroy();
        mYSpring.destroy();
    }

    /**
     * A gesture listener class to monitor standard fling and click events on the web head view.
     */
    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            mWasClicked = true;
            if (Preferences.webHeadsCloseOnOpen(getContext()) && mContentGroup != null) {
                if (mWindowParams.x < sDispWidth / 2) {
                    mContentGroup.setPivotX(0);
                } else {
                    mContentGroup.setPivotX(mContentGroup.getWidth());
                }
                mContentGroup.setPivotY((float) (mContentGroup.getHeight() * 0.75));
                try {
                    mScaleSpring.setAtRest();
                } catch (Exception e) {
                    Timber.e("Error : %s", e.getMessage());
                }
                mContentGroup.animate()
                        .scaleX(0.0f)
                        .scaleY(0.0f)
                        .alpha(0.5f)
                        .withLayer()
                        .setDuration(250)
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
            mContract.onWebHeadClick(WebHead.this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mDragging = false;

            float[] adjustedVelocities = mMovementTracker.getAdjustedVelocities(velocityX, velocityY);

            if (adjustedVelocities == null) {
                float[] down = new float[]{e1.getRawX(), e1.getRawY()};
                float[] up = new float[]{e2.getRawX(), e2.getRawY()};
                adjustedVelocities = MovementTracker.adjustVelocities(down, up, velocityX, velocityY);
            }

            if (adjustedVelocities != null) {
                mWasFlung = true;

                velocityX = interpolateXVelocity(e2, adjustedVelocities[0]);

                mXSpring.setSpringConfig(SpringConfigs.DRAG);
                mYSpring.setSpringConfig(SpringConfigs.DRAG);

                mXSpring.setVelocity(velocityX);
                mYSpring.setVelocity(adjustedVelocities[1]);
                return true;
            }
            return false;
        }

        /**
         * Attempts to figure out the correct X velocity by using {@link #MINIMUM_HORIZONTAL_FLING_VELOCITY}
         * This is needed since if we blindly upscale the velocity, web heads will jump too quickly
         * when near screen edges. This method proportionally upscales the velocity based on where the
         * web head was released to prevent quick  jumps.
         *
         * @param upEvent   Motion event of last touch release
         * @param velocityX original velocity
         * @return Scaled velocity
         */
        private float interpolateXVelocity(MotionEvent upEvent, float velocityX) {
            float x = upEvent.getRawX() / sDispWidth;
            if (velocityX > 0) {
                velocityX = Math.max(velocityX, MINIMUM_HORIZONTAL_FLING_VELOCITY * (1 - x));
            } else {
                velocityX = -Math.max(velocityX, MINIMUM_HORIZONTAL_FLING_VELOCITY * x);
            }
            return velocityX;
        }
    }

    @Override
    public String toString() {
        return "Webhead " + getUrl() + "master: " + String.valueOf(isMaster());
    }
}
