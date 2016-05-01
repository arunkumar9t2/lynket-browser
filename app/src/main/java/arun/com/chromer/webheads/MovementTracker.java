package arun.com.chromer.webheads;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import java.util.Collection;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * A helper class for tracking web heads movements. It uses a size limited queue structure to log
 * user's move coordinates. The sized queue is then used to analyze the gesture performed and provide a
 * end point on the either sides of the display. By using this end point, it is possible to know
 * where the gesture would end if it was continued along the tangent of the curve.
 */
class MovementTracker {
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_RIGHT = 2;
    private static final int BOTTOM_LEFT = 3;
    private static final int TOP_LEFT = 4;

    private static int mDispHeight = 0;
    private static int mDispWidth = 0;

    private int mTrackingSize = 0;
    private static int mEndOffset = 0;

    private final SizedQueue<Coordinate> mPoints;

    MovementTracker(int trackingSize, int dispHeight, int dispWidth, int endOffset) {
        mTrackingSize = trackingSize;
        mPoints = new SizedQueue<>(mTrackingSize);
        mDispHeight = dispHeight;
        mDispWidth = dispWidth;
        mEndOffset = endOffset;
        Timber.d("Created tracker of size %d", trackingSize);
    }

    /**
     * Adds a motion event to the tracker.
     *
     * @param event The event to be added.
     */
    public void addMovement(@NonNull MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        mPoints.add(new Coordinate(x, y));
    }

    /**
     * Clear the tracking queue when user begins the gesture.
     */
    public void onDown() {
        mPoints.clear();
    }

    /**
     * Clear the tracking queue when user ends the gesture.
     */
    public void onUp() {
        mPoints.clear();
    }

    /**
     * Calculates the end point using point (upX,upY) with angle and direction.
     * The mathematics used is structured like this
     * - Base formula for angle of triangle is  Tan(theta) = OPP/ADJ.
     * - If (theta) is known then OPP = Tan(theta) * ADJ.
     * - ADJ is calculated simply by knowing the current upX and doing (screen width - upX)
     * - To know 'Y', do (upY - OPP) (Depends on direction)
     *
     * @param upX       X value where the user gesture ended
     * @param upY       Y value where the user gesture ended
     * @param angle     Angle between the start and end point
     * @param direction Direction of line between start and end point
     * @return
     */
    private static Coordinate getProjection(double upX, double upY, double angle, int direction) {
        Coordinate point = new Coordinate();

        // Timber.v(String.valueOf(direction) + " " + angle);

        if (direction == TOP_RIGHT || direction == BOTTOM_RIGHT) {
            point.x = (int) (mDispWidth - mEndOffset * 0.8);
            double dX = mDispWidth - upX;

            int dY;
            if (direction == TOP_RIGHT) {
                dY = (int) (upY - (dX * Math.tan(Math.toRadians(angle))));
                point.y = Math.max(0, dY);
            } else {
                dY = (int) (upY + (dX * Math.tan(Math.toRadians(angle))));
                point.y = (int) Math.min(mDispHeight * 0.8, dY);
            }
        } else {
            point.x = (int) (0 - mEndOffset * 0.2);
            int dY;
            if (direction == TOP_LEFT) {
                dY = (int) (upY - (upX * Math.tan(Math.toRadians(angle))));
                point.y = Math.max(0, dY);
            } else {
                dY = (int) (upY + (upX * Math.tan(Math.toRadians(angle))));
                point.y = (int) Math.min(mDispHeight * 0.8, dY);
            }
        }

        return point;
    }

