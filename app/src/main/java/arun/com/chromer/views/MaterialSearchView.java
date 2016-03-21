package arun.com.chromer.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
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
    private final int mNormalColor = ContextCompat.getColor(getContext(), R.color.accent_icon_nofocus);
    private final int mFocusedColor = ContextCompat.getColor(getContext(), R.color.accent);
    private boolean mAnimated = false;
    private ImageView mSearchIconView;
    private ImageView mVoiceIconView;
    private TextView mLabel;
    private EditText mEditText;
    private boolean mShouldClearText;
    private final IconicsDrawable mXIcon = new IconicsDrawable(getContext())
            .icon(GoogleMaterial.Icon.gmd_clear)
            .color(mNormalColor)
            .sizeDp(16);
    private final IconicsDrawable mVoiceIcon = new IconicsDrawable(getContext())
            .icon(GoogleMaterial.Icon.gmd_keyboard_voice)
            .color(mNormalColor)
            .sizeDp(18);

    private VoiceIconClickListener mVoiceClickListener;

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

    private void loseFocus() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                getColorChangeAnimatorOnImageView(mSearchIconView, mFocusedColor, mNormalColor, 400),
                getColorChangeAnimatorOnImageView(mVoiceIconView, mFocusedColor, mNormalColor, 400),
                ObjectAnimator.ofFloat(mLabel, "scaleX", 1),
                ObjectAnimator.ofFloat(mLabel, "scaleY", 1),
                ObjectAnimator.ofFloat(mLabel, "alpha", 1),
                ObjectAnimator.ofFloat(mEditText, "alpha", 0).setDuration(300)
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mShouldClearText) {
                    mVoiceIconView.setImageDrawable(mVoiceIcon.color(mNormalColor));
                    mShouldClearText = false;
                }
            }
        });

        animatorSet.start();
        mEditText.clearFocus();

        hideKeyboard();

        mAnimated = false;
    }

    private void hideKeyboard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void gainFocus() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                getColorChangeAnimatorOnImageView(mSearchIconView, mNormalColor, mFocusedColor, 400),
                getColorChangeAnimatorOnImageView(mVoiceIconView, mNormalColor, mFocusedColor, 400),
                ObjectAnimator.ofFloat(mLabel, "scaleX", 0.6f),
                ObjectAnimator.ofFloat(mLabel, "scaleY", 0.6f),
                ObjectAnimator.ofFloat(mLabel, "alpha", 0.5f),
                ObjectAnimator.ofFloat(mEditText, "alpha", 1).setDuration(300)
        );
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                handleVoiceIcon();
            }
        });
        animatorSet.start();

        mAnimated = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Inflate and add the xml layout we designed
        addView(LayoutInflater.from(getContext()).inflate(R.layout.material_search_view, this, false));

        mEditText = (EditText) findViewById(R.id.msv_edittext);
        mEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performClick();
            }
        });
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) gainFocus();
                else loseFocus();
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handleVoiceIcon();
            }
        });

        mSearchIconView = (ImageView) findViewById(R.id.msv_left_icon);
        mSearchIconView.setImageDrawable(new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(mNormalColor)
                .sizeDp(18));

        mVoiceIconView = (ImageView) findViewById(R.id.msv_right_icon);
        mVoiceIconView.setImageDrawable(mVoiceIcon);
        mVoiceIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShouldClearText) {
                    mEditText.setText("");
                    loseFocus();
                } else {
                    if (mVoiceClickListener != null) mVoiceClickListener.onClick(v);
                }
            }
        });

        mLabel = (TextView) findViewById(R.id.msv_label);
        mLabel.setPivotX(0);
        mLabel.setPivotY(0);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAnimated) gainFocus();
            }
        });
    }

    private void handleVoiceIcon() {
        if (mEditText.getText() != null && mEditText.getText().length() != 0) {
            if (!mShouldClearText) {
                if (mAnimated)
                    mVoiceIconView.setImageDrawable(mXIcon.color(mFocusedColor));
                else mVoiceIconView.setImageDrawable(mXIcon.color(mNormalColor));
            }
            mShouldClearText = true;
        } else {
            if (mShouldClearText) {
                if (mAnimated)
                    mVoiceIconView.setImageDrawable(mVoiceIcon.color(mNormalColor));
                else mVoiceIconView.setImageDrawable(mVoiceIcon.color(mFocusedColor));
            }
            mShouldClearText = false;
        }
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

    @Override
    public void clearFocus() {
        loseFocus();
        View view = findFocus();
        if (view != null) view.clearFocus();
        super.clearFocus();
    }

    @Override
    public boolean hasFocus() {
        return mAnimated && super.hasFocus();
    }

    public void setOnKeyListener(TextView.OnEditorActionListener listener) {
        mEditText.setOnEditorActionListener(listener);
    }

    public String getText() {
        return mEditText.getText() == null ? "" : mEditText.getText().toString();
    }

    public void setVoiceIconClickListener(VoiceIconClickListener listener) {
        mVoiceClickListener = listener;
    }

    public interface VoiceIconClickListener {
        void onClick(View v);
    }
}
