package arun.com.chromer.activities.intro;

/**
 * Created by Arun on 06/02/2016.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.paolorotolo.appintro.R.id;
import com.github.paolorotolo.appintro.R.layout;

public class AppIntroFragment extends Fragment {
    private int drawable;
    private int bgColor;
    private int titleColor;
    private int descColor;
    private CharSequence title;
    private CharSequence description;

    public AppIntroFragment() {
    }

    public static AppIntroFragment newInstance(CharSequence title, CharSequence description, int imageDrawable, int bgColor) {
        return newInstance(title, description, imageDrawable, bgColor, 0, 0);
    }

    @SuppressWarnings("WeakerAccess")
    public static AppIntroFragment newInstance(CharSequence title, CharSequence description, int imageDrawable, int bgColor, int titleColor, int descColor) {
        AppIntroFragment sampleSlide = new AppIntroFragment();
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

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null && this.getArguments().size() != 0) {
            this.drawable = this.getArguments().getInt("drawable");
            this.title = this.getArguments().getCharSequence("title");
            this.description = this.getArguments().getCharSequence("desc");
            this.bgColor = this.getArguments().getInt("bg_color");
            this.titleColor = this.getArguments().containsKey("title_color") ? this.getArguments().getInt("title_color") : 0;
            this.descColor = this.getArguments().containsKey("desc_color") ? this.getArguments().getInt("desc_color") : 0;
        }

    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(layout.fragment_intro, container, false);
        TextView t = (TextView) v.findViewById(id.title);
        TextView d = (TextView) v.findViewById(id.description);
        ImageView i = (ImageView) v.findViewById(id.image);
        LinearLayout m = (LinearLayout) v.findViewById(id.main);
        t.setText(this.title);
        if (this.titleColor != 0) {
            t.setTextColor(this.titleColor);
        }

        d.setText(this.description);
        if (this.descColor != 0) {
            d.setTextColor(this.descColor);
        }

        // Use glide to load the drawable
        Glide.with(this).load(drawable).crossFade().into(i);

        m.setBackgroundColor(this.bgColor);
        return v;
    }
}
