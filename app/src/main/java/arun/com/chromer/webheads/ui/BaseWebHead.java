package arun.com.chromer.webheads.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * ViewGroup that holds the web head UI elements. Allows configuring various parameters in relation
 * to UI like favicon, text indicator and is responsible for inflating all the content.
 */
public abstract class BaseWebHead extends FrameLayout {
    /**
     * Distance in pixels to be displaced when web heads are getting stacked
     */
    private static final int STACKING_GAP_PX = Util.dpToPx(6);
    /**
     * Helper instance to know screen boundaries that web head is allowed to travel
     */
    static ScreenBounds sScreenBounds;
    /**
     * Counter to keep count of active web heads
     */
    static int WEB_HEAD_COUNT = 0;
    /**
     * Static window manager instance to update, add and remove web heads
     */
    private static WindowManager sWindowManager;
    /**
     * Window parameters used to track and update web heads post creation;
     */
    final WindowManager.LayoutParams mWindowParams;
    /**
     * Color of web head when removed
     */
    final int mDeleteColor;
    /**
     * The url of the website that this web head represents, not allowed to change
     */
    private final String mUrl;
    /**
     * Butter knife un binder to release references;
     */
    private Unbinder mUnBinder;
    /**
     * Integer to keep track of web head creation order.
     */
    @BindView(R.id.favicon)
    protected ImageView mFavicon;
    @BindView(R.id.indicator)
    protected TextView mIndicator;
    @BindView(R.id.circleBackground)
    protected ElevatedCircleView mCircleBackground;
    @BindView(R.id.revealView)
    protected CircleView mRevealView;
    /**
     * The content view group which host all our elements
     */
    FrameLayout mContentGroup;
    int sDispWidth, sDispHeight;
    /**
     * Flag to know if the user moved manually or if the web heads is still resting
     */
    boolean mUserManuallyMoved;
    /**
     * If web head was issued with destroy before.
     */
    boolean mDestroyed;
    /**
     * Color of the web head
     */
    @ColorInt
    private int mWebHeadColor;
    /**
     * The un shortened url resolved from @link mUrl
     */
    private String mUnShortenedUrl;
    /**
     * Title of the website
     */
    private String mTitle;
    /**
     * Flag to know if this web head was created for opening in new tab
     */
    private boolean mIsFromNewTab;
    /**
     * X icon drawable used when closing
     */
    private static Drawable sXDrawable;
    /**
     * Master Wayne
     */
    boolean mMaster = false;

    private static int masterX;
    private static int masterY;

    @SuppressLint("RtlHardcoded")
    BaseWebHead(@NonNull Context context, @NonNull String url) {
        super(context);
        mUrl = url;
        sWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        inflateContent(context);
        initContent();

        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;

        initDisplayMetrics();
        setSpawnLocation();

        WEB_HEAD_COUNT++;

        sWindowManager.addView(this, mWindowParams);

        sXDrawable = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_close)
                .color(Color.WHITE)
                .sizeDp(18);
        mDeleteColor = ContextCompat.getColor(context, R.color.remove_web_head_color);

