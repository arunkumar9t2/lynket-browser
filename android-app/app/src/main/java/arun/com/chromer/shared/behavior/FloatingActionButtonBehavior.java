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

package arun.com.chromer.shared.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

/**
 * Created by crugnola on 11/2/16.
 * BottomNavigation
 */
@SuppressWarnings("unused")
public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private static final String TAG = FloatingActionButtonBehavior.class.getSimpleName();
    private int navigationBarHeight = 0;

    public FloatingActionButtonBehavior() {
        super();
    }

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull final CoordinatorLayout.LayoutParams lp) {
        // super.onAttachedToLayoutParams(lp);
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
        if (BottomNavigation.class.isInstance(dependency)) {
            return true;
        } else if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
        final List<View> list = parent.getDependencies(child);
        int bottomMargin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;

        float t = 0;
        float t2 = 0;
        float t3 = 0;
        boolean result = false;

        for (View dep : list) {
            if (Snackbar.SnackbarLayout.class.isInstance(dep)) {
                t += dep.getTranslationY() - dep.getHeight();
                result = true;
            } else if (BottomNavigation.class.isInstance(dep)) {
                BottomNavigation navigation = (BottomNavigation) dep;
                t2 = navigation.getTranslationY() - navigation.getHeight() + (bottomMargin / 2);
                t += t2;
                result = true;
                if (navigationBarHeight > 0) {
                    if (!navigation.isExpanded()) {
                        child.hide();
                    } else {
                        child.show();
                    }
                }
            }
        }

        if (navigationBarHeight > 0 && t2 < 0) {
            t = Math.min(t2, t + navigationBarHeight);
        }
        child.setTranslationY(t);
        return result;
    }

    @Override
    public void onDependentViewRemoved(
            final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);
    }

    public void setNavigationBarHeight(final int height) {
        this.navigationBarHeight = height;
    }
}