package arun.com.chromer.activities.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arun.com.chromer.R;
import arun.com.chromer.util.ColorUtil;

/**
 * Created by Arun on 16/09/2016.
 */

public class WebHeadIntroExplainFragment extends AppIntroFragment {

    public static WebHeadIntroExplainFragment newInstance(CharSequence title, CharSequence description, int imageDrawable, int bgColor) {
        return newInstance(title, description, imageDrawable, bgColor, 0, 0);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public static WebHeadIntroExplainFragment newInstance(CharSequence title, CharSequence description, int imageDrawable, int bgColor, int titleColor, int descColor) {
        WebHeadIntroExplainFragment sampleSlide = new WebHeadIntroExplainFragment();
        Bundle args = new Bundle();
        args.putCharSequence("title", title);
        args.putCharSequence("desc", description);
        args.putInt("drawable", imageDrawable);
        args.putInt("bg_color", bgColor);
        args.putInt("title_color", titleColor);
        args.putInt("desc_color", descColor);
        sampleSlide.setArguments(args);
        return sampleSlide;
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            final TextView d = (TextView) v.findViewById(com.github.paolorotolo.appintro.R.id.description);
            d.setBackground(ColorUtil.getRippleDrawableCompat(ContextCompat.getColor(getActivity(), R.color.accent)));
            d.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), WebHeadsIntro.class));
                }
            });
        }
        return v;
    }
}
