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

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package arun.com.chromer.search.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import arun.com.chromer.search.view.MaterialSearchView;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class MaterialSearchViewBehavior extends Behavior<MaterialSearchView> {
    private int navigationBarHeight = 0;

    public MaterialSearchViewBehavior() {
        super();
    }

    public MaterialSearchViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull final CoordinatorLayout.LayoutParams lp) {
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final MaterialSearchView child, final View dependency) {
        if (BottomNavigation.class.isInstance(dependency)) {
            return true;
        } else if (Snackbar.SnackbarLayout.class.isInstance(dependency)) {
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final MaterialSearchView child, final View dependency) {
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
                t2 = navigation.getTranslationY() - navigation.getHeight() + bottomMargin;
                t += t2;
                result = true;

                if (navigationBarHeight > 0) {
                    if (!navigation.isExpanded()) {
                        //child.hide();
                    } else {
                        // child.show();
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

    public void setNavigationBarHeight(final int height) {
        this.navigationBarHeight = height;
    }
}
