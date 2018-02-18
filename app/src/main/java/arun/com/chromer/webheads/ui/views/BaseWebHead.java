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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.nekocode.badge.BadgeDrawable;
import timber.log.Timber;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;
import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.TOP;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
import static android.widget.ImageView.ScaleType.CENTER;
import static arun.com.chromer.shared.Constants.NO_COLOR;
import static arun.com.chromer.util.ColorUtil.getForegroundWhiteOrBlack;
import static arun.com.chromer.util.Utils.dpToPx;
import static cn.nekocode.badge.BadgeDrawable.TYPE_NUMBER;
import static com.mikepenz.community_material_typeface_library.CommunityMaterial.Icon.cmd_close;

/**
 * ViewGroup that holds the web head UI elements. Allows configuring various parameters in relation
 * to UI like favicon, text indicator and is responsible for inflating all the content.
 */
public abstract class BaseWebHead extends FrameLayout {
    // Helper instance to know screen boundaries that web head is allowed to travel
    static ScreenBounds screenBounds;
    // Counter to keep count of active web heads
    static int WEB_HEAD_COUNT = 0;
    // Class variables to keep track of where the master was last touched down
    static int masterDownX;
    static int masterDownY;
    // Static window manager instance to update, add and remove web heads
    private static WindowManager windowManager;
    // X icon drawable used when closing
    private static Drawable xDrawable;
    // Badge indicator
    private static BadgeDrawable badgeDrawable;
    // Class variables to keep track of master movements
    private static int masterX;
    private static int masterY;
    // Window parameters used to track and update web heads post creation;
    final WindowManager.LayoutParams windowParams;
    // Color of web head when removed
    int deleteColor = NO_COLOR;
    // The preferredUrl of the website that this web head represents, not allowed to change
    private final String url;
    // Website data that this web head represents
    protected Website website;

    @BindView(R.id.favicon)
    protected ImageView favicon;
    @BindView(R.id.indicator)
    protected TextView indicator;
    @BindView(R.id.circleBackground)
    protected ElevatedCircleView circleBg;
    @BindView(R.id.revealView)
    protected CircleView revealView;
    @BindView(R.id.badge)
    protected TextView badgeView;

    // Display dimensions
    int dispWidth, dispHeight;
    // The content view group which host all our elements
    FrameLayout contentRoot;
    // Flag to know if the user moved manually or if the web heads is still resting
    boolean userManuallyMoved;
    // If web head was issued with destroy before.
    boolean destroyed;
    // Master Wayne
    boolean master;
    // If this web head is being queued to be displayed on screen.
    boolean inQueue;
    // Flag to know if this web head was created for opening in new tab
    private boolean fromNewTab;
    protected boolean spawnCoordSet;
    // Color of the web head
    @ColorInt
    int webHeadColor;

    @SuppressLint("RtlHardcoded")
    BaseWebHead(@NonNull final Context context, @NonNull final String url) {
        super(context);
        WEB_HEAD_COUNT++;
        this.url = url;
        website = new Website();
        website.url = url;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        inflateContent(context);
        windowParams = createWindowParams();
        windowParams.gravity = TOP | LEFT;
        initDisplayMetrics();
        windowManager.addView(this, windowParams);
        if (xDrawable == null) {
            xDrawable = new IconicsDrawable(context)
                    .icon(cmd_close)
                    .color(WHITE)
                    .sizeDp(18);
        }
        if (deleteColor == NO_COLOR) {
            deleteColor = ContextCompat.getColor(context, R.color.remove_web_head_color);
        }
        // Needed to prevent overly dark shadow.
        if (WEB_HEAD_COUNT > 2) {
            setWebHeadElevation(dpToPx(5));
        }
    }

    public static void clearMasterPosition() {
        masterY = 0;
        masterX = 0;
    }

    protected abstract void onMasterChanged(boolean master);

    /**
     * Event for sub class to get notified once spawn location is set.
     *
     * @param x X
     * @param y Y
     */
    protected abstract void onSpawnLocationSet(int x, int y);

