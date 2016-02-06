package arun.com.chromer.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;

public class MaterialSearchView extends FrameLayout {
    private final int normalColor = ContextCompat.getColor(getContext(), R.color.accent_icon_nofocus);
    private final int focusedColor = ContextCompat.getColor(getContext(), R.color.accent);
    private boolean animated = false;
    private ImageView searchIcon;
    private ImageView voiceIcon;
    private TextView label;
    private EditText editText;
    private CardView card;

    public MaterialSearchView(Context context) {
        super(context);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MaterialSearchView, defStyle, 0);

        a.recycle();
    }

    private void toggle() {
        if (animated) loseFocus();
        else gainFocus();
    }

    private void loseFocus() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                getColorChangeAnimatorOnImageView(searchIcon, focusedColor, normalColor, 400),
                getColorChangeAnimatorOnImageView(voiceIcon, focusedColor, normalColor, 400),
                ObjectAnimator.ofFloat(label, "scaleX", 1),
                ObjectAnimator.ofFloat(label, "scaleY", 1),
                ObjectAnimator.ofFloat(label, "alpha", 1),

                ObjectAnimator.ofFloat(editText, "alpha", 0).setDuration(300)
        );

        animatorSet.start();
        editText.clearFocus();

        hideKeyboard();

        animated = false;
    }

    private void hideKeyboard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void gainFocus() {

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                getColorChangeAnimatorOnImageView(searchIcon, normalColor, focusedColor, 300),
                getColorChangeAnimatorOnImageView(voiceIcon, normalColor, focusedColor, 300),
                ObjectAnimator.ofFloat(label, "scaleX", 0.6f),
                ObjectAnimator.ofFloat(label, "scaleY", 0.6f),
                ObjectAnimator.ofFloat(label, "alpha", 0.5f),

                ObjectAnimator.ofFloat(editText, "alpha", 1).setDuration(300)
        );
        animatorSet.start();

        animated = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Inflate and add the xml layout we designed
        addView(LayoutInflater.from(getContext()).inflate(R.layout.material_search_view, this, false));

        editText = (EditText) findViewById(R.id.msv_edittext);
        editText.setImeActionLabel(getContext().getString(R.string.go), KeyEvent.KEYCODE_ENTER);
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performClick();
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) gainFocus();
                else loseFocus();
            }
        });

        searchIcon = (ImageView) findViewById(R.id.msv_left_icon);
        searchIcon.setImageDrawable(new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(normalColor)
                .sizeDp(24));

        voiceIcon = (ImageView) findViewById(R.id.msv_right_icon);
        voiceIcon.setImageDrawable(new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_keyboard_voice)
                .color(normalColor)
                .sizeDp(24));

        label = (TextView) findViewById(R.id.msv_label);
        label.setPivotX(0);
        label.setPivotY(0);

        card = (CardView) findViewById(R.id.msv_card);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!animated) gainFocus();
            }
        });

    }

    private Animator getColorChangeAnimatorOnImageView(final ImageView viewToAnimate, int fromColor, int toColor, int duration) {
        final float[] from = new float[3],
                to = new float[3];

        Color.colorToHSV(fromColor, from);
        Color.colorToHSV(toColor, to);

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(duration);

        final float[] hsv = new float[3];
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                IconicsDrawable drawable = (IconicsDrawable) viewToAnimate.getDrawable();
                viewToAnimate.setImageDrawable(drawable.color(Color.HSVToColor(hsv)));
            }
        });
        return anim;
    }


    private int adjustAlpha(int color, float factor) {
        return Color.argb(Math.round(Color.alpha(color) * factor),
                Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public void clearFocus() {
        loseFocus();
        View view = findFocus();
        if (view != null) view.clearFocus();
        super.clearFocus();
    }

    @Override
    public boolean hasFocus() {
        return animated && super.hasFocus();
    }

    public void setOnKeyListener(OnKeyListener listener) {
        editText.setOnKeyListener(listener);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setVoiceIconClickListener(OnClickListener listener) {
        voiceIcon.setOnClickListener(listener);
    }
}
