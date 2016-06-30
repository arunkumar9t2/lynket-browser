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
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
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
    private static int WEB_HEAD_COUNT = 0;
    /**
     * Static window manager instance to update, add and remove web heads
     */
    private static WindowManager sWindowManager;
    /**
     * Window parameters used to track and update web heads post creation;
     */
    final WindowManager.LayoutParams mWindowParams;
    /**
     * The content view group which host all our elements
     */
    final FrameLayout mContentGroup;
    /**
     * The url of the website that this web head represents, not allowed to change
     */
    private final String mUrl;
    /**
     * Butter knife un binder to release references;
     */
    private final Unbinder mUnBinder;
    @BindView(R.id.favicon)
    protected ImageView mFavicon;
    @BindView(R.id.indicator)
    protected TextView mIndicator;
    @BindView(R.id.circleBackground)
    protected ElevatedCircleView mCircleBackground;
    @BindView(R.id.revealView)
    protected CircleView mRevealView;
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

    @SuppressLint("RtlHardcoded")
    BaseWebHead(@NonNull Context context, @NonNull String url) {
        super(context);
        mUrl = url;
        sWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        mContentGroup = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.web_head_layout, this, false);
        addView(mContentGroup);
        mUnBinder = ButterKnife.bind(this);
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

        sWindowManager.addView(this, mWindowParams);

        WEB_HEAD_COUNT++;
    }

    private void initDisplayMetrics() {
        final DisplayMetrics metrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(metrics);
        sDispWidth = metrics.widthPixels;
        sDispHeight = metrics.heightPixels;
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
                if (sScreenBounds == null)
                    sScreenBounds = new ScreenBounds(sDispWidth, sDispHeight, getWidth());

                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (Preferences.webHeadsSpawnLocation(getContext()) == 1) {
                    mWindowParams.x = sScreenBounds.right;
                } else {
                    mWindowParams.x = sScreenBounds.left;
                }
                mWindowParams.y = sDispHeight / 3;
                updateView();
            }
        });
    }

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
        sWindowManager.updateViewLayout(this, mWindowParams);
    }

    /**
     * @return true if current web head is the last active one
     */
    boolean isLastWebHead() {
        return WEB_HEAD_COUNT == 0;
    }

    /**
     * Returns an animation instance that smoothly clear the url indicator
     */
    private ViewPropertyAnimator getIndicatorClearAnimation() {
        return mIndicator.animate()
                .alpha(0.0f)
                .withLayer()
                .setDuration(150);
    }

    public void setFaviconDrawable(@NonNull final Drawable faviconDrawable) {
        try {
            getIndicatorClearAnimation().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
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
            });
        } catch (Exception ignore) {
            Timber.d(ignore.getMessage());
        }
    }

    @Nullable
    public ValueAnimator getStackDistanceAnimator() {
        ValueAnimator animator;
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
        return null;
    }

    @NonNull
    public Animator getColorChangeAnimator(@ColorInt final int newWebHeadColor) {
        initRevealView(newWebHeadColor);

        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(mRevealView, "scaleX", 1f),
                ObjectAnimator.ofFloat(mRevealView, "scaleY", 1f),
                ObjectAnimator.ofFloat(mRevealView, "alpha", 1f)
        );
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mWebHeadColor = newWebHeadColor;
                mCircleBackground.setColor(newWebHeadColor);
                mIndicator.setTextColor(ColorUtil.getForegroundWhiteOrBlack(newWebHeadColor));
                mRevealView.setLayerType(LAYER_TYPE_NONE, null);
                mRevealView.setScaleX(0f);
                mRevealView.setScaleY(0f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mRevealView.setLayerType(LAYER_TYPE_HARDWARE, null);
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(250);
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

    @ColorInt
    public int getWebHeadColor() {
        return mWebHeadColor;
    }

    public void setWebHeadColor(@ColorInt int webHeadColor) {
        getColorChangeAnimator(webHeadColor).start();
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
            TransitionDrawable drawable = (TransitionDrawable) mFavicon.getDrawable();
            RoundedBitmapDrawable roundedBitmapDrawable = (RoundedBitmapDrawable) drawable.getDrawable(1);
            return roundedBitmapDrawable.getBitmap();
        } catch (Exception e) {
            Timber.e("Error while getting favicon bitmap: %s", e.getMessage());
        }
        return null;
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
        mUnBinder.unbind();
    }

    @CallSuper
    void destroySelf(boolean receiveCallback) {
        mDestroyed = true;
        WEB_HEAD_COUNT--;
        RemoveWebHead.disappear();
        removeView(mContentGroup);
        if (sWindowManager != null)
            sWindowManager.removeView(this);
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
