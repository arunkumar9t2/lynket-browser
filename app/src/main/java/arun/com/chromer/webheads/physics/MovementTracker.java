package arun.com.chromer.webheads.physics;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A helper class for tracking web heads movements. This is needed to correctly apply polarity on calculated velocity
 * by velocity tracker. For example when web head is moved from left to right and top to bottom, the
 * X and Y velocity should be positive. Sometimes that is not the case with raw values given by {@link android.view.VelocityTracker}
 */
public class MovementTracker {
    private static MovementTracker sInstance;
    private final SizedQueue<Float> mXPoints;
    private final SizedQueue<Float> mYPoints;
    private int mTrackingSize = 0;

    private MovementTracker() {
        mTrackingSize = 10;
        mXPoints = new SizedQueue<>(mTrackingSize);
        mYPoints = new SizedQueue<>(mTrackingSize);
    }

    @NonNull
    public static MovementTracker obtain() {
        if (sInstance == null) {
            sInstance = new MovementTracker();
        }
        return sInstance;
    }

    public static float[] adjustVelocities(float[] p1, float[] p2, float xVelocity, float yVelocity) {
        float downX = p1[0];
        float downY = p1[1];

        float upX = p2[0];
        float upY = p2[1];

        float x = 0, y = 0;

        if (upX >= downX && upY >= downY) {
            // Bottom right
            x = positive(xVelocity);
            y = positive(yVelocity);
        } else if (upX >= downX && upY <= downY) {
            // Top right
            x = positive(xVelocity);
            y = negate(yVelocity);
        } else if (upX <= downX && upY <= downY) {
            // Top left
            x = negate(xVelocity);
            y = negate(yVelocity);
        } else if (upX <= downX && upY >= downY) {
            // Bottom left
            x = negate(xVelocity);
            y = positive(yVelocity);
        }
        return new float[]{x, y};
    }

    private static float negate(float value) {
        return value > 0 ? -value : value;
    }

    private static float positive(float value) {
        return Math.abs(value);
    }

    /**
     * Adds a motion event to the tracker.
     *
     * @param event The event to be added.
     */
    public void addMovement(@NonNull MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        mXPoints.add(x);
        mYPoints.add(y);
    }

    /**
     * Clear the tracking queue when user begins the gesture.
     */
    public void onDown() {
        mXPoints.clear();
        mYPoints.clear();
    }

    /**
     * Clear the tracking queue when user ends the gesture.
     */
    public void onUp() {
        mXPoints.clear();
        mYPoints.clear();
    }

    public float[] getAdjustedVelocities(float xVelocity, float yVelocity) {
        int trackingThreshold = (int) (0.25 * mTrackingSize);
        float[] velocities;
        if (mXPoints.size() >= trackingThreshold) {
            int downIndex = mXPoints.size() - trackingThreshold;

            float[] up = new float[]{mXPoints.getLast(), mYPoints.getLast()};
            float[] down = new float[]{mXPoints.get(downIndex), mYPoints.get(downIndex)};

            velocities = adjustVelocities(down, up, xVelocity, yVelocity);
        } else {
            velocities = null;
        }
        return velocities;
    }

    @Override
    public String toString() {
        return mXPoints.toString() + mYPoints.toString();
    }
}

/**
 * A size limited queue structure that evicts the queue head when maximum queue size is reached. At
 * any instant the queue is equal or less than the max queue size.
 *
 * @param <E>
 */
class SizedQueue<E> extends LinkedList<E> {
    /**
     * The maximum size of queue
     */
    private final int limit;

    SizedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) {
            super.remove();
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException("Not implemented, use add()");
    }

    @Override
    public void add(int location, E object) {
        throw new UnsupportedOperationException("Not implemented, use add()");
    }

    @Override
    public void addFirst(E object) {
        throw new UnsupportedOperationException("Not implemented, use add()");
    }

    @Override
    public void addLast(E object) {
        throw new UnsupportedOperationException("Not implemented, use add()");
    }

    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        throw new UnsupportedOperationException("Not implemented, use add()");
    }
}