    private void inflateContent(@NonNull Context context) {
        // size
        if (Preferences.get(context).webHeadsSize() == 2) {
            contentRoot = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.widget_web_head_layout_small, this, false);
        } else
            contentRoot = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.widget_web_head_layout, this, false);
        addView(contentRoot);
        ButterKnife.bind(this);

        webHeadColor = Preferences.get(context).webHeadColor();
        indicator.setText(Utils.getFirstLetter(url));
        indicator.setTextColor(getForegroundWhiteOrBlack(webHeadColor));
        initRevealView(webHeadColor);

        if (badgeDrawable == null) {
            badgeDrawable = new BadgeDrawable.Builder()
                    .type(TYPE_NUMBER)
                    .badgeColor(ContextCompat.getColor(getContext(), R.color.accent))
                    .textColor(WHITE)
                    .number(WEB_HEAD_COUNT)
                    .build();
        } else {
            badgeDrawable.setNumber(WEB_HEAD_COUNT);
        }
        badgeView.setVisibility(VISIBLE);
        badgeView.setText(new SpannableString(badgeDrawable.toSpannable()));
        updateBadgeColors(webHeadColor);

        if (!Utils.isLollipopAbove()) {
            final int pad = dpToPx(5);
            badgeView.setPadding(pad, pad, pad, pad);
        }
    }

    private void initDisplayMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        dispWidth = metrics.widthPixels;
        dispHeight = metrics.heightPixels;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        masterX = 0;
        masterY = 0;
        initDisplayMetrics();
    }

    protected void setInitialSpawnLocation() {
        Timber.d("Initial spawn location set.");
        if (screenBounds == null) {
            screenBounds = new ScreenBounds(dispWidth, dispHeight, getWidth());
        }
        if (!spawnCoordSet) {
            int x, y = dispHeight / 3;
            if (masterX != 0 || masterY != 0) {
                x = masterX;
                y = masterY;
            } else {
                if (Preferences.get(getContext()).webHeadsSpawnLocation() == 1) {
                    x = screenBounds.right;
                } else {
                    x = screenBounds.left;
                }
            }
            spawnCoordSet = true;
            onSpawnLocationSet(x, y);
        }
    }

    /**
     * Used to get an instance of remove web head
     *
     * @return an instance of {@link Trashy}
     */
    Trashy getTrashy() {
        return Trashy.get(getContext());
    }

    /**
     * Wrapper around window manager to update this view. Called to move the web head usually.
     */
    void updateView() {
        try {
            if (master) {
                masterX = windowParams.x;
                masterY = windowParams.y;
            }
            windowManager.updateViewLayout(this, windowParams);
        } catch (IllegalArgumentException e) {
            Timber.e("Update called after view was removed");
        }
    }

    public WindowManager.LayoutParams getWindowParams() {
        return windowParams;
    }

    boolean isLastWebHead() {
        return WEB_HEAD_COUNT == 0;
    }

    private void setWebHeadElevation(final int elevationPx) {
        if (Utils.isLollipopAbove()) {
            if (circleBg != null && revealView != null) {
                circleBg.setElevation(elevationPx);
                revealView.setElevation(elevationPx + 1);
            }
        }
    }

    @NonNull
    public Animator getRevealAnimator(@ColorInt final int newWebHeadColor) {
        revealView.clearAnimation();
        initRevealView(newWebHeadColor);

        final AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(revealView, "scaleX", 1f),
                ObjectAnimator.ofFloat(revealView, "scaleY", 1f),
                ObjectAnimator.ofFloat(revealView, "alpha", 1f)
        );
        revealView.setLayerType(LAYER_TYPE_HARDWARE, null);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                webHeadColor = newWebHeadColor;
                updateBadgeColors(webHeadColor);
                if (indicator != null && circleBg != null && revealView != null) {
                    circleBg.setColor(newWebHeadColor);
                    indicator.setTextColor(getForegroundWhiteOrBlack(newWebHeadColor));
                    revealView.setLayerType(LAYER_TYPE_NONE, null);
                    revealView.setScaleX(0f);
                    revealView.setScaleY(0f);
                }
            }
        });
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.setDuration(250);
        return animator;
    }

    /**
     * Opposite of {@link #getRevealAnimator(int)}. Reveal goes from max scale to 0 appearing to be
     * revealing in.
     *
     * @param newWebHeadColor New themeColor of reveal
     * @param start           Runnable to run on start
     * @param end             Runnable to run on end
     */
    void revealInAnimation(@ColorInt final int newWebHeadColor, @NonNull final Runnable start, @NonNull final Runnable end) {
        if (revealView == null || circleBg == null) {
            start.run();
            end.run();
        }
        revealView.clearAnimation();
        revealView.setColor(circleBg.getColor());
        revealView.setScaleX(1f);
        revealView.setScaleY(1f);
        revealView.setAlpha(1f);
        circleBg.setColor(newWebHeadColor);
        final AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(revealView, "scaleX", 0f),
                ObjectAnimator.ofFloat(revealView, "scaleY", 0f)
        );
        revealView.setLayerType(LAYER_TYPE_HARDWARE, null);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                start.run();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                webHeadColor = newWebHeadColor;
                indicator.setTextColor(getForegroundWhiteOrBlack(newWebHeadColor));
                revealView.setLayerType(LAYER_TYPE_NONE, null);
                revealView.setScaleX(0f);
                revealView.setScaleY(0f);
                end.run();
            }

        });
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.setDuration(400);
        animator.setStartDelay(100);
        animator.start();
    }

    /**
     * Resets the reveal color so that it is ready to being reveal animation
     *
     * @param revealColor The color to appear during animation
     */
    private void initRevealView(@ColorInt int revealColor) {
        revealView.setColor(revealColor);
        revealView.setScaleX(0f);
        revealView.setScaleY(0f);
        revealView.setAlpha(0.8f);
    }

    /**
     * Applies a cross fade animation to transform the current favicon to an X icon. Ensures favicon
     * is visible by hiding indicators.
     */
    void crossFadeFaviconToX() {
        favicon.setVisibility(VISIBLE);
        favicon.clearAnimation();
        favicon.setScaleType(CENTER);
        final TransitionDrawable icon = new TransitionDrawable(
                new Drawable[]{
                        new ColorDrawable(TRANSPARENT),
                        xDrawable
                });
        favicon.setImageDrawable(icon);
        icon.setCrossFadeEnabled(true);
        icon.startTransition(50);
        favicon
                .animate()
                .withLayer()
                .rotation(180)
                .setDuration(250)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .start();
    }

    @SuppressWarnings("SameParameterValue")
    @ColorInt
    public int getWebHeadColor(boolean ignoreFavicons) {
        if (ignoreFavicons) {
            return webHeadColor;
        } else {
            if (getFaviconBitmap() != null) {
                return webHeadColor;
            } else return NO_COLOR;
        }
    }

    public void setWebHeadColor(@ColorInt int webHeadColor) {
        getRevealAnimator(webHeadColor).start();
    }

    void updateBadgeColors(@ColorInt int webHeadColor) {
        final int badgeColor = ColorUtil.getClosestAccentColor(webHeadColor);
        badgeDrawable.setBadgeColor(badgeColor);
        badgeDrawable.setTextColor(getForegroundWhiteOrBlack(badgeColor));
        badgeView.invalidate();
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public String getUnShortenedUrl() {
        return website.preferredUrl();
    }

    @Nullable
    public Bitmap getFaviconBitmap() {
        try {
            final RoundedBitmapDrawable roundedBitmapDrawable = (RoundedBitmapDrawable) getFaviconDrawable();
            return roundedBitmapDrawable != null ? roundedBitmapDrawable.getBitmap() : null;
        } catch (Exception e) {
            Timber.e("Error while getting favicon bitmap: %s", e.getMessage());
        }
        return null;
    }

    @Nullable
    private Drawable getFaviconDrawable() {
        try {
            TransitionDrawable drawable = (TransitionDrawable) favicon.getDrawable();
            if (drawable != null) {
                return drawable.getDrawable(1);
            } else
                return null;
        } catch (ClassCastException e) {
            Timber.e("Error while getting favicon drawable: %s", e.getMessage());
        }
        return null;
    }

    public void setFaviconDrawable(@NonNull final Drawable faviconDrawable) {
        if (indicator != null && favicon != null) {
            indicator.animate().alpha(0).withLayer().start();
            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{
                            new ColorDrawable(TRANSPARENT),
                            faviconDrawable
                    });
            favicon.setVisibility(VISIBLE);
            favicon.setImageDrawable(transitionDrawable);
            transitionDrawable.setCrossFadeEnabled(true);
            transitionDrawable.startTransition(500);
        }
    }

    @SuppressWarnings("UnusedParameters")
    void destroySelf(boolean receiveCallback) {
        destroyed = true;
        Trashy.disappear();
        removeView(contentRoot);
        if (windowManager != null)
            try {
                windowManager.removeView(this);
            } catch (Exception ignored) {
            }
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
        if (!master) {
            badgeView.setVisibility(INVISIBLE);
        } else {
            badgeView.setVisibility(VISIBLE);
            badgeDrawable.setNumber(WEB_HEAD_COUNT);
            setInQueue(false);
        }
        onMasterChanged(master);
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
        if (inQueue) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    @NonNull
    private WindowManager.LayoutParams createWindowParams() {
        if (Utils.ANDROID_OREO) {
            return new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    TYPE_APPLICATION_OVERLAY,
                    FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS | FLAG_HARDWARE_ACCELERATED,
                    TRANSLUCENT);
        } else
            //noinspection deprecation
            return new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    TYPE_SYSTEM_ALERT,
                    FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS | FLAG_HARDWARE_ACCELERATED,
                    TRANSLUCENT);
    }

    /**
     * Returns website POJO containing useful data.
     *
     * @return website data.
     */
    @NonNull
    public Website getWebsite() {
        return website;
    }

    public void setWebsite(@NonNull Website website) {
        // Timber.d(website.toString());
        this.website = website;
    }

    /**
     * Helper class to hold screen boundaries
     */
    class ScreenBounds {
        /**
         * Amount of web head that will be displaced off of the screen horizontally
         */
        private static final double DISPLACE_PERC = 0.7;

        public int left;
        public int right;
        public int top;
        public int bottom;

        ScreenBounds(int dispWidth, int dispHeight, int webHeadWidth) {
            if (webHeadWidth == 0 || dispWidth == 0 || dispHeight == 0) {
                throw new IllegalArgumentException("Width of web head or screen size cannot be 0");
            }
            right = (int) (dispWidth - (webHeadWidth * DISPLACE_PERC));
            left = (int) (webHeadWidth * (1 - DISPLACE_PERC)) * -1;
            top = dpToPx(25);
            bottom = (int) (dispHeight * 0.85);
        }
    }
}
