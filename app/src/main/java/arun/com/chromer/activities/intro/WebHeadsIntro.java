package arun.com.chromer.activities.intro;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;

import arun.com.chromer.R;

/**
 * Created by Arun on 17/12/2015.
 */
public class WebHeadsIntro extends AppIntro {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        int bgColor = ContextCompat.getColor(this, R.color.colorPrimaryDarker);

        // OPTIONAL METHODS
        // Override bar/separator color
        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getString(R.string.webheads_intro_1),
                R.drawable.webheads_intro_1,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getString(R.string.webheads_intro_2),
                R.drawable.webheads_intro_2,
                bgColor));


        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getString(R.string.webheads_intro_3),
                R.drawable.webheads_intro_3,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getString(R.string.webheads_intro_4),
                R.drawable.webheads_intro_4,
                bgColor));

        // Hide Skip/Done button
        showSkipButton(true);

        //noinspection deprecation
        showDoneButton(true);
        showStatusBar(false);
    }

    @Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onSlideChanged() {

    }
}