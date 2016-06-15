package arun.com.chromer.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import arun.com.chromer.R;
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

    @BindView(R.id.tab_view_icon)
    public ImageView mTabIcon;
    @BindView(R.id.tab_view_text)
    public TextView mTabText;

    Unbinder mUnBinder;

    @ColorInt
    private static final int SELECTED_COLOR = Color.WHITE;
    @ColorInt
    private static final int UN_SELECTED_COLOR = ColorUtils.setAlphaComponent(SELECTED_COLOR, 191);

    @TabType
    private int mTabType = TAB_TYPE_OPTIONS;

    private float initialIconX = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TAB_TYPE_OPTIONS, TAB_TYPE_WEB_HEADS, TAB_TYPE_CUSTOMIZE,})

    public @interface TabType {
    }

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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addView(LayoutInflater.from(getContext()).inflate(R.layout.tab_view_layout, this, false));
        mUnBinder = ButterKnife.bind(this);
        switch (mTabType) {
            case TAB_TYPE_OPTIONS:
                mTabIcon.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_settings)
                        .color(SELECTED_COLOR)
                        .sizeDp(24));
                mTabText.setText(R.string.options);
                mTabText.setTextColor(SELECTED_COLOR);
                break;
            case TAB_TYPE_WEB_HEADS:
                mTabIcon.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_phone_android)
                        .color(UN_SELECTED_COLOR)
                        .sizeDp(24));
                mTabText.setText(R.string.web_heads);
                mTabText.setTextColor(UN_SELECTED_COLOR);
                break;
            case TAB_TYPE_CUSTOMIZE:
                mTabIcon.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(GoogleMaterial.Icon.gmd_format_paint)
                        .color(UN_SELECTED_COLOR)
                        .sizeDp(24));
                mTabText.setText(R.string.customize);
                mTabText.setTextColor(UN_SELECTED_COLOR);
                break;
        }
        initialIconX = mTabIcon.getX();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUnBinder.unbind();
    }

    @Override
    public void setSelected(boolean selected) {
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

    private void unSelectedAnimation() {
        mTabText.clearAnimation();
        mTabIcon.clearAnimation();
        final AnimatorSet animatorSet = new AnimatorSet();
        mTabText.setPivotX(0);
        mTabText.setPivotY(mTabText.getHeight() / 2);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(mTabIcon, "translationX", initialIconX),
                ObjectAnimator.ofFloat(mTabIcon, "scaleX", 0.75f),
                ObjectAnimator.ofFloat(mTabIcon, "scaleY", 0.75f),
                ObjectAnimator.ofFloat(mTabText, "scaleX", 1f),
                ObjectAnimator.ofFloat(mTabText, "scaleY", 1f),
                ObjectAnimator.ofFloat(mTabText, "alpha", 1f)
        );
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void selectedAnimation() {
        mTabText.clearAnimation();
        mTabIcon.clearAnimation();
        mTabText.setPivotX(0);
        mTabText.setPivotY(mTabText.getHeight() / 2);

        float translationX = ((getWidth() / 2) - mTabIcon.getX() - (mTabIcon.getWidth() / 2));
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(mTabIcon, "translationX", translationX),
                ObjectAnimator.ofFloat(mTabIcon, "scaleX", 1f),
                ObjectAnimator.ofFloat(mTabIcon, "scaleY", 1f),
                ObjectAnimator.ofFloat(mTabText, "scaleX", 0f),
                ObjectAnimator.ofFloat(mTabText, "scaleY", 0f),
                ObjectAnimator.ofFloat(mTabText, "alpha", 0f)
        );
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
}
