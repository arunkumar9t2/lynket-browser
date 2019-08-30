package arun.com.chromer.search.suggestion.model

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import arun.com.chromer.R
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.search.suggestion.items.COPY
import arun.com.chromer.search.suggestion.items.GOOGLE
import arun.com.chromer.search.suggestion.items.HISTORY
import arun.com.chromer.search.suggestion.items.SuggestionItem
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.widget_suggestions_item_template.*

@EpoxyModelClass(layout = R.layout.widget_suggestions_item_template)
abstract class SuggestionLayoutModel : KotlinEpoxyModelWithHolder<SuggestionLayoutModel.ViewHolder>() {
    @EpoxyAttribute
    lateinit var suggestionItem: SuggestionItem
    @EpoxyAttribute(DoNotHash)
    lateinit var copyIcon: Drawable
    @EpoxyAttribute(DoNotHash)
    lateinit var historyIcon: Drawable
    @EpoxyAttribute(DoNotHash)
    lateinit var searchIcon: Drawable
    @EpoxyAttribute(DoNotHash)
    lateinit var onClickListener: View.OnClickListener

    override fun bind(holder: ViewHolder) {
        super.bind(holder)
        holder.apply {
            suggestionsText.text = suggestionItem.title
            when (suggestionItem.type) {
                COPY -> suggestionIcon.setImageDrawable(copyIcon)
                HISTORY -> suggestionIcon.setImageDrawable(historyIcon)
                GOOGLE -> suggestionIcon.setImageDrawable(searchIcon)
            }
            when {
                TextUtils.isEmpty(suggestionItem.subTitle) -> {
                    suggestionsSubTitle.gone()
                    suggestionsSubTitle.text = null
                }
                else -> {
                    suggestionsSubTitle.show()
                    suggestionsSubTitle.text = suggestionItem.subTitle
                }
            }
            containerView.setOnClickListener(onClickListener)
        }
    }

    class ViewHolder : KotlinHolder()
}