/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.views.searchview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.search.SuggestionAdapter;
import arun.com.chromer.search.SuggestionItem;
import arun.com.chromer.util.Utils;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class MaterialSearchView extends RelativeLayout implements SuggestionAdapter.SuggestionClickListener {
    @BindColor(R.color.accent_icon_nofocus)
    int normalColor;
    @BindColor(R.color.accent)
    int focusedColor;

    private boolean inFocus = false;
    private boolean clearText;

    @BindView(R.id.msv_left_icon)
    public ImageView menuIconView;
    @BindView(R.id.msv_right_icon)
    public ImageView voiceIconView;
    @BindView(R.id.msv_label)
    public TextView label;
    @BindView(R.id.msv_edit_text)
    public EditText editText;
    @BindView(R.id.search_suggestions)
    public RecyclerView suggestionList;
    @BindView(R.id.msv_card)
    public CardView card;

    private IconicsDrawable xIcon;
    private IconicsDrawable voiceIcon;
    private IconicsDrawable menuIcon;

    private SuggestionAdapter suggestionAdapter;

    private PublishSubject<Void> voiceIconClicks = PublishSubject.create();
    private PublishSubject<String> searchPerforms = PublishSubject.create();
    private PublishSubject<Void> clearClicks = PublishSubject.create();

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
        xIcon = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_close)
                .color(normalColor)
                .sizeDp(16);
        voiceIcon = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_microphone)
                .color(normalColor)
                .sizeDp(18);
        menuIcon = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_magnify)
                .color(normalColor)
                .sizeDp(18);
        addView(LayoutInflater.from(getContext()).inflate(R.layout.widget_material_search_view, this, false));
        ButterKnife.bind(this);

        suggestionAdapter = new SuggestionAdapter(getContext(), this);
        suggestionList.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionList.setItemAnimator(null);
        suggestionList.setAdapter(suggestionAdapter);
        suggestionList.addItemDecoration(new DividerItemDecoration(getContext(), VERTICAL));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        editText.setOnClickListener(v -> performClick());
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) gainFocus();
            else loseFocus(null);
        });
        editText.addTextChangedListener(new TextWatcher() {
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
                    label.setAlpha(0f);
                } else label.setAlpha(0.5f);
            }
        });
        editText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == IME_ACTION_SEARCH) {
                searchPerforms.onNext(getURL());
                return true;
            }
            return false;
        });

        menuIconView.setImageDrawable(menuIcon);

        voiceIconView.setImageDrawable(voiceIcon);
        voiceIconView.setOnClickListener(v -> {
            if (clearText) {
                editText.setText("");
                loseFocus(null);
            } else {
                voiceIconClicks.onNext(null);
            }
        });

        setOnClickListener(v -> {
            if (!inFocus) gainFocus();
        });
    }

    public Observable<Void> voiceIconClicks() {
        return voiceIconClicks.asObservable();
    }

    public Observable<String> searchPerfomed() {
        return searchPerforms.asObservable();
    }

    public Observable<Void> clearClicks() {
        return clearClicks.asObservable();
    }

    private void gainFocus() {
        final float labelAlpha = editText.getText().length() == 0 ? 0.5f : 0f;
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(label, "alpha", labelAlpha),
                ObjectAnimator.ofFloat(editText, "alpha", 1).setDuration(300)
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
        inFocus = true;
    }

    private void loseFocus(@Nullable final Runnable endAction) {
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(label, "alpha", 1),
                ObjectAnimator.ofFloat(editText, "alpha", 0).setDuration(300)
        );
        hardwareLayers();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                editText.clearFocus();
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
        inFocus = false;
    }

    private void clearLayerTypes() {
        label.setLayerType(LAYER_TYPE_NONE, null);
        editText.setLayerType(LAYER_TYPE_NONE, null);
    }

    private void hardwareLayers() {
        label.setLayerType(LAYER_TYPE_HARDWARE, null);
        editText.setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private void hideKeyboard() {
        ((InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void setFocusedColor() {
        menuIconView.setImageDrawable(menuIcon.color(focusedColor));
        voiceIconView.setImageDrawable(voiceIcon.color(focusedColor));
    }

    private void setNormalColor() {
        menuIconView.setImageDrawable(menuIcon.color(normalColor));
        voiceIconView.setImageDrawable(voiceIcon.color(normalColor));
    }

    private void handleVoiceIconState() {
        if (editText.getText() != null && editText.getText().length() != 0) {
            if (inFocus)
                voiceIconView.setImageDrawable(xIcon.color(focusedColor));
            else voiceIconView.setImageDrawable(xIcon.color(normalColor));
            clearText = true;
        } else {
            if (inFocus)
                voiceIconView.setImageDrawable(voiceIcon.color(focusedColor));
            else voiceIconView.setImageDrawable(voiceIcon.color(normalColor));
            clearText = false;
        }
    }

    @NonNull
    public EditText getEditText() {
        return editText;
    }

    @Override
    public void clearFocus() {
        clearFocus(null);
    }

    private void clearFocus(@Nullable Runnable endAction) {
        loseFocus(endAction);
        final View view = findFocus();
        if (view != null) view.clearFocus();
        super.clearFocus();
    }

    @Override
    public boolean hasFocus() {
        return inFocus && super.hasFocus();
    }

    @NonNull
    public String getText() {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    @NonNull
    public String getURL() {
        return Utils.getSearchUrl(getText());
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // no op
    }

    @Override
    public void onSuggestionClicked(@NonNull final String suggestion) {
        clearFocus(() -> searchPerforms.onNext(Utils.getSearchUrl(suggestion)));
    }

    private void hideSuggestions() {
        clearClicks.onNext(null);
        suggestionAdapter.clear();
    }

    public void setSuggestions(@NonNull List<SuggestionItem> suggestions) {
        suggestionAdapter.updateSuggestions(suggestions);
    }
}
