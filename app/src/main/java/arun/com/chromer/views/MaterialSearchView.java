package arun.com.chromer.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.search.SearchSuggestions;
import arun.com.chromer.search.SuggestionAdapter;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MaterialSearchView extends RelativeLayout implements SearchSuggestions.SuggestionsCallback, SuggestionAdapter.SuggestionClickListener {
    @ColorInt
    private final int mNormalColor = ContextCompat.getColor(getContext(), R.color.accent_icon_nofocus);
    @ColorInt
    private final int mFocusedColor = ContextCompat.getColor(getContext(), R.color.accent);

    private boolean mInFocus = false;
    private boolean mShouldClearText;

    @BindView(R.id.msv_left_icon)
    public ImageView mMenuIconView;
    @BindView(R.id.msv_right_icon)
    public ImageView mVoiceIconView;
    @BindView(R.id.msv_label)
    public TextView mLabel;
    @BindView(R.id.msv_edittext)
    public EditText mEditText;
    @BindView(R.id.search_suggestions)
    public RecyclerView mSuggestionList;

    private IconicsDrawable mXIcon;
    private IconicsDrawable mVoiceIcon;
    private IconicsDrawable mMenuIcon;

    private SearchSuggestions mSearchSuggestions;
    private SuggestionAdapter mSuggestionAdapter;

    private Unbinder mUnBinder;

    private InteractionListener mInteractionListener = new InteractionListener() {
        @Override
        public void onVoiceIconClick() {
            // no op
        }

        @Override
        public void onSearchPerformed(@NonNull String query) {
            // no op
        }

        @Override
        public void onMenuClick() {
            // no op
        }
    };

    public MaterialSearchView(Context context) {
        super(context);
        init(context);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mSearchSuggestions = new SearchSuggestions(context, this);
        mXIcon = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_close)
                .color(mNormalColor)
                .sizeDp(16);
        mVoiceIcon = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_microphone)
                .color(mNormalColor)
                .sizeDp(18);
        mMenuIcon = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_menu)
                .color(mNormalColor)
                .sizeDp(18);
        mSuggestionAdapter = new SuggestionAdapter(getContext(), this);
        addView(LayoutInflater.from(getContext()).inflate(R.layout.material_search_view, this, false));
        mUnBinder = ButterKnife.bind(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSuggestionList.setLayoutManager(new LinearLayoutManager(getContext()));
        mSuggestionList.setAdapter(mSuggestionAdapter);
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
                else loseFocus(null);
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
                handleVoiceIconState();

                if (s.length() != 0) {
                    mLabel.setAlpha(0f);
                    mSearchSuggestions.fetchForQuery(s.toString());
                } else mLabel.setAlpha(0.5f);
            }
        });
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mInteractionListener.onSearchPerformed(getURL());
                    return true;
                }
                return false;
            }
        });

        mMenuIconView.setImageDrawable(mMenuIcon);
        mMenuIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mInteractionListener.onMenuClick();
            }
        });

        mVoiceIconView.setImageDrawable(mVoiceIcon);
        mVoiceIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShouldClearText) {
                    mEditText.setText("");
                    loseFocus(null);
                } else {
                    if (mInteractionListener != null) mInteractionListener.onVoiceIconClick();
                }
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mInFocus) gainFocus();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUnBinder.unbind();
    }

    private void gainFocus() {
        float labelAlpha = mEditText.getText().length() == 0 ? 0.5f : 0f;
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(mLabel, "alpha", labelAlpha),
                ObjectAnimator.ofFloat(mEditText, "alpha", 1).setDuration(300)
        );
        hardwareLayers();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                clearLayerTypes();
                handleVoiceIconState();
                setFocusedColor();
            }
        });
        animatorSet.start();
        mInFocus = true;
    }

    private void loseFocus(final Runnable endAction) {
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(mLabel, "alpha", 1),
                ObjectAnimator.ofFloat(mEditText, "alpha", 0).setDuration(300)
        );
        hardwareLayers();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mEditText.clearFocus();
                setNormalColor();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                clearLayerTypes();
                hideKeyboard();
                hideSuggestions();
                if (endAction != null)
                    endAction.run();
            }
        });
        animatorSet.start();
        mInFocus = false;
    }

    private void clearLayerTypes() {
        mLabel.setLayerType(LAYER_TYPE_NONE, null);
        mEditText.setLayerType(LAYER_TYPE_NONE, null);
    }

    private void hardwareLayers() {
        mLabel.setLayerType(LAYER_TYPE_HARDWARE, null);
        mEditText.setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private void hideKeyboard() {
        ((InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void setFocusedColor() {
        mMenuIconView.setImageDrawable(mMenuIcon.color(mFocusedColor));
        mVoiceIconView.setImageDrawable(mVoiceIcon.color(mFocusedColor));
    }

    private void setNormalColor() {
        mMenuIconView.setImageDrawable(mMenuIcon.color(mNormalColor));
        mVoiceIconView.setImageDrawable(mVoiceIcon.color(mNormalColor));
    }

    private void handleVoiceIconState() {
        if (mEditText.getText() != null && mEditText.getText().length() != 0) {
            if (mInFocus)
                mVoiceIconView.setImageDrawable(mXIcon.color(mFocusedColor));
            else mVoiceIconView.setImageDrawable(mXIcon.color(mNormalColor));
            mShouldClearText = true;
        } else {
            if (mInFocus)
                mVoiceIconView.setImageDrawable(mVoiceIcon.color(mFocusedColor));
            else mVoiceIconView.setImageDrawable(mVoiceIcon.color(mNormalColor));
            mShouldClearText = false;
        }
    }

    @Override
    public void clearFocus() {
        loseFocus(null);
        View view = findFocus();
        if (view != null) view.clearFocus();
        super.clearFocus();
    }

    private void clearFocus(@NonNull Runnable endAction) {
        loseFocus(endAction);
        View view = findFocus();
        if (view != null) view.clearFocus();
        super.clearFocus();
    }

    @Override
    public boolean hasFocus() {
        return mInFocus && super.hasFocus();
    }

    @NonNull
    public String getText() {
        return mEditText.getText() == null ? "" : mEditText.getText().toString();
    }

    @NonNull
    public String getURL() {
        return Util.getSearchUrl(getText());
    }

    public void setInteractionListener(@NonNull InteractionListener listener) {
        mInteractionListener = listener;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // no op
    }

    @UiThread
    @Override
    public void onFetchSuggestions(@NonNull List<String> suggestions) {
        mSuggestionAdapter.updateSuggestions(suggestions);
    }

    @Override
    public void onSuggestionClicked(@NonNull final String suggestion) {
        clearFocus(new Runnable() {
            @Override
            public void run() {
                mInteractionListener.onSearchPerformed(Util.getSearchUrl(suggestion));
            }
        });
    }

    private void hideSuggestions() {
        mSuggestionAdapter.clear();
    }

    public interface InteractionListener {
        void onVoiceIconClick();

        void onSearchPerformed(@NonNull String query);

        void onMenuClick();
    }
}
