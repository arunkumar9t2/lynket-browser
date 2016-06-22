package arun.com.chromer.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Arun on 16/06/2016.
 */
public class TabView extends FrameLayout {
    public static final int TAB_TYPE_OPTIONS = 0;
    public static final int TAB_TYPE_WEB_HEADS = 1;
    public static final int TAB_TYPE_CUSTOMIZE = 2;
    @ColorInt
    private static final int SELECTED_COLOR = Color.WHITE;
    @ColorInt
    private static final int UN_SELECTED_COLOR = ColorUtils.setAlphaComponent(SELECTED_COLOR, 178);
    @BindView(R.id.tab_view_icon)
    public ImageView mTabIcon;
    @BindView(R.id.tab_view_text)
    public TextView mTabText;
    Unbinder mUnBinder;
    private float initialIconX = 0;
    private float initialTextX = 0;
    private boolean mSelected;
    @TabType
    private int mTabType = TAB_TYPE_OPTIONS;

    public TabView(Context context, @TabType int tabType) {
        super(context);
        init(context, tabType);
    }

    public TabView(Context context, AttributeSet attrs, @TabType int tabType) {
        super(context, attrs);
        init(context, tabType);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr, @TabType int tabType) {
        super(context, attrs, defStyleAttr);
        init(context, tabType);
    }

    private void init(@NonNull Context context, @TabType int tabType) {
        mTabType = tabType;
        addView(LayoutInflater.from(context).inflate(R.layout.tab_view_layout, this, false));
        mUnBinder = ButterKnife.bind(this);
        switch (mTabType) {
            case TAB_TYPE_OPTIONS:
                mTabIcon.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_settings)
                        .color(SELECTED_COLOR)
                        .sizeDp(23));
                mTabText.setText(R.string.options);
                mTabText.setTextColor(SELECTED_COLOR);
                break;
            case TAB_TYPE_WEB_HEADS:
                mTabIcon.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_hdr_strong)
                        .color(UN_SELECTED_COLOR)
                        .sizeDp(23));
                mTabText.setText(R.string.web_heads);
                mTabText.setTextColor(UN_SELECTED_COLOR);
                break;
            case TAB_TYPE_CUSTOMIZE:
                mTabIcon.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_format_paint)
                        .color(UN_SELECTED_COLOR)
                        .sizeDp(23));
                mTabText.setText(R.string.customize);
                mTabText.setTextColor(UN_SELECTED_COLOR);
                break;
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int totalWidth = mTabIcon.getWidth() + Util.dpToPx(5) + mTabText.getWidth();
                int layoutWidth = getWidth();
                initialIconX = (layoutWidth / 2) - (totalWidth / 2);
                mTabIcon.setX(initialIconX);
                initialTextX = initialIconX + mTabIcon.getWidth() + Util.dpToPx(10);
                mTabText.setX(initialTextX);

                mTabText.setPivotX(0);
                mTabText.setPivotY(mTabText.getHeight() / 2);
                // Refresh animations
                setSelected(mSelected);
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUnBinder.unbind();
    }

    @Override
    public void setSelected(boolean selected) {
        mSelected = selected;
        if (mTabIcon == null || mTabText == null) return;
        if (selected) {
            mTabText.setTextColor(SELECTED_COLOR);
            IconicsDrawable drawable = (IconicsDrawable) mTabIcon.getDrawable();
            mTabIcon.setImageDrawable(drawable.color(SELECTED_COLOR));
            selectedAnimation();
        } else {
            mTabText.setTextColor(UN_SELECTED_COLOR);
            IconicsDrawable drawable = (IconicsDrawable) mTabIcon.getDrawable();
            mTabIcon.setImageDrawable(drawable.color(UN_SELECTED_COLOR));
            unSelectedAnimation();
        }
    }

    private float getIconCentreInLayout() {
        return ((getWidth() / 2) - (mTabIcon.getWidth() / 2));
    }

    private void clearAnimations() {
        mTabText.clearAnimation();
        mTabIcon.clearAnimation();
    }

    private void unSelectedAnimation() {
        clearAnimations();
        final AnimatorSet transformAnimator = new AnimatorSet();
        transformAnimator.playTogether(
                ObjectAnimator.ofFloat(mTabIcon, "translationX", initialIconX),
                ObjectAnimator.ofFloat(mTabIcon, "scaleX", 0.75f),
                ObjectAnimator.ofFloat(mTabIcon, "scaleY", 0.75f),
                ObjectAnimator.ofFloat(mTabText, "scaleX", 1f),
                ObjectAnimator.ofFloat(mTabText, "scaleY", 1f),
                ObjectAnimator.ofFloat(mTabText, "alpha", 1f)
        );
        transformAnimator.setDuration(275);
        transformAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        final AnimatorSet sequentialAnimator = new AnimatorSet();
        sequentialAnimator.playTogether(
                transformAnimator,
                getIconUnSelectionAnimator()
        );
        sequentialAnimator.start();
    }

    private void selectedAnimation() {
        clearAnimations();
        final AnimatorSet transformAnimator = new AnimatorSet();
        transformAnimator.playTogether(
                ObjectAnimator.ofFloat(mTabIcon, "translationX", getIconCentreInLayout()),
                ObjectAnimator.ofFloat(mTabIcon, "scaleX", 1f),
                ObjectAnimator.ofFloat(mTabIcon, "scaleY", 1f),
                ObjectAnimator.ofFloat(mTabText, "scaleX", 0f),
                ObjectAnimator.ofFloat(mTabText, "scaleY", 0f),
                ObjectAnimator.ofFloat(mTabText, "alpha", 0f)
        );
        transformAnimator.setDuration(275);
        transformAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        final AnimatorSet togetherAnimator = new AnimatorSet();
        togetherAnimator.playSequentially(
                transformAnimator,
                getIconSelectionAnimator()
        );
        togetherAnimator.start();
    }

    private Animator getIconSelectionAnimator() {
        Animator animator = null;
        switch (mTabType) {
            case TAB_TYPE_OPTIONS:
                animator = ObjectAnimator.ofFloat(mTabIcon, "rotation", 180);
                break;
            case TAB_TYPE_WEB_HEADS:
                animator = ObjectAnimator.ofFloat(mTabIcon, "rotation", -90);
                break;
            case TAB_TYPE_CUSTOMIZE:
                animator = ObjectAnimator.ofFloat(mTabIcon, "scaleY", 1.2f);
                ((ObjectAnimator) animator).setRepeatMode(ValueAnimator.REVERSE);
                ((ObjectAnimator) animator).setRepeatCount(3);
                animator.setInterpolator(new LinearInterpolator());
                break;
        }
        if (animator != null)
            animator.setDuration(250);
        return animator;
    }

    private Animator getIconUnSelectionAnimator() {
        Animator animator = null;
        switch (mTabType) {
            case TAB_TYPE_OPTIONS:
                animator = ObjectAnimator.ofFloat(mTabIcon, "rotation", -180);
                break;
            case TAB_TYPE_WEB_HEADS:
                animator = ObjectAnimator.ofFloat(mTabIcon, "rotation", 180);
                break;
            case TAB_TYPE_CUSTOMIZE:
                animator = ObjectAnimator.ofFloat(mTabIcon, "scaleY", 0.75f);
                break;
        }
        if (animator != null)
            animator.setDuration(250);
        return animator;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TAB_TYPE_OPTIONS, TAB_TYPE_WEB_HEADS, TAB_TYPE_CUSTOMIZE})
    public @interface TabType {
    }
}
