package arun.com.chromer.activities.intro;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;

import arun.com.chromer.R;

/**
 * Created by Arun on 17/12/2015.
 */
public class ChromerIntro extends AppIntro {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        int bgColor = ContextCompat.getColor(this, R.color.colorPrimaryDarker);

        // OPTIONAL METHODS
        // Override bar/separator color
        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name),
                getString(R.string.intro_1),
                R.drawable.chromer_hd_icon,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name),
                getString(R.string.intro_2),
                R.drawable.chromer_hd_icon,
                bgColor));


        addSlide(AppIntroFragment.newInstance(getString(R.string.warmup_browser),
                getString(R.string.intro_3),
                R.drawable.chromer_hd_icon,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.web_heads),
                getString(R.string.intro_4),
                R.drawable.intro_webheads,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.pre_fetch_content),
                getString(R.string.intro_5),
                R.drawable.intro_scan_links,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.how_to_use),
                getString(R.string.intro_6),
                R.drawable.intro_set_default,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.why_use),
                getString(R.string.intro_7),
                R.drawable.intro_why_use,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.thats_it),
                getString(R.string.intro_8),
                R.drawable.intro_customizable,
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