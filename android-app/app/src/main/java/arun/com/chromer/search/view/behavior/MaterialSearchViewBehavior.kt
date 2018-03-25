/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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
package arun.com.chromer.search.view.behavior

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.CoordinatorLayout.Behavior
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

import arun.com.chromer.search.view.MaterialSearchView
import it.sephiroth.android.library.bottomnavigation.BottomNavigation

/**
 * Custom behavior implementation for {@link android.support.design.widget.FloatingActionButton} so
 * that it can be rendered above the {@link MaterialSearchView}. Handles cases when {@link Snackbar}
 * is shown as well.
 */
class MaterialSearchViewBehavior : Behavior<MaterialSearchView> {
    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onAttachedToLayoutParams(lp: CoordinatorLayout.LayoutParams) {}

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: MaterialSearchView?, dependency: View?): Boolean {
        return when (dependency) {
            is BottomNavigation -> {
                true
            }
            is Snackbar.SnackbarLayout -> {
                true
            }
            else -> super.layoutDependsOn(parent, child, dependency)
        }
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: MaterialSearchView?, dependency: View?): Boolean {
        val dependencies = parent!!.getDependencies(child!!)
        val bottomMargin = (child.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        var t = 0f
        var result = false

        for (dep in dependencies) {
            when (dep) {
                is Snackbar.SnackbarLayout -> {
                    t += dep.translationY - dep.height
                    result = true
                }
                is BottomNavigation -> {
                    t += dep.translationY - dep.height + bottomMargin
                    result = true
                    if (!dep.isExpanded) {
                        // child.hide();
                    } else {
                        // child.show();
                    }
                }
            }
        }
        child.translationY = t
        return result
    }
}
