/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.shared.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import arun.com.chromer.R;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arun on 16/06/2016.
 */
@Deprecated
public class TabView extends FrameLayout {
  public static final int TAB_TYPE_OPTIONS = 0;
  public static final int TAB_TYPE_WEB_HEADS = 1;
  public static final int TAB_TYPE_CUSTOMIZE = 2;

  @ColorInt
  private static final int SELECTED_COLOR = Color.WHITE;
  @ColorInt
  private static final int UN_SELECTED_COLOR = ColorUtils.setAlphaComponent(SELECTED_COLOR, 178);

  @BindView(R.id.tab_view_icon)
  public ImageView tabIcon;
  @BindView(R.id.tab_view_text)
  public TextView text;

  private float initialIconX = 0;
  private float initialTextX = 0;
  private boolean selected;

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
    addView(LayoutInflater.from(context).inflate(R.layout.widget_tab_view_layout, this, false));
    ButterKnife.bind(this);
    switch (mTabType) {
      case TAB_TYPE_OPTIONS:
        tabIcon.setImageDrawable(new IconicsDrawable(getContext())
          .icon(CommunityMaterial.Icon.cmd_settings)
          .color(SELECTED_COLOR)
          .sizeDp(23));
        text.setText(R.string.options);
        text.setTextColor(SELECTED_COLOR);
        break;
      case TAB_TYPE_WEB_HEADS:
        tabIcon.setImageDrawable(new IconicsDrawable(getContext())
          .icon(CommunityMaterial.Icon.cmd_chart_bubble)
          .color(UN_SELECTED_COLOR)
          .sizeDp(23));
        text.setText(R.string.web_heads);
        text.setTextColor(UN_SELECTED_COLOR);
        break;
      case TAB_TYPE_CUSTOMIZE:
        tabIcon.setImageDrawable(new IconicsDrawable(getContext())
          .icon(CommunityMaterial.Icon.cmd_format_paint)
          .color(UN_SELECTED_COLOR)
          .sizeDp(23));
        text.setText(R.string.customize);
        text.setTextColor(UN_SELECTED_COLOR);
        break;
    }
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        int totalWidth = tabIcon.getWidth() + Utils.dpToPx(5) + text.getWidth();
        int layoutWidth = getWidth();
        initialIconX = (layoutWidth / 2) - (totalWidth / 2);
        tabIcon.setX(initialIconX);
        initialTextX = initialIconX + tabIcon.getWidth() + Utils.dpToPx(10);
        text.setX(initialTextX);

        text.setPivotX(0);
        text.setPivotY(text.getHeight() / 2);
        // Refresh animations
        setSelected(selected);
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
      }
    });
  }

  @Override
  public void setSelected(boolean selected) {
    this.selected = selected;
    if (tabIcon == null || text == null) return;
    if (selected) {
      text.setTextColor(SELECTED_COLOR);
      IconicsDrawable drawable = (IconicsDrawable) tabIcon.getDrawable();
      tabIcon.setImageDrawable(drawable.color(SELECTED_COLOR));
      selectedAnimation();
    } else {
      text.setTextColor(UN_SELECTED_COLOR);
      IconicsDrawable drawable = (IconicsDrawable) tabIcon.getDrawable();
      tabIcon.setImageDrawable(drawable.color(UN_SELECTED_COLOR));
      unSelectedAnimation();
    }
  }

  private float getIconCentreInLayout() {
    return ((getWidth() / 2) - (tabIcon.getWidth() / 2));
  }

  private void clearAnimations() {
    text.clearAnimation();
    tabIcon.clearAnimation();
  }

  private void unSelectedAnimation() {
    clearAnimations();
    final AnimatorSet transformAnimator = new AnimatorSet();
    transformAnimator.playTogether(
      ObjectAnimator.ofFloat(tabIcon, "translationX", initialIconX),
      ObjectAnimator.ofFloat(tabIcon, "scaleX", 0.75f),
      ObjectAnimator.ofFloat(tabIcon, "scaleY", 0.75f),
      ObjectAnimator.ofFloat(text, "scaleX", 1f),
      ObjectAnimator.ofFloat(text, "scaleY", 1f),
      ObjectAnimator.ofFloat(text, "alpha", 1f)
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
      ObjectAnimator.ofFloat(tabIcon, "translationX", getIconCentreInLayout()),
      ObjectAnimator.ofFloat(tabIcon, "scaleX", 1f),
      ObjectAnimator.ofFloat(tabIcon, "scaleY", 1f),
      ObjectAnimator.ofFloat(text, "scaleX", 0f),
      ObjectAnimator.ofFloat(text, "scaleY", 0f),
      ObjectAnimator.ofFloat(text, "alpha", 0f)
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
        animator = ObjectAnimator.ofFloat(tabIcon, "rotation", 180);
        break;
      case TAB_TYPE_WEB_HEADS:
        animator = ObjectAnimator.ofFloat(tabIcon, "rotation", 125);
        break;
      case TAB_TYPE_CUSTOMIZE:
        animator = ObjectAnimator.ofFloat(tabIcon, "scaleY", 1.2f);
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
        animator = ObjectAnimator.ofFloat(tabIcon, "rotation", -180);
        break;
      case TAB_TYPE_WEB_HEADS:
        animator = ObjectAnimator.ofFloat(tabIcon, "rotation", -90);
        break;
      case TAB_TYPE_CUSTOMIZE:
        animator = ObjectAnimator.ofFloat(tabIcon, "scaleY", 0.75f);
        break;
    }
    if (animator != null)
      animator.setDuration(250);
    return animator;
  }

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({TAB_TYPE_OPTIONS, TAB_TYPE_WEB_HEADS, TAB_TYPE_CUSTOMIZE})
  @interface TabType {
  }
}
