/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.webheads.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;

import java.util.Timer;
import java.util.TimerTask;

import arun.com.chromer.settings.Preferences;
import arun.com.chromer.util.Utils;
import arun.com.chromer.webheads.physics.MovementTracker;
import arun.com.chromer.webheads.physics.SpringConfigs;
import arun.com.chromer.webheads.ui.WebHeadContract;
import timber.log.Timber;

import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;
import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static arun.com.chromer.webheads.physics.SpringConfigs.FLING;
import static arun.com.chromer.webheads.ui.views.Trashy.MAGNETISM_THRESHOLD;

/**
 * Web head object which adds draggable and gesture functionality.
 */
@SuppressLint("ViewConstructor")
public class WebHead extends BaseWebHead implements SpringListener {
    private static final float TOUCH_DOWN_SCALE = 1f;
    private static final float TOUCH_UP_SCALE = 1f;
    // Coordinate of remove web head that we can lock on to.
    private int[] trashLockingCoordinates;
    // True when being dragged, otherwise false
    private boolean dragging;
    // True when attached to remove view, otherwise false
    private boolean wasLockedToRemove;
    // True when fling detected and false on new touch event
    private boolean wasFlung;
    // True when click was detected, and false on new touch event
    private boolean wasClicked;
    // True when touched down and false otherwise
    private boolean scaledDown;
    // If web head is resting on the sides
    private boolean isCoasting = false;
    // Minimum horizontal velocity that we need to move the web head from one end of the scree to another
    private static int MINIMUM_HORIZONTAL_FLING_VELOCITY = 0;
    // Touch slop of the device
    private final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    // Gesture detector to recognize fling and click on web heads
    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetectorListener());
    // Individual springs to control X, Y
    private Spring xSpring, ySpring;
    // As per material guidelines, fast out slow in recommended for shrink/expand animations
    private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private float posX, posY;
    private int initialDownX, initialDownY;

    private static final Timer timer = new Timer();
    private TimerTask coastingTask;
    // The interaction listener that clients can provide to listen for events on webhead.
    private final WebHeadContract webHeadContract;
    /**
     * Movement tracker instance that is used to adjust X and Y velocity calculated by {@link #gestureDetector}.
     * This is needed since sometimes velocities coming from
     * {@link GestureDetectorListener#onFling(MotionEvent, MotionEvent, float, float)}has wrong polarity.
     */
    private static MovementTracker movementTracker;
    private boolean fromAmp;
    private boolean incognito;

    /**
     * Inits the web head and attaches to the system window. It is assumed that draw over other apps
     * permission is granted for 6.0+.
     *
     * @param context  Service
     * @param url      Url the web head will carry
     * @param contract for communicating to events on the webhead
     */
    public WebHead(@NonNull Context context, @NonNull String url, @NonNull WebHeadContract contract) {
        super(context, url);
        webHeadContract = contract;
        master = true;
        movementTracker = MovementTracker.obtain();
        calcVelocities();
        setupSprings();
        scheduleCoastingTask();
    }

    private void calcVelocities() {
        if (MINIMUM_HORIZONTAL_FLING_VELOCITY == 0) {
            final int scaledScreenWidthDp = (getResources().getConfiguration().screenWidthDp * 10);
            MINIMUM_HORIZONTAL_FLING_VELOCITY = Utils.dpToPx(scaledScreenWidthDp);
        }
    }

    private void setupSprings() {
        ySpring = webHeadContract.newSpring();
        ySpring.addListener(this);
        xSpring = webHeadContract.newSpring();
        xSpring.addListener(this);
        setContentScale(0.0f);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Don't react to any touch event and consume it when we are being destroyed, if we are a
        // a slave or if we are in queue.
        if (destroyed || !master || inQueue) return true;
        try {
            // Reset gesture flag on each event
            wasFlung = false;
            wasClicked = false;

            // Let gesture detector intercept events, needed for fling and click
            gestureDetector.onTouchEvent(event);

            if (wasClicked) return true;

            switch (event.getAction()) {
                case ACTION_DOWN:
                    handleTouchDown(event);
                    break;
                case ACTION_MOVE:
                    handleMove(event);
                    break;
                case ACTION_UP:
                case ACTION_CANCEL:
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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        trashLockingCoordinates = null;
        MINIMUM_HORIZONTAL_FLING_VELOCITY = 0;
        spawnCoordSet = false;
        screenBounds = null;
        calcVelocities();
        Utils.doAfterLayout(this, this::setInitialSpawnLocation);
    }

    private void handleTouchDown(@NonNull MotionEvent event) {
        dragging = false;

        movementTracker.onDown();

        initialDownX = windowParams.x;
        initialDownY = windowParams.y;

        posX = event.getRawX();
        posY = event.getRawY();

        if (master) {
            masterDownX = (int) posX;
            masterDownY = (int) posY;
        }

        touchDown();

        cancelCoastingTask();
    }

    /**
     * Responsible for moving the web heads around and for locking/unlocking the web head to
     * remove view.
     *
     * @param event the touch event
     */
    private void handleMove(@NonNull MotionEvent event) {
        movementTracker.addMovement(event);

        float offsetX = event.getRawX() - posX;
        float offsetY = event.getRawY() - posY;

        if (Math.hypot(offsetX, offsetY) > touchSlop) {
            dragging = true;
        }

        if (dragging) {
            getTrashy().reveal();

            userManuallyMoved = true;

            int x = (int) (initialDownX + offsetX);
            int y = (int) (initialDownY + offsetY);

            if (isNearRemoveCircle(x, y)) {
                getTrashy().grow();
                touchUp();

                xSpring.setSpringConfig(SpringConfigs.SNAP);
                ySpring.setSpringConfig(SpringConfigs.SNAP);

                xSpring.setEndValue(trashLockCoOrd()[0]);
                ySpring.setEndValue(trashLockCoOrd()[1]);

            } else {
                getTrashy().shrink();

                xSpring.setSpringConfig(SpringConfigs.DRAG);
                ySpring.setSpringConfig(SpringConfigs.DRAG);

                xSpring.setCurrentValue(x);
                ySpring.setCurrentValue(y);

                touchDown();
            }
        }
    }

    private boolean handleTouchUp() {
        if (wasLockedToRemove) {
            // If head was locked onto a remove bubble before, then kill ourselves
            destroySelf(true);
            return true;
        }
        dragging = false;

        movementTracker.onUp();

        if (!wasFlung && userManuallyMoved) {
            stickToWall();
        }
        touchUp();
        // hide remove view
        Trashy.disappear();
        scheduleCoastingTask();
        return false;
    }

    /**
     * Schedules a coasting task that will make the master web head move further away from the screen.
     */
    private void scheduleCoastingTask() {
        if (!isMaster()) {
            return;
        }
        cancelCoastingTask();
        coastingTask = new TimerTask() {
            @Override
            public void run() {
                Timber.v("Coasting active");
                isCoasting = true;
                final int halfWidth = getWidth() / 4;
                if (screenBounds != null)
                    if (windowParams.x < dispWidth / 2) {
                        xSpring.setEndValue(screenBounds.left - halfWidth);
                    } else {
                        xSpring.setEndValue(screenBounds.right + halfWidth);
                    }
            }
        };
        Timber.v("Scheduled a coasting task");
        timer.schedule(coastingTask, 6000);
    }

    private void cancelCoastingTask() {
        isCoasting = false;
        if (coastingTask != null) {
            coastingTask.cancel();
        }
        timer.purge();
    }

    /**
     * Returns the coordinate where the web head should lock to the remove web heads.
     * Calculated once and reused there after.
     *
     * @return array of x and y.
     */
    private int[] trashLockCoOrd() {
        if (trashLockingCoordinates == null) {
            int[] removeCentre = getTrashy().getCenterCoordinates();
            int offset = getWidth() / 2;
            int x = removeCentre[0] - offset;
            int y = removeCentre[1] - offset;
            trashLockingCoordinates = new int[]{x, y};
        }
        return trashLockingCoordinates;
    }

    /**
     * Used to determine if the web head is in vicinity of remove web head view.
     *
     * @param x Current x position of web head
     * @param y Current y position of web head
     * @return true if near, false other wise
     */
    private boolean isNearRemoveCircle(int x, int y) {
        final int[] p = getTrashy().getCenterCoordinates();
        final int rX = p[0];
        final int rY = p[1];

        final int offset = getWidth() / 2;
        x += offset;
        y += offset;

        if (dist(rX, rY, x, y) < MAGNETISM_THRESHOLD) {
            wasLockedToRemove = true;
            badgeView.setVisibility(INVISIBLE);
            webHeadContract.onMasterLockedToTrashy();
            return true;
        } else {
            wasLockedToRemove = false;
            badgeView.setVisibility(VISIBLE);
            webHeadContract.onMasterReleasedFromTrashy();
            return false;
        }
    }

    private float dist(double x1, double y1, double x2, double y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Used to set the scale of {@link #contentRoot} for use in reveal and shrink animations. This
     * one directly sets the vales, for animations refer {@link #setContentScale(float)}
     *
     * @param scale Scale to set
     */
    private void setContentScale(final float scale) {
        contentRoot.setScaleX(scale);
        contentRoot.setScaleY(scale);
    }

    /**
     * Same as {@link #setContentScale(float)} but with animations.
     *
     * @param scale
     */
    private void animateContentScale(final float scale) {
        animateContentScale(scale, null);
    }

    /**
     * Same as {@link #setContentScale(float)} but with animations and ability to listen when animation
     * finishes by giving a Runnable. {@param end}
     *
     * @param scale     Scale to set
     * @param endAction End runnable to execute after animations
     */
    private void animateContentScale(final float scale, @Nullable final Runnable endAction) {
        contentRoot.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setInterpolator(new SpringInterpolator(0.2, 5))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (endAction != null) {
                            endAction.run();
                        }
                    }
                }).start();
    }

    public void reveal(@Nullable final Runnable endAction) {
        Utils.doAfterLayout(this, () -> {
            setInitialSpawnLocation();
            Timber.d("Reveal %s", website.url);
            animateContentScale(TOUCH_UP_SCALE, endAction);
            scaledDown = false;
        });
    }

    private void touchDown() {
        if (!scaledDown) {
            scaledDown = true;
        }
    }

    private void touchUp() {
        if (scaledDown) {
            scaledDown = false;
        }
    }

    @NonNull
    public Spring getXSpring() {
        return xSpring;
    }

    @NonNull
    public Spring getYSpring() {
        return ySpring;
    }

    public void setSpringConfig(@NonNull final SpringConfig config) {
        xSpring.setSpringConfig(config);
        ySpring.setSpringConfig(config);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        windowParams.x = (int) xSpring.getCurrentValue();
        windowParams.y = (int) ySpring.getCurrentValue();
        updateView();
        if (master) {
            webHeadContract.onMasterWebHeadMoved(windowParams.x, windowParams.y);
            checkBounds();
            updateBadgeLocation();
        }
    }

    @SuppressLint("RtlHardcoded")
    private void updateBadgeLocation() {
        final LayoutParams params = (LayoutParams) badgeView.getLayoutParams();
        if (windowParams.x > dispWidth / 2) {
            params.gravity = TOP | LEFT;
        } else {
            params.gravity = TOP | RIGHT;
        }
        badgeView.setLayoutParams(params);
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
        if (dragging || screenBounds == null || !master || inQueue || isCoasting) {
            return;
        }

        final int x = windowParams.x;
        final int y = windowParams.y;

        final int width = getWidth();

        if (x + width >= dispWidth) {
            xSpring.setSpringConfig(FLING);
            xSpring.setEndValue(screenBounds.right);
        }
        if (x - width <= 0) {
            xSpring.setSpringConfig(FLING);
            xSpring.setEndValue(screenBounds.left);
        }
        if (y + width >= dispHeight) {
            ySpring.setSpringConfig(FLING);
            ySpring.setEndValue(screenBounds.bottom);
        }
        if (y - width <= 0) {
            ySpring.setSpringConfig(FLING);
            ySpring.setEndValue(screenBounds.top);
        }

        /*final int minimumVelocityToReachSides = Utils.dpToPx(100);
        //noinspection StatementWithEmptyBody
        if (!wasLockedToRemove
                && Math.abs(xSpring.getVelocity()) < minimumVelocityToReachSides
                && Math.abs(ySpring.getVelocity()) < minimumVelocityToReachSides
                && !dragging) {
            // Commenting temporarily
            // stickToWall();
        }*/
    }

    /**
     * Makes the web head stick to either side of the wall.
     */
    private void stickToWall() {
        if (windowParams.x > dispWidth / 2) {
            xSpring.setSpringConfig(FLING);
            xSpring.setEndValue(screenBounds.right);
        } else {
            xSpring.setSpringConfig(FLING);
            xSpring.setEndValue(screenBounds.left);
        }
        if (windowParams.y < screenBounds.top) {
            ySpring.setSpringConfig(FLING);
            ySpring.setEndValue(screenBounds.top);
        } else if (windowParams.y > screenBounds.bottom) {
            ySpring.setSpringConfig(FLING);
            ySpring.setEndValue(screenBounds.bottom);
        }
    }

    /**
     * Method to move the current web head to wherever the last master was.
     */
    public void goToMasterTouchDownPoint() {
        setSpringConfig(FLING);
        xSpring.setEndValue(masterDownX);
        ySpring.setEndValue(masterDownY);
    }

    @Override
    protected void onMasterChanged(final boolean master) {
        if (master) {
            updateBadgeLocation();
            updateBadgeColors(webHeadColor);
            isCoasting = false;
        }
    }

    @Override
    protected void onSpawnLocationSet(final int x, final int y) {
        try {
            ySpring.setCurrentValue(y);
            xSpring.setCurrentValue(x);
        } catch (IllegalArgumentException e) {
            // Should never happen
            Timber.e(e);
        }
    }

    @Override
    public void destroySelf(final boolean receiveCallback) {
        cancelCoastingTask();
        destroyed = true;
        WEB_HEAD_COUNT--;
        destroySprings();
        if (isCurrentlyAtRemoveWeb()) {
            if (Utils.isLollipopAbove()) {
                closeWithAnimationL(receiveCallback);
            } else
                closeWithAnimation(receiveCallback);
        } else {
            if (receiveCallback) webHeadContract.onWebHeadDestroyed(this, isLastWebHead());
            super.destroySelf(receiveCallback);
        }
    }

    /**
     * Animates and closes web head for pre L.
     *
     * @param receiveCallback True if clients should be notified
     */
    private void closeWithAnimation(final boolean receiveCallback) {
        revealInAnimation(deleteColor,
                () -> {
                    if (circleBg != null && indicator != null) {
                        circleBg.clearElevation();
                        indicator.setVisibility(GONE);
                    }
                    crossFadeFaviconToX();
                },
                () -> new Handler()
                        .postDelayed(() -> {
                            if (receiveCallback)
                                webHeadContract.onWebHeadDestroyed(WebHead.this, isLastWebHead());
                            WebHead.super.destroySelf(receiveCallback);
                        }, 200));
    }

    /**
     * Animates and closes the web head. For android L and above so as to use elevation animations.
     *
     * @param receiveCallback True if clients should be notified
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void closeWithAnimationL(final boolean receiveCallback) {
        circleBg.animate()
                .setDuration(50)
                .withLayer()
                .translationZ(0)
                .z(0)
                .withEndAction(() ->
                        revealInAnimation(deleteColor,
                                () -> {
                                    crossFadeFaviconToX();
                                    if (indicator != null)
                                        indicator.setVisibility(GONE);
                                },
                                () -> new Handler().postDelayed(() -> {
                                    if (receiveCallback)
                                        webHeadContract.onWebHeadDestroyed(WebHead.this, isLastWebHead());
                                    WebHead.super.destroySelf(receiveCallback);
                                }, 200)));
    }

    /**
     * Helper to know if the web head is currently locked in place with remove web head.
     * NOTE: Will also adjust the web head to remove web head center if it is in close vicinity.
     *
     * @return true if locked, else false.
     */
    private boolean isCurrentlyAtRemoveWeb() {
        final int rx = trashLockCoOrd()[0];
        final int ry = trashLockCoOrd()[1];
        if (windowParams.x == rx && windowParams.y == ry) {
            return true;
        } else {
            final double dist = dist(windowParams.x, windowParams.y, rx, ry);
            if (dist < Utils.dpToPx(15)) {
                Timber.d("Adjusting positions");
                windowParams.x = rx;
                windowParams.y = ry;
                updateView();
                return true;
            } else return false;
        }
    }

    private void destroySprings() {
        xSpring.destroy();
        ySpring.destroy();
    }

    public boolean isFromAmp() {
        return fromAmp;
    }

    public void setFromAmp(boolean fromAmp) {
        this.fromAmp = fromAmp;
    }

    public void setIncognito(boolean incognito) {
        this.incognito = incognito;
    }

    public boolean isIncognito() {
        return incognito;
    }

    /**
     * A gesture listener class to monitor standard fling and click events on the web head view.
     */
    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            webHeadContract.onMasterLongClick();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            wasClicked = true;
            if (Preferences.get(getContext()).webHeadsCloseOnOpen() && contentRoot != null) {
                if (windowParams.x < dispWidth / 2) {
                    contentRoot.setPivotX(0);
                } else {
                    contentRoot.setPivotX(contentRoot.getWidth());
                }
                contentRoot.setPivotY((float) (contentRoot.getHeight() * 0.75));
                contentRoot.animate()
                        .scaleX(0.0f)
                        .scaleY(0.0f)
                        .alpha(0.5f)
                        .withLayer()
                        .setDuration(125)
                        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .withEndAction(this::sendCallback)
                        .start();
                // Store the touch down point if its master
                if (master) {
                    masterDownX = windowParams.x;
                    masterDownY = windowParams.y;
                }
            } else sendCallback();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            dragging = false;

            float[] adjustedVelocities = movementTracker.getAdjustedVelocities(velocityX, velocityY);
            if (adjustedVelocities == null) {
                float[] down = new float[]{e1.getRawX(), e1.getRawY()};
                float[] up = new float[]{e2.getRawX(), e2.getRawY()};
                adjustedVelocities = MovementTracker.adjustVelocities(down, up, velocityX, velocityY);
            }
            if (adjustedVelocities != null) {
                wasFlung = true;

                velocityX = interpolateXVelocity(e2, adjustedVelocities[0]);

                xSpring.setSpringConfig(SpringConfigs.DRAG);
                ySpring.setSpringConfig(SpringConfigs.DRAG);

                xSpring.setVelocity(velocityX);
                ySpring.setVelocity(adjustedVelocities[1]);
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
            float x = upEvent.getRawX() / dispWidth;
            if (velocityX > 0) {
                velocityX = Math.max(velocityX, MINIMUM_HORIZONTAL_FLING_VELOCITY * (1 - x));
            } else {
                velocityX = -Math.max(velocityX, MINIMUM_HORIZONTAL_FLING_VELOCITY * x);
            }
            return velocityX;
        }

        private void sendCallback() {
            Trashy.disappear();
            webHeadContract.onWebHeadClick(WebHead.this);
        }
    }

    @Override
    public String toString() {
        return "Webhead " + getUrl() + "master: " + String.valueOf(isMaster());
    }

    static class SpringInterpolator implements android.view.animation.Interpolator {
        double amp = 1;
        double frequency = 10;

        SpringInterpolator(double amplitude, double frequency) {
            amp = amplitude;
            this.frequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time / amp) *
                    Math.cos(frequency * time) + 1);
        }
    }
}
