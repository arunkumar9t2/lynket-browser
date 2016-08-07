package arun.com.chromer.webheads.physics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;

import java.util.Iterator;
import java.util.LinkedList;

import arun.com.chromer.util.Util;


/**
 * Created by Arun on 06/08/2016.
 * Custom spring chain helper that simplifies maintaining 2 separate chains for X and Y axis.
 */
public class SpringChain2D implements SpringListener {
    private final LinkedList<Spring> mXSprings = new LinkedList<>();
    private final LinkedList<Spring> mYSprings = new LinkedList<>();

    private Spring mXMasterSpring;
    private Spring mYMasterSpring;

    private final int sDispWidth;

    private static final int xDiff = Util.dpToPx(4);
    private static final int yDiff = Util.dpToPx(2);

    private SpringChain2D(int dispWidth) {
        this.sDispWidth = dispWidth;
    }

    public static SpringChain2D create(Context context) {
        final DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return new SpringChain2D(metrics.widthPixels);
    }

    public void setMasterSprings(@NonNull Spring xMaster, @NonNull Spring yMaster) {
        mXMasterSpring = xMaster;
        mYMasterSpring = yMaster;
        mXMasterSpring.addListener(this);
        mYMasterSpring.addListener(this);
    }

    public void clear() {
        mXSprings.clear();
        mYSprings.clear();
    }

    public void addSlaveSprings(@NonNull Spring xSpring, @NonNull Spring ySpring) {
        mXSprings.add(xSpring);
        mYSprings.add(ySpring);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        final int masterX = (int) mXMasterSpring.getCurrentValue();
        final int masterY = (int) mYMasterSpring.getCurrentValue();
        performGroupMove(masterX, masterY);
    }

    public void rest() {
        Iterator lit = mXSprings.descendingIterator();
        while (lit.hasNext()) {
            final Spring s = (Spring) lit.next();
            s.setAtRest();
        }
        lit = mYSprings.descendingIterator();
        while (lit.hasNext()) {
            final Spring s = (Spring) lit.next();
            s.setAtRest();
        }
    }

    public void performGroupMove(int masterX, int masterY) {
        int displacement = 0;
        Iterator lit = mXSprings.descendingIterator();
        while (lit.hasNext()) {
            final Spring s = (Spring) lit.next();
            if (isRight(masterX)) {
                displacement += xDiff;
            } else {
                displacement -= xDiff;
            }
            s.setEndValue(masterX + displacement);
        }

        displacement = 0;
        lit = mYSprings.descendingIterator();
        while (lit.hasNext()) {
            final Spring s = (Spring) lit.next();
            displacement += yDiff;
            s.setEndValue(masterY + displacement);
        }
    }

    /**
     * Used to determine if the given pixel is to the left or the right of the screen.
     *
     * @return true if right
     */
    private boolean isRight(int x) {
        return x > (sDispWidth / 2);
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
}
