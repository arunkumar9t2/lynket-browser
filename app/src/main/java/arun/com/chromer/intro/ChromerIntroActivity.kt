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

package arun.com.chromer.intro

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import arun.com.chromer.Chromer
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.di.activity.ActivityModule
import arun.com.chromer.intro.fragments.AppIntroFragment
import arun.com.chromer.intro.fragments.SlideOverExplanationFragment
import arun.com.chromer.intro.fragments.WebHeadsIntroFragment
import arun.com.chromer.shared.base.ProvidesActivityComponent
import arun.com.chromer.util.Utils
import com.github.paolorotolo.appintro.AppIntro

/**
 * Created by Arun on 17/12/2015.
 */
class ChromerIntroActivity : AppIntro(), ProvidesActivityComponent {

    private var activityComponent: ActivityComponent? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        activityComponent = (application as Chromer)
                .appComponent
                .newActivityComponent(ActivityModule(this))
        inject(activityComponent!!)
        super.onCreate(savedInstanceState)

        val bgColor = ContextCompat.getColor(this, R.color.colorPrimaryDarker)

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name),
                getString(R.string.intro_1),
                R.drawable.chromer_hd_icon,
                bgColor))

        addSlide(SlideOverExplanationFragment())

        addSlide(WebHeadsIntroFragment())

        addSlide(AppIntroFragment.newInstance(getString(R.string.amp),
                getString(R.string.amp_summary),
                R.drawable.tutorial_amp_mode,
                bgColor))

        addSlide(AppIntroFragment.newInstance(getString(R.string.article_mode),
                getText(R.string.article_mode_summary),
                R.drawable.tutorial_article_mode,
                bgColor))

        if (Utils.ANDROID_LOLLIPOP) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.merge_tabs),
                    getText(R.string.merge_tabs_explanation_intro),
                    R.drawable.tutorial_merge_tabs_and_apps,
                    bgColor))
        }

        setColorTransitionsEnabled(true)
        showSkipButton(true)
        isProgressButtonEnabled = true
        showStatusBar(true)

        // Color status bar for lollipop above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = bgColor
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun getActivityComponent(): ActivityComponent {
        return activityComponent!!
    }

    override fun inject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityComponent = null
    }
}