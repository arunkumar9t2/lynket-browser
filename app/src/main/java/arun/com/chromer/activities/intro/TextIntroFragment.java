package arun.com.chromer.activities.intro;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import arun.com.chromer.R;
import arun.com.chromer.util.cache.FontCache;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TextIntroFragment extends Fragment {
    @BindView(R.id.title)
    TextView titleTv;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.description)
    TextView descriptionTv;
    @BindView(R.id.root)
    LinearLayout root;

    private int drawable;
    private int bgColor;
    private int titleColor;
    private int descColor;
    private CharSequence title;
    private CharSequence description;

    public TextIntroFragment() {
    }

    public static TextIntroFragment newInstance(CharSequence title, CharSequence description, @ColorInt int bgColor) {
        return newInstance(title, description, 0, bgColor, 0, 0);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public static TextIntroFragment newInstance(CharSequence title, CharSequence description, @DrawableRes int imageDrawable, @ColorInt int bgColor, @ColorInt int titleColor, @ColorInt int descColor) {
        final TextIntroFragment sampleSlide = new TextIntroFragment();
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
        final View view = inflater.inflate(R.layout.fragment_chromer_intro, container, false);
        ButterKnife.bind(this, view);

        titleTv.setTypeface(FontCache.get(FontCache.MONO, getContext()));
        titleTv.setText(this.title);
        if (this.titleColor != 0) {
            titleTv.setTextColor(this.titleColor);
        }

        descriptionTv.setText(this.description);
        if (this.descColor != 0) {
            descriptionTv.setTextColor(this.descColor);
        }
        image.setVisibility(View.GONE);
        root.setBackgroundColor(this.bgColor);
        ButterKnife.bind(this, view);
        return view;
    }
}
