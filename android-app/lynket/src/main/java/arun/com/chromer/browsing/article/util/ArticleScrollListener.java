/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.browsing.article.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import arun.com.chromer.R;

/**
 * Scroll listener for interacting with the toolbar when the recyclerview scrolls. This includes
 * hiding the toolbar and showing it again when appropriate, along with changing the colors.
 */
public final class ArticleScrollListener extends RecyclerView.OnScrollListener {

  private static final int ANIMATION_DURATION = 200; // ms

  private final Toolbar toolbar;
  private final View statusBar;
  private final int transparentColor;
  private int primaryColor;
  private boolean transparentBackground = true;
  private boolean isUpdatingTranslation = false;
  private boolean isUpdatingBackground = false;

  public ArticleScrollListener(Toolbar toolbar, View statusBar, int primaryColor) {
    this.toolbar = toolbar;
    this.statusBar = statusBar;
    this.primaryColor = primaryColor;
    this.transparentColor = ContextCompat.getColor(toolbar.getContext(), R.color.article_toolbarBackground);
  }

  @Override
  public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    super.onScrollStateChanged(recyclerView, newState);
    final LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int firstItem = manager.findFirstCompletelyVisibleItemPosition();
    if (newState == RecyclerView.SCROLL_STATE_IDLE && !transparentBackground &&
      firstItem == 0 && !isUpdatingBackground) {
      animateBackgroundColor(primaryColor, transparentColor, new DecelerateInterpolator());
      transparentBackground = true;
    }
  }

  @Override
  public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    int minDistance = toolbar.getContext().getResources()
      .getDimensionPixelSize(R.dimen.article_minToolbarScroll);
    if (Math.abs(dy) < minDistance) {
      return;
    }
    if (dy > 0 && toolbar.getTranslationY() == 0) {
      Interpolator interpolator = new AccelerateInterpolator();

      if (!isUpdatingTranslation) {
        animateTranslation(-1 * toolbar.getHeight(), interpolator);
      }

      if (transparentBackground && !isUpdatingBackground) {
        animateBackgroundColor(transparentColor, primaryColor, interpolator);
        transparentBackground = false;
      }
    } else if (dy < 0 && toolbar.getTranslationY() != 0) {
      Interpolator interpolator = new DecelerateInterpolator();

      if (!isUpdatingTranslation) {
        animateTranslation(0, interpolator);
      }

      LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
      int firstItem = manager.findFirstVisibleItemPosition();
      if (!transparentBackground && firstItem == 0 && !isUpdatingBackground) {
        animateBackgroundColor(primaryColor, transparentColor, interpolator);
        transparentBackground = true;
      }
    }
  }

  public void setPrimaryColor(@ColorInt int primaryColor) {
    this.primaryColor = primaryColor;
  }

  private void animateTranslation(int to, Interpolator interpolator) {
    toolbar.animate()
      .translationY(to)
      .setDuration(ANIMATION_DURATION)
      .setInterpolator(interpolator)
      .setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          super.onAnimationEnd(animation);
          isUpdatingTranslation = false;
        }
      })
      .start();
    isUpdatingTranslation = true;
  }

  private void animateBackgroundColor(int from, int to, Interpolator interpolator) {
    final ValueAnimator anim = new ValueAnimator();
    anim.setIntValues(from, to);
    anim.setEvaluator(new ArgbEvaluator());
    anim.setInterpolator(interpolator);
    anim.addUpdateListener(valueAnimator -> {
      toolbar.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
      statusBar.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
    });
    anim.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        isUpdatingBackground = false;
      }
    });

    anim.setDuration(ANIMATION_DURATION);
    anim.start();
    isUpdatingBackground = true;
  }

}
