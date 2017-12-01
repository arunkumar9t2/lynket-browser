/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

/**
 * Created by Arun on 03/02/2016.
 */
@SuppressLint("ViewConstructor")
public class Trashy extends FrameLayout {
    static final double MAGNETISM_THRESHOLD = Utils.dpToPx(120);
    private static WindowManager windowManager;
    private static Trashy INSTANCE;

    private WindowManager.LayoutParams windowParams;

    private int dispWidth;
    private int dispHeight;

    private Spring scaleSpring;
    private SpringSystem springSystem;

    private boolean hidden;

    private RemoveHeadCircle removeHeadCircle;

    private boolean grew;

    private int[] centrePoint = null;

    private Trashy(Context context, WindowManager windowManager) {
        super(context);
        Trashy.windowManager = windowManager;

        removeHeadCircle = new RemoveHeadCircle(context);
        addView(removeHeadCircle);


        setVisibility(INVISIBLE);
        hidden = true;

        setInitialLocation();

        setUpSprings();
        initCentreCoords();

        Trashy.windowManager.addView(this, windowParams);
    }

    @SuppressLint("RtlHardcoded")
    private void setInitialLocation() {
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        dispWidth = metrics.widthPixels;
        dispHeight = metrics.heightPixels;

        createWindowParams();

        windowParams.gravity = Gravity.LEFT | Gravity.TOP;
        int offset = getAdaptWidth() / 2;
        windowParams.x = (dispWidth / 2) - offset;
        windowParams.y = dispHeight - (dispHeight / 6) - offset;
    }

    private void createWindowParams() {
        if (Utils.ANDROID_OREO) {
            windowParams = new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    TYPE_APPLICATION_OVERLAY,
                    FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE,
                    TRANSLUCENT);
        } else {
            windowParams = new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    TYPE_SYSTEM_ALERT,
                    FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE,
                    TRANSLUCENT);
        }
    }

    private void updateView() {
        if (windowParams != null) {
            windowManager.updateViewLayout(this, windowParams);
        }
    }

    public static void init(@NonNull Context context) {
        get(context);
    }

    /**
     * Returns an instance of this view. If the view is not initialized, then a new view is created
     * and returned.
     * The returned view might not have been laid out yet.
     *
     * @param context
     * @return
     */
    public synchronized static Trashy get(@NonNull Context context) {
        if (INSTANCE != null)
            return INSTANCE;
        else {
            Timber.d("Creating new instance of remove web head");
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            INSTANCE = new Trashy(context, windowManager);
            return INSTANCE;
        }
    }

    public static void destroy() {
        if (INSTANCE != null) {
            INSTANCE.destroySelf();
        }
    }

    public static void disappear() {
        if (INSTANCE != null) {
            INSTANCE.hide();
        }
    }

    public void destroyAnimator(final Runnable endAction) {
        if (INSTANCE == null || removeHeadCircle == null) endAction.run();

        INSTANCE.removeHeadCircle.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .alpha(0.5f)
                .setDuration(300)
                .withLayer()
                .withEndAction(endAction)
                .setInterpolator(new BounceInterpolator())
                .start();
    }

    private void destroySelf() {
        scaleSpring.setAtRest();
        scaleSpring.destroy();
        scaleSpring = null;

        removeView(removeHeadCircle);
        removeHeadCircle = null;

        windowParams = null;

        springSystem = null;

        windowManager.removeView(this);

        centrePoint = null;

        INSTANCE = null;
        Timber.d("Remove view detached and killed");
    }

    private int getAdaptWidth() {
        return Math.max(getWidth(), RemoveHeadCircle.getSizePx());
    }

    int[] getCenterCoordinates() {
        if (centrePoint == null) {
            initCentreCoords();
        }
        return centrePoint;
    }

    private void initCentreCoords() {
        int offset = getAdaptWidth() / 2;
        int rX = getWindowParams().x + offset;
        int rY = getWindowParams().y + offset;
        centrePoint = new int[]{rX, rY};
    }

    private void setUpSprings() {
        springSystem = SpringSystem.create();
        scaleSpring = springSystem.createSpring();

        SpringConfig scaleSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(100, 9);
        scaleSpring.setSpringConfig(scaleSpringConfig);
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                removeHeadCircle.setScaleX(value);
                removeHeadCircle.setScaleY(value);
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Timber.d(newConfig.toString());
        centrePoint = null;
        setInitialLocation();
        post(this::updateView);
    }

    private WindowManager.LayoutParams getWindowParams() {
        return windowParams;
    }

    private void hide() {
        if (!hidden) {
            scaleSpring.setEndValue(0.0f);
            hidden = true;
        }
    }

    void reveal() {
        setVisibility(VISIBLE);
        if (hidden) {
            scaleSpring.setEndValue(0.9f);
            hidden = false;
        }
    }

    void grow() {
        if (!grew) {
            scaleSpring.setCurrentValue(0.9f, true);
            scaleSpring.setEndValue(1f);
            grew = true;
        }
    }

    void shrink() {
        if (grew) {
            scaleSpring.setEndValue(0.9f);
            grew = false;
        }
    }

    /**
     * Created by Arun on 04/02/2016.
     */
    private static class RemoveHeadCircle extends View {

        private static int sSizePx;
        private static int sDiameterPx;
        private final Paint mBgPaint;

        public RemoveHeadCircle(Context context) {
            super(context);
            mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.remove_web_head_color));
            mBgPaint.setStyle(Paint.Style.FILL);

            float shadwR = context.getResources().getDimension(R.dimen.remove_head_shadow_radius);
            float shadwDx = context.getResources().getDimension(R.dimen.remove_head_shadow_dx);
            float shadwDy = context.getResources().getDimension(R.dimen.remove_head_shadow_dy);

            mBgPaint.setShadowLayer(shadwR, shadwDx, shadwDy, 0x75000000);

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            sSizePx = context.getResources().getDimensionPixelSize(R.dimen.remove_head_size);
        }

        static int getSizePx() {
            return sSizePx;
        }

        public static int getDiameterPx() {
            return sDiameterPx;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(sSizePx, sSizePx);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            float radius = (float) (getWidth() / 2.4);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mBgPaint);

            drawDeleteIcon(canvas);

            sDiameterPx = (int) (2 * radius);
        }

        private void drawDeleteIcon(Canvas canvas) {
            Bitmap deleteIcon = new IconicsDrawable(getContext())
                    .icon(CommunityMaterial.Icon.cmd_delete)
                    .color(Color.WHITE)
                    .sizeDp(18).toBitmap();
            int cHeight = canvas.getClipBounds().height();
            int cWidth = canvas.getClipBounds().width();
            float x = cWidth / 2f - deleteIcon.getWidth() / 2;
            float y = cHeight / 2f - deleteIcon.getHeight() / 2;
            canvas.drawBitmap(deleteIcon, x, y, null);
        }
    }
}
