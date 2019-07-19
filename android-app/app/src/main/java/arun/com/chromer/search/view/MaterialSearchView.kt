/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.search.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.di.view.ViewComponent
import arun.com.chromer.di.view.ViewModule
import arun.com.chromer.search.suggestion.SuggestionAdapter
import arun.com.chromer.search.suggestion.items.HistorySuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.ProvidesActivityComponent
import arun.com.chromer.util.Utils
import arun.com.chromer.util.Utils.getSearchUrl
import butterknife.BindColor
import butterknife.ButterKnife
import com.jakewharton.rxbinding.widget.RxTextView
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.widget_material_search_view.view.*
import rx.Observable
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class MaterialSearchView : RelativeLayout, Search.View {
    @BindColor(R.color.accent_icon_no_focus)
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

    private val voiceSearchFailed = PublishSubject.create<Void>()
    private val searchPerforms = PublishSubject.create<String>()
    private val focusChanges = PublishSubject.create<Boolean>()

    private val subs = CompositeSubscription()

    private var viewComponent: ViewComponent? = null

    @Inject
    lateinit var searchPresenter: Search.Presenter

    val text: String get() = if (msvEditText.text == null) "" else msvEditText?.text.toString()

    val url: String get() = getSearchUrl(text)

    val editText: EditText get() = msvEditText

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
        if (context is ProvidesActivityComponent) {
            viewComponent = context.activityComponent.newViewComponent(ViewModule(this))
            viewComponent?.inject(this)
        }

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

        suggestionAdapter = SuggestionAdapter(getContext())
        search_suggestions?.apply {
            layoutManager = LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true)
            adapter = suggestionAdapter
            addItemDecoration(DividerItemDecoration(getContext(), VERTICAL))
        }

        subs.add(suggestionAdapter.clicks()
                .doOnNext {
                    searchPerformed(getSearchUrl(if (it is HistorySuggestionItem) it.subTitle else it.title))
                }.subscribe())

        searchPresenter.registerSearch(RxTextView.textChangeEvents(msvEditText)
                .filter { it != null }
                .map { it.text().toString() })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        msvEditText?.setOnClickListener { performClick() }
        msvEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                gainFocus()
            } else {
                loseFocus(null)
            }
        }
        msvEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_SEARCH) {
                searchPerformed(url)
                return@setOnEditorActionListener true
            }
            false
        }
        msvEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                handleVoiceIconState()
            }
        })

        msvLeftIcon?.setImageDrawable(menuIcon)

        msvRightIcon?.setImageDrawable(voiceIcon)
        msvRightIcon?.setOnClickListener {
            if (clearText) {
                msvEditText?.setText("")
                clearFocus()
            } else {
                if (Utils.isVoiceRecognizerPresent(context)) {
                    (context as Activity).startActivityForResult(Utils.getRecognizerIntent(context), Constants.REQUEST_CODE_VOICE)
                } else {
                    voiceSearchFailed.onNext(null)
                }
            }
        }

        setOnClickListener { if (!msvEditText!!.hasFocus()) gainFocus() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        searchPresenter.takeView(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewComponent = null
        searchPresenter.cleanUp()
        subs.clear()
    }

    override fun clearFocus() {
        clearFocus(null)
    }

    override fun hasFocus(): Boolean {
        return if (msvEditText != null) {
            msvEditText.hasFocus() && super.hasFocus()
        } else super.hasFocus()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        // no op
    }

    fun voiceSearchFailed(): Observable<Void> {
        return voiceSearchFailed.asObservable()
    }

    fun searchPerforms(): Observable<String> {
        return searchPerforms.asObservable().filter { it != null }
    }

    fun focusChanges(): Observable<Boolean> = focusChanges.asObservable()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_VOICE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val resultList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (resultList != null && !resultList.isEmpty()) {
                        searchPerformed(getSearchUrl(resultList[0]))
                    }
                }
            }
        }
    }

    fun gainFocus() {
        handleVoiceIconState()
        setFocusedColor()
        focusChanges.onNext(true)
    }

    fun loseFocus(endAction: (() -> Unit)?) {
        setNormalColor()
        msvEditText.text = null
        hideKeyboard()
        hideSuggestions()
        endAction?.invoke()
        focusChanges.onNext(false)
    }

    private fun hideKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setFocusedColor() {
        msvLeftIcon?.setImageDrawable(menuIcon.color(focusedColor))
        msvRightIcon?.setImageDrawable(voiceIcon.color(focusedColor))
    }

    private fun setNormalColor() {
        msvLeftIcon?.setImageDrawable(menuIcon.color(normalColor))
        msvRightIcon?.setImageDrawable(voiceIcon.color(normalColor))
    }

    private fun handleVoiceIconState() {
        clearText = !TextUtils.isEmpty(msvEditText?.text) || suggestionAdapter.itemCount != 0
        if (clearText) {
            msvRightIcon?.setImageDrawable(xIcon.color(if (msvEditText!!.hasFocus()) focusedColor else normalColor))
        } else {
            msvRightIcon?.setImageDrawable(voiceIcon.color(if (msvEditText!!.hasFocus()) focusedColor else normalColor))
        }
    }

    private fun clearFocus(endAction: (() -> Unit)?) {
        loseFocus(endAction)
        val view = findFocus()
        view?.clearFocus()
        super.clearFocus()
    }

    private fun searchPerformed(url: String) {
        clearFocus { searchPerforms.onNext(url) }
    }

    private fun hideSuggestions() {
        suggestionAdapter.clear()
    }

    override fun setSuggestions(suggestionItems: List<SuggestionItem>) {
        suggestionAdapter.updateSuggestions(suggestionItems)
    }
}
