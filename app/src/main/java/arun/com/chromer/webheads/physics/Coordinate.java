package arun.com.chromer.webheads.physics;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

public class Coordinate {
    public float x;
    public float y;

    Coordinate() {

    }

    Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate FromMotionEvent(@NonNull MotionEvent event) {
        return new Coordinate(event.getRawX(), event.getRawY());
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
