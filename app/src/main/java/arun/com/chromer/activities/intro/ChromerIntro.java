package arun.com.chromer.activities.intro;

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
public class ChromerIntro extends AppIntro {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ContextCompat.getColor(this, R.color.colorPrimaryDarker);

        // OPTIONAL METHODS
        // Override bar/separator color
        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name),
                getString(R.string.intro_1),
                R.drawable.chromer_hd_icon,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name),
                getText(R.string.intro_2),
                R.drawable.chromer_hd_icon,
                bgColor));

        addSlide(WebHeadIntroExplainFragment.newInstance(getString(R.string.web_heads),
                getText(R.string.intro_4_new),
                R.drawable.intro_webheads,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.avoid_back_fatigue),
                getText(R.string.intro_back_fatigue),
                R.drawable.back_fatigue,
                bgColor));

        if (Utils.isLollipopAbove())
            addSlide(AppIntroFragment.newInstance(getString(R.string.merge_tabs),
                    getText(R.string.merge_tabs_explanation_intro),
                    R.drawable.merge_tabs,
                    bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.improves_loading_times),
                getText(R.string.intro_5_new),
                R.drawable.intro_scan_links,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.how_to_use),
                getText(R.string.intro_6),
                R.drawable.intro_set_default,
                bgColor));

        addSlide(AppIntroFragment.newInstance(getString(R.string.why_use),
                getText(R.string.intro_7_new),
                R.drawable.intro_why_use,
                bgColor));

        // Hide Skip/Done button
        showSkipButton(true);
        //noinspection deprecation
        showDoneButton(true);
        showStatusBar(true);
        // Color status bar for lollipop above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDarker));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDarker));
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