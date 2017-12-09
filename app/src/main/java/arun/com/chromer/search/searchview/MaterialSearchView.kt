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

package arun.com.chromer.search.searchview

import android.content.Context
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DividerItemDecoration.VERTICAL
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import arun.com.chromer.R
import arun.com.chromer.search.SuggestionAdapter
import arun.com.chromer.search.SuggestionItem
import arun.com.chromer.util.Utils.getSearchUrl
import butterknife.BindColor
import butterknife.ButterKnife
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.widget_material_search_view.view.*
import rx.Observable
import rx.subjects.PublishSubject

class MaterialSearchView : RelativeLayout, SuggestionAdapter.SuggestionClickListener {
    @BindColor(R.color.accent_icon_nofocus)
    @JvmField
    var normalColor: Int = 0
    @BindColor(R.color.accent)
    @JvmField
    var focusedColor: Int = 0

    private var clearText: Boolean = false

    private lateinit var xIcon: IconicsDrawable
    private lateinit var voiceIcon: IconicsDrawable
    private lateinit var menuIcon: IconicsDrawable

    private lateinit var suggestionAdapter: SuggestionAdapter

    private val voiceIconClicks = PublishSubject.create<Void>()
    private val searchPerforms = PublishSubject.create<String>()
    private val clearClicks = PublishSubject.create<Void>()

    val text: String
        get() = if (msv_edit_text.text == null) "" else msv_edit_text?.text.toString()

    val url: String
        get() = getSearchUrl(text)

    fun getEditText(): EditText = this.msv_edit_text

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        xIcon = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_close)
                .color(normalColor)
                .sizeDp(16)
        voiceIcon = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_microphone)
                .color(normalColor)
                .sizeDp(18)
        menuIcon = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_magnify)
                .color(normalColor)
                .sizeDp(18)
        addView(LayoutInflater.from(getContext()).inflate(R.layout.widget_material_search_view, this, false))
        ButterKnife.bind(this)

        suggestionAdapter = SuggestionAdapter(getContext(), this)
        search_suggestions?.apply {
            layoutManager = LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true)
            adapter = suggestionAdapter
            addItemDecoration(DividerItemDecoration(getContext(), VERTICAL))
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        msv_edit_text?.setOnClickListener { performClick() }
        msv_edit_text?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                gainFocus()
            else
                loseFocus(null)
        }
        msv_edit_text?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_SEARCH) {
                searchPerforms.onNext(url)
                return@setOnEditorActionListener true
            }
            false
        }
        msv_edit_text?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                handleVoiceIconState()
            }
        })

        msv_left_icon?.setImageDrawable(menuIcon)

        msv_right_icon?.setImageDrawable(voiceIcon)
        msv_right_icon?.setOnClickListener {
            if (clearText) {
                msv_edit_text?.setText("")
                clearFocus()
            } else {
                voiceIconClicks.onNext(null)
            }
        }

        setOnClickListener { if (!msv_edit_text!!.hasFocus()) gainFocus() }
    }

    fun voiceIconClicks(): Observable<Void> {
        return voiceIconClicks.asObservable()
    }

    fun searchPerformed(): Observable<String> {
        return searchPerforms.asObservable()
    }

    fun clearClicks(): Observable<Void> {
        return clearClicks.asObservable()
    }

    private fun gainFocus() {
        handleVoiceIconState()
        setFocusedColor()
    }

    private fun loseFocus(endAction: (() -> Unit)?) {
        setNormalColor()
        hideKeyboard()
        hideSuggestions()
        endAction?.invoke()
    }

    private fun hideKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setFocusedColor() {
        msv_left_icon?.setImageDrawable(menuIcon.color(focusedColor))
        msv_right_icon?.setImageDrawable(voiceIcon.color(focusedColor))
    }

    private fun setNormalColor() {
        msv_left_icon?.setImageDrawable(menuIcon.color(normalColor))
        msv_right_icon?.setImageDrawable(voiceIcon.color(normalColor))
    }

    private fun handleVoiceIconState() {
        clearText = !TextUtils.isEmpty(msv_edit_text?.text)
        if (clearText) {
            msv_right_icon?.setImageDrawable(xIcon.color(if (msv_edit_text!!.hasFocus()) focusedColor else normalColor))
        } else {
            msv_right_icon?.setImageDrawable(voiceIcon.color(if (msv_edit_text!!.hasFocus()) focusedColor else normalColor))
        }
    }

    override fun clearFocus() {
        clearFocus(null)
    }

    private fun clearFocus(endAction: (() -> Unit)?) {
        loseFocus(endAction)
        val view = findFocus()
        view?.clearFocus()
        super.clearFocus()
    }

    override fun hasFocus(): Boolean {
        return msv_edit_text!!.hasFocus() && super.hasFocus()
    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        // no op
    }

    override fun onSuggestionClicked(suggestion: String) {
        clearFocus({ searchPerforms.onNext(getSearchUrl(suggestion)) })
    }

    private fun hideSuggestions() {
        clearClicks.onNext(null)
        suggestionAdapter.clear()
    }

    fun setSuggestions(suggestions: List<SuggestionItem>) {
        suggestionAdapter.updateSuggestions(suggestions)
    }
}