        // Needed to prevent overly dark shadow.
        if (WEB_HEAD_COUNT > 2) {
            setWebHeadElevation(Util.dpToPx(4));
        }
    }

    public void setMaster(boolean master) {
        this.mMaster = master;
        onMasterChanged(master);
    }

    protected abstract void onMasterChanged(boolean master);

    private void inflateContent(@NonNull Context context) {
        // size
        if (Preferences.webHeadsSize(context) == 2) {
            mContentGroup = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.web_head_layout_small, this, false);
        } else
            mContentGroup = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.web_head_layout, this, false);
        addView(mContentGroup);
        mUnBinder = ButterKnife.bind(this);
    }

    private void initDisplayMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(metrics);
        sDispWidth = metrics.widthPixels;
        sDispHeight = metrics.heightPixels;

        mWindowParams.y = sDispHeight / 3;
    }

    /**
     * Listens for layout events and once width is measured, sets the initial spawn location based on
     * user preference
     */
    private void setSpawnLocation() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (sScreenBounds == null)
                    sScreenBounds = new ScreenBounds(sDispWidth, sDispHeight, getWidth());

                if (masterX != 0 || masterY != 0) {
                    mWindowParams.x = masterX;
                    mWindowParams.y = masterY;
                } else {
                    if (Preferences.webHeadsSpawnLocation(getContext()) == 1) {
                        mWindowParams.x = sScreenBounds.right;
                    } else {
                        mWindowParams.x = sScreenBounds.left;
                    }
                }
                updateView();
                onNewMasterPositionSet();
            }
        });
    }

    /**
     * Event for sub class to get notified once spawn location is set.
     */
    protected abstract void onNewMasterPositionSet();

    /**
     * Initializes web head from user preferences
     */
    private void initContent() {
        mWebHeadColor = Preferences.webHeadColor(getContext());
        mIndicator.setText(Util.getFirstLetter(mUrl));
        mIndicator.setTextColor(ColorUtil.getForegroundWhiteOrBlack(mWebHeadColor));
        initRevealView(mWebHeadColor);
    }

    /**
     * Used to get an instance of remove web head
     *
     * @return an instance of {@link RemoveWebHead}
     */
    RemoveWebHead getRemoveWebHead() {
        return RemoveWebHead.get(getContext());
    }

    /**
     * Wrapper around window manager to update this view. Called to move the web head usually.
     */
    void updateView() {
        try {
            if (mMaster) {
                masterX = mWindowParams.x;
                masterY = mWindowParams.y;
            }
            sWindowManager.updateViewLayout(this, mWindowParams);
        } catch (IllegalArgumentException e) {
            Timber.e("Update called after view was removed");
        }
    }

    /**
     * @return true if current web head is the last active one
     */
    boolean isLastWebHead() {
        return WEB_HEAD_COUNT == 0;
    }

    private void setWebHeadElevation(int elevationPX) {
        if (Util.isLollipopAbove()) {
            if (mCircleBackground != null && mRevealView != null) {
                mCircleBackground.setElevation(elevationPX);
                mRevealView.setElevation(elevationPX + 1);
            }
        }
    }

    @Nullable
    public ValueAnimator getStackDistanceAnimator() {
        ValueAnimator animator = null;
        if (!mUserManuallyMoved) {
            animator = ValueAnimator.ofInt(mWindowParams.y, mWindowParams.y + STACKING_GAP_PX);
            animator.setInterpolator(new FastOutLinearInInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mWindowParams.y = (int) animation.getAnimatedValue();
                    updateView();
                }
            });
        }
        return animator;
    }

    @NonNull
    public Animator getRevealAnimator(@ColorInt final int newWebHeadColor) {
        mRevealView.clearAnimation();
        initRevealView(newWebHeadColor);

        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(mRevealView, "scaleX", 1f),
                ObjectAnimator.ofFloat(mRevealView, "scaleY", 1f),
                ObjectAnimator.ofFloat(mRevealView, "alpha", 1f)
        );
        mRevealView.setLayerType(LAYER_TYPE_HARDWARE, null);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mWebHeadColor = newWebHeadColor;
                if (mIndicator != null && mCircleBackground != null && mRevealView != null) {
                    mCircleBackground.setColor(newWebHeadColor);
                    mIndicator.setTextColor(ColorUtil.getForegroundWhiteOrBlack(newWebHeadColor));
                    mRevealView.setLayerType(LAYER_TYPE_NONE, null);
                    mRevealView.setScaleX(0f);
                    mRevealView.setScaleY(0f);
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
     * @param newWebHeadColor The color to apply to circle background after animation is done
     * @return animator
     */
    @NonNull
    Animator getRevealInAnimator(@ColorInt final int newWebHeadColor) {
        // TODO Fix NPE
        mRevealView.clearAnimation();
        mRevealView.setColor(mCircleBackground.getColor());
        mRevealView.setScaleX(1f);
        mRevealView.setScaleY(1f);
        mRevealView.setAlpha(1f);
        mCircleBackground.setColor(newWebHeadColor);
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(mRevealView, "scaleX", 0f),
                ObjectAnimator.ofFloat(mRevealView, "scaleY", 0f)
        );
        mRevealView.setLayerType(LAYER_TYPE_HARDWARE, null);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mWebHeadColor = newWebHeadColor;
                mIndicator.setTextColor(ColorUtil.getForegroundWhiteOrBlack(newWebHeadColor));
                mRevealView.setLayerType(LAYER_TYPE_NONE, null);
                mRevealView.setScaleX(0f);
                mRevealView.setScaleY(0f);
            }

        });
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.setDuration(400);
        return animator;
    }

    /**
     * Resets the reveal color so that it is ready to being reveal animation
     *
     * @param revealColor The color to appear during animation
     */
    private void initRevealView(@ColorInt int revealColor) {
        mRevealView.setColor(revealColor);
        mRevealView.setScaleX(0f);
        mRevealView.setScaleY(0f);
        mRevealView.setAlpha(0.8f);
    }

    /**
     * Applies a cross fade animation to transform the current favicon to an X icon. Ensures favicon
     * is visible by hiding indicators.
     */
    void crossFadeFaviconToX() {
        mFavicon.setVisibility(VISIBLE);
        mFavicon.clearAnimation();
        mFavicon.setScaleType(ImageView.ScaleType.CENTER);
        final TransitionDrawable icon = new TransitionDrawable(
                new Drawable[]{
                        new ColorDrawable(Color.TRANSPARENT),
                        sXDrawable
                });
        mFavicon.setImageDrawable(icon);
        icon.setCrossFadeEnabled(true);
        icon.startTransition(50);
        mFavicon
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
            return mWebHeadColor;
        } else {
            if (getFaviconBitmap() != null) {
                return mWebHeadColor;
            } else return Constants.NO_COLOR;
        }
    }

    public void setWebHeadColor(@ColorInt int webHeadColor) {
        getRevealAnimator(webHeadColor).start();
    }

    @NonNull
    public ImageView getFaviconView() {
        return mFavicon;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getUnShortenedUrl() {
        return mUnShortenedUrl == null ? getUrl() : mUnShortenedUrl;
    }

    public void setUnShortenedUrl(String unShortenedUrl) {
        mUnShortenedUrl = unShortenedUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Nullable
    public Bitmap getFaviconBitmap() {
        try {
            RoundedBitmapDrawable roundedBitmapDrawable = (RoundedBitmapDrawable) getFaviconDrawable();
            return roundedBitmapDrawable != null ? roundedBitmapDrawable.getBitmap() : null;
        } catch (Exception e) {
            Timber.e("Error while getting favicon bitmap: %s", e.getMessage());
        }
        return null;
    }

    @Nullable
    private Drawable getFaviconDrawable() {
        try {
            TransitionDrawable drawable = (TransitionDrawable) mFavicon.getDrawable();
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
        if (mIndicator != null && mFavicon != null) {
            mIndicator.animate().alpha(0).withLayer().start();
            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{
                            new ColorDrawable(Color.TRANSPARENT),
                            faviconDrawable
                    });
            mFavicon.setVisibility(VISIBLE);
            mFavicon.setImageDrawable(transitionDrawable);
            transitionDrawable.setCrossFadeEnabled(true);
            transitionDrawable.startTransition(500);
        }
    }

    public boolean isFromNewTab() {
        return mIsFromNewTab;
    }

    public void setFromNewTab(boolean fromNewTab) {
        mIsFromNewTab = fromNewTab;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // mUnBinder.unbind();
    }

    @SuppressWarnings("UnusedParameters")
    void destroySelf(boolean receiveCallback) {
        mDestroyed = true;
        RemoveWebHead.disappear();
        removeView(mContentGroup);
        if (sWindowManager != null)
            try {
                sWindowManager.removeView(this);
            } catch (Exception ignored) {
            }
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

        public ScreenBounds(int dispWidth, int dispHeight, int webHeadWidth) {
            if (webHeadWidth == 0 || dispWidth == 0 || dispHeight == 0) {
                throw new IllegalArgumentException("Width of web head or screen size cannot be 0");
            }
            right = (int) (dispWidth - (webHeadWidth * DISPLACE_PERC));
            left = (int) (webHeadWidth * (1 - DISPLACE_PERC)) * -1;
            top = Util.dpToPx(25);
            bottom = (int) (dispHeight * 0.85);
        }
    }
}
