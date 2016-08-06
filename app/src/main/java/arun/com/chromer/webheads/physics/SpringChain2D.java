package arun.com.chromer.webheads.physics;

import android.support.annotation.NonNull;

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

    private final int DIFF = Util.dpToPx(4);

    private SpringChain2D() {
    }

    public static SpringChain2D create() {
        return new SpringChain2D();
    }

    public void setMasterSprings(@NonNull Spring xMaster, @NonNull Spring yMaster) {
        mXSprings.clear();
        mYSprings.clear();
        mXMasterSpring = xMaster;
        mYMasterSpring = yMaster;
        mXMasterSpring.addListener(this);
        mYMasterSpring.addListener(this);
    }

    public void addSlaveSprings(@NonNull Spring xSpring, @NonNull Spring ySpring) {
        mXSprings.add(xSpring);
        mYSprings.add(ySpring);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        final int masterX = (int) mXMasterSpring.getCurrentValue();
        final int masterY = (int) mYMasterSpring.getCurrentValue();
        performMove(masterX, masterY);
    }

    public void performMove(int masterX, int masterY) {
        int displacement = 0;
        Iterator lit = mXSprings.descendingIterator();
        while (lit.hasNext()) {
            final Spring s = (Spring) lit.next();
            displacement += DIFF;
            s.setEndValue(masterX + displacement);
        }
        displacement = 0;
        lit = mYSprings.descendingIterator();
        while (lit.hasNext()) {
            final Spring s = (Spring) lit.next();
            displacement += DIFF;
            s.setEndValue(masterY + displacement);
        }
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
