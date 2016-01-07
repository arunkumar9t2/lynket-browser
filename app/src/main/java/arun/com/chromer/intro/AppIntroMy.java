package arun.com.chromer.intro;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import arun.com.chromer.R;

/**
 * Created by Arun on 17/12/2015.
 */
public class AppIntroMy extends AppIntro {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // OPTIONAL METHODS
        // Override bar/separator color
        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name),
                getString(R.string.intro_1),
                R.drawable.web_hi_res_512,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.how_to_use),
                getString(R.string.intro_2),
                R.drawable.chromer_default_image,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.optional),
                getString(R.string.optional_intro),
                R.drawable.optional,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.advantages),
                getString(R.string.intro_3),
                R.drawable.advantages,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.thats_it),
                getString(R.string.intro_4),
                R.drawable.smile,
                ContextCompat.getColor(this, R.color.colorPrimary)));


        // Hide Skip/Done button
        showSkipButton(false);
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