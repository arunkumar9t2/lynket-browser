package arun.com.chromer.search.suggestion

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import arun.com.chromer.R
import arun.com.chromer.di.scopes.PerActivity
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.model.suggestionLayout
import com.airbnb.epoxy.AsyncEpoxyController
import com.jakewharton.rxrelay2.PublishRelay
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.reactivex.Observable
import javax.inject.Inject

@PerActivity
class SuggestionController
@Inject
constructor(private val activity: Activity) : AsyncEpoxyController() {

    private val clicksSubject = PublishRelay.create<SuggestionItem>()

    val clicks: Observable<SuggestionItem> get() = clicksSubject.hide()

    private val searchIcon: Drawable by lazy {
        IconicsDrawable(activity)
                .icon(CommunityMaterial.Icon.cmd_magnify)
                .color(ContextCompat.getColor(activity, R.color.material_dark_light))
                .sizeDp(18)
    }

    private val historyIcon: Drawable by lazy {
        IconicsDrawable(activity)
                .icon(CommunityMaterial.Icon.cmd_history)
                .color(ContextCompat.getColor(activity, R.color.md_red_500))
                .sizeDp(18)
    }

    private val copyIcon: Drawable by lazy {
        IconicsDrawable(activity)
                .icon(CommunityMaterial.Icon.cmd_content_copy)
                .color(ContextCompat.getColor(activity, R.color.md_green_500))
                .sizeDp(18)
    }

    var suggestions: List<SuggestionItem> = emptyList()
        set(value) {
            field = value
            requestDelayedModelBuild(0)
        }

    fun clear() {
        suggestions = emptyList()
    }

    override fun buildModels() {
        suggestions.forEach { suggestion ->
            suggestionLayout {
                id(suggestion.hashCode())
                suggestionItem(suggestion)
                copyIcon(copyIcon)
                historyIcon(historyIcon)
                searchIcon(searchIcon)
                onClickListener { _ ->
                    clicksSubject.accept(suggestion)
                }
            }
        }
    }
}