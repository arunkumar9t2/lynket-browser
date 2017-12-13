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

package arun.com.chromer.intro;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;

import arun.com.chromer.R;
import arun.com.chromer.util.Utils;

/**
 * Created by Arun on 17/12/2015.
 */
public class WebHeadsIntro extends AppIntro {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ContextCompat.getColor(this, R.color.md_teal_800);

        // OPTIONAL METHODS
        // Override bar/separator color
        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getText(R.string.webheads_intro_1),
                R.drawable.intro_webheads,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.multiple_links),
                getText(R.string.webheads_intro_2_new),
                R.drawable.webheads_2,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getText(R.string.webheads_intro_3),
                R.drawable.webheads_3,
                bgColor));

        if (Utils.isLollipopAbove()) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.save_time),
                    getText(R.string.webheads_intro_4_lolli),
                    R.drawable.webheads_3,
                    bgColor));
        } else {
            addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                    getText(R.string.webheads_intro_4),
                    R.drawable.webheads_3,
                    bgColor));
        }
        // Hide Skip/Done button
        showSkipButton(true);

        //noinspection deprecation
        showDoneButton(true);
        showStatusBar(true);
        // Color status bar for lollipop above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(bgColor);
            getWindow().setNavigationBarColor(bgColor);
        }
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}