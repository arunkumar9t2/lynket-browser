package arun.com.chromer.webheads.ui;

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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * ViewGroup that holds the web head UI elements. Allows configuring various parameters in relation
 * to UI like favicon, indicator and is responsible for inflating all the content.
 */
public abstract class BaseWebHead extends FrameLayout {
    private static final int STACKING_GAP_PX = Util.dpToPx(6);
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
    protected ElevatedCircleView mRevealView;
    int sDispWidth;
    int sDispHeight;
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
    int mWebHeadColor;
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
                mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
                if (Preferences.webHeadsSpawnLocation(getContext()) == 1) {
                    mWindowParams.x = (int) (sDispWidth - getWidth() * 0.8);
                } else {
                    mWindowParams.x = (int) (0 - getWidth() * 0.2);
                }
                mWindowParams.y = sDispHeight / 3;
                updateView();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * Initializes web head from user preferences
     */
    private void initContent() {
        mWebHeadColor = Preferences.webHeadColor(getContext());
        mIndicator.setText(Util.getFirstLetter(mUrl));
        mRevealView.setVisibility(GONE);
    }

    /**
     * Used to get an instance of remove web head
     *
     * @return RemoveWebHead instance
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
    protected boolean isLastWebHead() {
        return WEB_HEAD_COUNT == 0;
    }

    /**
     * Removes url indicator with an animation
     */
    private void clearUrlIndicator() {
        mIndicator.animate().alpha(0.0f).withLayer().start();
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

    @ColorInt
    public int getWebHeadColor() {
        return mWebHeadColor;
    }

    public void setWebHeadColor(@ColorInt int webHeadColor) {
        mWebHeadColor = webHeadColor;
        mCircleBackground.setColor(webHeadColor);
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
        if (mFavicon != null) {
            TransitionDrawable drawable = (TransitionDrawable) mFavicon.getDrawable();
            if (drawable != null && drawable.getDrawable(1) instanceof RoundedBitmapDrawable) {
                RoundedBitmapDrawable roundedBitmapDrawable = (RoundedBitmapDrawable) drawable.getDrawable(1);
                return roundedBitmapDrawable.getBitmap();
            }
        } else return null;
        return null;
    }

    public boolean isFromNewTab() {
        return mIsFromNewTab;
    }

    public void setFromNewTab(boolean fromNewTab) {
        mIsFromNewTab = fromNewTab;
    }

    public void setFaviconDrawable(@NonNull Drawable drawable) {
        try {
            clearUrlIndicator();
            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{
                            new ColorDrawable(Color.TRANSPARENT),
                            drawable
                    });
            mFavicon.setVisibility(VISIBLE);
            mFavicon.setImageDrawable(transitionDrawable);
            transitionDrawable.setCrossFadeEnabled(true);
            transitionDrawable.startTransition(500);
        } catch (Exception ignore) {
            Timber.d(ignore.getMessage());
        }
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


}
