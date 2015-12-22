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
        addSlide(AppIntroFragment.newInstance("Chromer",
                "Chromer allows you to use Chrome's custom tabs feature, which is an extremely light weight chrome tab for faster and secure browsing experience.",
                R.drawable.web_hi_res_512,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("How to use?",
                "Just set Chromer as your default browser, Chromer will display the link in a non intrusive way. Or choose Chromer when prompted.",
                R.drawable.chromer_default_image,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Advantages?",
                "Faster loading, non intrusive, saves data, shared login info from Chrome, auto complete forms and SECURE with latest updates!",
                R.drawable.advantages,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("That's it!",
                "Configure Chromer to your liking by launching the main app. Enjoy and share your thoughts!",
                R.drawable.smile,
                ContextCompat.getColor(this, R.color.colorPrimary)));
        // Hide Skip/Done button
        showSkipButton(false);
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