/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.search.suggestion

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import arun.com.chromer.R
import arun.com.chromer.search.provider.SearchProvider
import arun.com.chromer.search.provider.searchProviderLayout
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionItem.HistorySuggestionItem
import arun.com.chromer.search.suggestion.model.suggestionLayout
import arun.com.chromer.shared.epxoy.model.headerLayout
import arun.com.chromer.shared.epxoy.model.spaceLayout
import arun.com.chromer.shared.epxoy.model.websiteLayout
import arun.com.chromer.tabs.TabsManager
import com.airbnb.epoxy.AsyncEpoxyController
import com.jakewharton.rxrelay2.PublishRelay
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import dev.arunkumar.android.dagger.view.PerView
import dev.arunkumar.android.epoxy.span.TotalSpanOverride
import dev.arunkumar.common.context.dpToPx
import io.reactivex.Observable
import javax.inject.Inject

@PerView
class SuggestionController
@Inject
constructor(
  private val activity: Activity,
  private val tabsManager: TabsManager
) : AsyncEpoxyController() {

  private val suggestionsClicksRelay = PublishRelay.create<SuggestionItem>()
  private val suggestionLongClickRelay = PublishRelay.create<SuggestionItem>()
  private val searchProviderRelay = PublishRelay.create<SearchProvider>()

  val suggestionClicks: Observable<SuggestionItem> = suggestionsClicksRelay.hide()
  val suggestionLongClicks: Observable<SuggestionItem> = suggestionLongClickRelay.hide()
  val searchProviderClicks: Observable<SearchProvider> = searchProviderRelay.hide()

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

  var query: String = ""

  var copySuggestions: List<SuggestionItem> = emptyList()
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  var googleSuggestions: List<SuggestionItem> = emptyList()
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  var historySuggestions: List<SuggestionItem> = emptyList()
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  var searchProviders: List<SearchProvider> = emptyList()
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  var showSearchProviders = false
    set(value) {
      field = value
      requestDelayedModelBuild(0)
    }

  fun clear() {
    showSearchProviders = false
    copySuggestions = emptyList()
    googleSuggestions = emptyList()
    historySuggestions = emptyList()
  }


  override fun buildModels() {
    if (searchProviders.isNotEmpty() && showSearchProviders) {
      searchProviders.forEach { searchProvider ->
        searchProviderLayout {
          id(searchProvider.hashCode())
          searchProvider(searchProvider)
          spanSizeOverride { _, _, _ -> 2 }
          onClick { _ ->
            searchProviderRelay.accept(searchProvider)
            showSearchProviders = false
          }
        }
      }
      headerLayout {
        id("search-engines-header")
        title(activity.getString(R.string.pick_search_engines))
        spanSizeOverride(TotalSpanOverride)
      }
      spaceLayout {
        id("search-engines-header-space")
        spaceHeight(activity.dpToPx(8.0))
        spaceWidth(MATCH_PARENT)
        spanSizeOverride(TotalSpanOverride)
      }
      return
    }

    copySuggestions.forEach { suggestion ->
      suggestionLayout {
        id(suggestion.hashCode())
        suggestionItem(suggestion)
        copyIcon(copyIcon)
        historyIcon(historyIcon)
        searchIcon(searchIcon)
        spanSizeOverride(TotalSpanOverride)
        onClickListener { _ ->
          suggestionsClicksRelay.accept(suggestion)
        }
        onLongClickListener { _ ->
          suggestionLongClickRelay.accept(suggestion)
          return@onLongClickListener true
        }
      }
    }

    historySuggestions
      .filterIsInstance<HistorySuggestionItem>()
      .onEach { suggestion ->
        websiteLayout {
          id(suggestion.hashCode())
          website(suggestion.website)
          tabsManager(tabsManager)
          query(query)
        }
      }.count().let { size ->
        if (size != 0) {
          headerLayout {
            id("history-header")
            title(activity.getString(R.string.title_history))
            spanSizeOverride(TotalSpanOverride)
          }
          spaceLayout {
            id("history-header-space")
            spaceHeight(activity.dpToPx(4.0))
            spaceWidth(MATCH_PARENT)
            spanSizeOverride(TotalSpanOverride)
          }
        }
      }

    googleSuggestions.forEach { suggestion ->
      suggestionLayout {
        id(suggestion.hashCode())
        suggestionItem(suggestion)
        copyIcon(copyIcon)
        historyIcon(historyIcon)
        searchIcon(searchIcon)
        spanSizeOverride(TotalSpanOverride)
        query(query)
        onClickListener { _ ->
          suggestionsClicksRelay.accept(suggestion)
        }
        onLongClickListener { _ ->
          suggestionLongClickRelay.accept(suggestion)
          return@onLongClickListener true
        }
      }
    }
  }
}
