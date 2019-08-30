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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent.EXTRA_RESULTS
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import arun.com.chromer.R
import arun.com.chromer.di.view.ViewComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.search.suggestion.SuggestionController
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionItem.HistorySuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionType
import arun.com.chromer.search.suggestion.items.SuggestionType.*
import arun.com.chromer.shared.Constants.REQUEST_CODE_VOICE
import arun.com.chromer.shared.base.ProvidesActivityComponent
import arun.com.chromer.util.Utils
import arun.com.chromer.util.Utils.getSearchUrl
import arun.com.chromer.util.animations.spring
import arun.com.chromer.util.epoxy.intercepts
import butterknife.BindColor
import butterknife.ButterKnife
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChanges
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.widget_material_search_view.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("CheckResult")
class MaterialSearchView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @BindColor(R.color.accent_icon_no_focus)
    @JvmField
    var normalColor = 0
    @BindColor(R.color.accent)
    @JvmField
    var focusedColor = 0

    private var viewComponent: ViewComponent? = null

    private val xIcon: IconicsDrawable by lazy {
        IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_close)
                .color(normalColor)
                .sizeDp(16)
    }
    private val voiceIcon: IconicsDrawable  by lazy {
        IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_microphone)
                .color(normalColor)
                .sizeDp(18)
    }
    private val menuIcon: IconicsDrawable  by lazy {
        IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_menu)
                .color(normalColor)
                .sizeDp(18)
    }

    @Inject
    lateinit var searchPresenter: SearchPresenter
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var suggestionController: SuggestionController

    private val voiceSearchFailed = PublishSubject.create<Any>()
    private val searchPerforms = PublishSubject.create<String>()
    private val focusChanges = PublishSubject.create<Boolean>()

    val text: String get() = if (msvEditText.text == null) "" else msvEditText?.text.toString()

    val url: String get() = getSearchUrl(text)

    val editText: EditText get() = msvEditText

    init {
        if (context is ProvidesActivityComponent) {
            viewComponent = context
                    .activityComponent
                    .viewComponentFactory().create(this)
                    .also { component -> component.inject(this) }
        }
        addView(LayoutInflater.from(context).inflate(
                R.layout.widget_material_search_view,
                this,
                false
        ))
        ButterKnife.bind(this)

        suggestionController.intercepts()
                .map { it.isEmpty() }
                .takeUntil(detaches())
                .observeOn(schedulerProvider.ui)
                .subscribe(searchSuggestions::gone)

        searchSuggestions.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            layoutManager = GridLayoutManager(context, 4, RecyclerView.VERTICAL, true)
            setController(suggestionController)
            clipToPadding = true
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        msvEditText.run {
            setOnClickListener { performClick() }
            focusChanges()
                    .takeUntil(detaches())
                    .subscribe { hasFocus ->
                        if (hasFocus) {
                            gainFocus()
                        } else {
                            loseFocus()
                        }
                    }
            editorActionEvents { event -> event.actionId == IME_ACTION_SEARCH }
                    .map { url }
                    .takeUntil(detaches())
                    .subscribe(::searchPerformed)
            afterTextChangeEvents()
                    .skipInitialValue()
                    .debounce(100, TimeUnit.MILLISECONDS)
                    .takeUntil(detaches())
                    .observeOn(schedulerProvider.ui)
                    .subscribe { handleIconsState() }
        }

        msvLeftIcon.setImageDrawable(menuIcon)

        msvVoiceIcon.run {
            setImageDrawable(voiceIcon)
            setOnClickListener {
                if (text.isNotEmpty()) {
                    msvEditText?.setText("")
                    clearFocus()
                } else {
                    if (Utils.isVoiceRecognizerPresent(context)) {
                        (context as Activity).startActivityForResult(
                                Utils.getRecognizerIntent(context),
                                REQUEST_CODE_VOICE
                        )
                    } else {
                        voiceSearchFailed.onNext(Any())
                    }
                }
            }
        }

        msvClearIcon.clicks().subscribe {
            msvEditText.text = null
        }

        setOnClickListener { if (!msvEditText.hasFocus()) gainFocus() }

        suggestionController.clicks
                .takeUntil(detaches())
                .subscribe { suggestionItem ->
                    val searchText = getSearchUrl(when (suggestionItem) {
                        is HistorySuggestionItem -> suggestionItem.subTitle
                        else -> suggestionItem.title
                    })
                    searchPerformed(searchText)
                }
        setupPresenter()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewComponent = null
    }

    override fun clearFocus() {
        clearFocus(null)
    }

    override fun hasFocus(): Boolean {
        return if (msvEditText != null) {
            msvEditText.hasFocus() && super.hasFocus()
        } else super.hasFocus()
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) = Unit

    fun voiceSearchFailed(): Observable<Any> = voiceSearchFailed.hide()

    fun searchPerforms(): Observable<String> = searchPerforms.hide()

    fun focusChanges(): Observable<Boolean> = focusChanges.hide()

    fun menuClicks(): Observable<Unit> = msvLeftIcon.clicks().share()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_VOICE) {
            when (resultCode) {
                RESULT_OK -> {
                    val resultList = data?.getStringArrayListExtra(EXTRA_RESULTS)
                    if (resultList != null && resultList.isNotEmpty()) {
                        searchPerformed(getSearchUrl(resultList[0]))
                    }
                }
            }
        }
    }

    fun gainFocus() {
        handleIconsState()
        setFocusedColor()
        focusChanges.onNext(true)
    }

    fun loseFocus(endAction: (() -> Unit)? = null) {
        setNormalColor()
        msvEditText.text = null
        hideKeyboard()
        hideSuggestions()
        endAction?.invoke()
        focusChanges.onNext(false)
        handleIconsState()
    }

    private fun setupPresenter() {
        searchPresenter.run {
            val queryObservable = msvEditText.textChanges()
                    .skipInitialValue()
                    .map { it.toString() }
                    .takeUntil(detaches())
            registerSearch(queryObservable)
            suggestions.observeOn(schedulerProvider.ui)
                    .subscribe { (suggestionType, suggestions) ->
                        setSuggestions(suggestionType, suggestions)
                    }
        }
    }

    private fun clearFocus(endAction: (() -> Unit)?) {
        loseFocus(endAction)
        val view = findFocus()
        view?.clearFocus()
        super.clearFocus()
    }


    private fun hideKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setFocusedColor() {
        msvLeftIcon.setImageDrawable(menuIcon.color(focusedColor))
        msvClearIcon.setImageDrawable(menuIcon.color(focusedColor))
        msvVoiceIcon.setImageDrawable(voiceIcon.color(focusedColor))
    }

    private fun setNormalColor() {
        msvLeftIcon.setImageDrawable(menuIcon.color(normalColor))
        msvClearIcon.setImageDrawable(menuIcon.color(normalColor))
        msvVoiceIcon.setImageDrawable(voiceIcon.color(normalColor))
    }

    private fun handleIconsState() {
        val color = if (msvEditText.hasFocus()) focusedColor else normalColor
        if (text.isNotEmpty()) {
            msvClearIcon.run {
                setImageDrawable(xIcon.color(color))
                spring(SpringAnimation.ALPHA).animateToFinalPosition(1F)
            }
            msvVoiceIcon.setImageDrawable(voiceIcon.color(color))
        } else {
            msvClearIcon.run {
                setImageDrawable(xIcon.color(color))
                spring(SpringAnimation.ALPHA).animateToFinalPosition(0F)
            }
            msvVoiceIcon.setImageDrawable(voiceIcon.color(color))
        }
    }

    private fun searchPerformed(url: String) {
        clearFocus { searchPerforms.onNext(url) }
    }

    private fun hideSuggestions() {
        suggestionController.clear()
    }

    private fun setSuggestions(
            suggestionType: SuggestionType,
            suggestionItems: List<SuggestionItem>
    ) = when (suggestionType) {
        COPY -> suggestionController.copySuggestions = suggestionItems
        GOOGLE -> suggestionController.googleSuggestions = suggestionItems
        HISTORY -> suggestionController.historySuggestions = suggestionItems
    }
}