    /**
     * Considers two points as ends of directional line placed in cartesian coordinate system. Calculates the angle
     * with respect to X and Y and then normalizes it based on the direction. Returns the point of
     * interception of the Y axis if the line is assumed to proceed indefinitely.
     *
     * @param p1 Starting of directional line
     * @param p2 Ending of the directional line
     * @return Interception point on the Y axis if the line proceeds indefinitely.
     */
    public static Coordinate calculateTrajectory(Coordinate p1, Coordinate p2) {
        // Timber.v("From %s to %s", p1.toString(), p2.toString());

        float downX = p1.x;
        float downY = p1.y;

        float upX = p2.x;
        float upY = p2.y;

        double angle = Math.toDegrees(Math.atan2(upY - downY, upX - downX));
        if (angle < 0) angle = 360 - (360 + angle);

        Coordinate projectedPoint = null;

        if (upX >= downX && upY >= downY) {
            // Bottom right
            projectedPoint = getProjection(upX, upY, angle, BOTTOM_RIGHT);
        } else if (upX >= downX && upY <= downY) {
            // Top right
            projectedPoint = getProjection(upX, upY, angle, TOP_RIGHT);
        } else if (upX <= downX && upY <= downY) {
            // Top left
            angle = 180 - angle;
            projectedPoint = getProjection(upX, upY, angle, TOP_LEFT);
        } else if (upX <= downX && upY >= downY) {
            // Bottom left
            angle = 180 - angle;
            projectedPoint = getProjection(upX, upY, angle, BOTTOM_LEFT);
        }
        return projectedPoint;
    }


    public static Coordinate adjustVelocities(Coordinate p1, Coordinate p2, float xVelocity, float yVelocity) {
        float downX = p1.x;
        float downY = p1.y;

        float upX = p2.x;
        float upY = p2.y;

        Coordinate velocities = new Coordinate();

        if (upX >= downX && upY >= downY) {
            // Bottom right
            // Timber.d("BR");
            velocities.x = positive(xVelocity);
            velocities.y = positive(yVelocity);
        } else if (upX >= downX && upY <= downY) {
            // Top right
            // Timber.d("TR");
            velocities.x = positive(xVelocity);
            velocities.y = negate(yVelocity);
        } else if (upX <= downX && upY <= downY) {
            // Top left
            // Timber.d("TL");
            velocities.x = negate(xVelocity);
            velocities.y = negate(yVelocity);
        } else if (upX <= downX && upY >= downY) {
            // Bottom left
            // Timber.d("BL");
            velocities.x = negate(xVelocity);
            velocities.y = positive(yVelocity);
        }
        return velocities;
    }

    private static float negate(float value) {
        return value > 0 ? -1 * value : value;
    }

    private static float positive(float value) {
        return value < 0 ? -1 * value : value;
    }

    /**
     * By using the tracked gesture points, calculates the fling end point. This is done by assuming
     * a line from the 75% of the tracked points to last tracked point. Then calculateTrajectory is
     * used to find the end point.
     * <p/>
     * The threshold is assumed to be at 75% of the tracked length. Increased values would mean
     * accurate direction but can be prone to errors as end points can have spiked data.
     *
     * @return Point where the fling would have ended on Y axis.
     */
    @Nullable
    public Coordinate getProjection() {
        int trackingThreshold = (int) (0.25 * mTrackingSize);
        Coordinate projectedPoint;
        if (mPoints.size() >= trackingThreshold) {
            Coordinate up = mPoints.getLast();
            Coordinate down = mPoints.get(mPoints.size() - trackingThreshold);

            projectedPoint = calculateTrajectory(down, up);
        } else {
            projectedPoint = null;
        }
        return projectedPoint;
    }

    public Coordinate getAdjustedVelocities(float xVelocity, float yVelocity) {
        int trackingThreshold = (int) (0.25 * mTrackingSize);
        Coordinate velocities;
        if (mPoints.size() >= trackingThreshold) {
            Coordinate up = mPoints.getLast();
            Coordinate down = mPoints.get(mPoints.size() - trackingThreshold);

            velocities = adjustVelocities(down, up, xVelocity, yVelocity);
        } else {
            velocities = null;
        }
        return velocities;
    }

    @Override
    public String toString() {
        return mPoints != null && mPoints.size() != 0 ? mPoints.toString() : "EMPTY";
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

    public SizedQueue(int limit) {
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