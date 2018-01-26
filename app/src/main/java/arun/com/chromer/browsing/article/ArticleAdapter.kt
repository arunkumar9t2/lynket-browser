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

package arun.com.chromer.browsing.article

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.browsing.article.util.ArticleUtil.changeTextSelectionHandleColors
import arun.com.chromer.browsing.article.util.SuppressiveLinkMovementMethod
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.util.HtmlCompat
import arun.com.chromer.util.glide.GlideApp
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf
import com.bumptech.glide.request.RequestOptions.placeholderOf
import org.jsoup.select.Elements
import timber.log.Timber

/**
 * Recycler adapter responsible for displaying the article in a recycler view. This will
 * implement a variety of different view types including:
 *
 *
 * 1. Header Image
 * 2. Title
 * 3. Author
 * 4. Paragraph
 * 5. Inline Image
 * 6. Paragraph Headers
 * 7. Block quotes
 */
internal class ArticleAdapter(private val article: WebArticle?, private val accentColor: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var elements: Elements? = null
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    private var requestManager: RequestManager? = null

    private val topItemCount: Int
        get() {
            // header image always present at top, even when one isn't available.
            var count = 0
            if (article != null) {
                count += 1
                if (!TextUtils.isEmpty(article.title) || !TextUtils.isEmpty(article.siteName)) {
                    count += 1
                }
            }
            return count
        }

    private fun initRequestManager(context: Context) {
        requestManager = GlideApp.with(context)
        val resources = context.resources
        imageWidth = resources.getDimensionPixelSize(R.dimen.article_articleWidth)
        if (imageWidth <= 0) {
            val display = (context as Activity).windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            imageWidth = size.x
        }
        imageHeight = resources.getDimensionPixelSize(R.dimen.article_imageParallax) + resources.getDimensionPixelSize(R.dimen.article_imageHeight)
    }

    fun addElements(elements: Elements) {
        this.elements = elements
        notifyItemRangeInserted(topItemCount, elements.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(getItemResourceFromType(viewType), parent, false)
        return when (viewType) {
            TYPE_HEADER_IMAGE -> HeaderImageViewHolder(view)
            TYPE_TITLE -> TitleTextViewHolder(view, accentColor)
            TYPE_INLINE_IMAGE -> ImageViewHolder(view)
            TYPE_BLOCKQUOTE -> BlockQuoteViewHolder(view, accentColor)
            TYPE_HEADER_1, TYPE_HEADER_2, TYPE_HEADER_3, TYPE_HEADER_4, TYPE_HEADER_5, TYPE_HEADER_6 -> SubtitleTextViewHolder(view, accentColor)
            TYPE_UNORDERED_LIST_ITEM, TYPE_ORDERED_LIST_ITEM, TYPE_PARAGRAPH, TYPE_PRE -> TextViewHolder(view, accentColor)
            else -> TextViewHolder(view, accentColor)
        }
    }

    @VisibleForTesting
    private fun getItemResourceFromType(viewType: Int): Int {
        when (viewType) {
            TYPE_HEADER_IMAGE -> return R.layout.layout_article_item_header
            TYPE_TITLE -> return R.layout.layout_article_item_title
            TYPE_PARAGRAPH -> return R.layout.layout_article_item_paragraph
            TYPE_INLINE_IMAGE -> return R.layout.layout_article_item_image
            TYPE_HEADER_1 -> return R.layout.layout_article_item_header_1
            TYPE_HEADER_2 -> return R.layout.layout_article_item_header_2
            TYPE_HEADER_3 -> return R.layout.layout_article_item_header_3
            TYPE_HEADER_4 -> return R.layout.layout_article_item_header_4
            TYPE_HEADER_5 -> return R.layout.layout_article_item_header_5
            TYPE_HEADER_6 -> return R.layout.layout_article_item_header_6
            TYPE_BLOCKQUOTE -> return R.layout.layout_article_item_blockquote
            TYPE_PRE -> return R.layout.layout_article_item_pre
            TYPE_UNORDERED_LIST_ITEM -> return R.layout.layout_article_item_unordered_list_item
            TYPE_ORDERED_LIST_ITEM -> return R.layout.layout_article_item_ordered_list_item
            else -> return R.layout.layout_article_item_other
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val topItemCount = topItemCount
        if (position >= topItemCount) {
            if (holder is ImageViewHolder) {
                val imageSourceUrl = ""
                val image = holder.image
                if (imageSourceUrl.startsWith("data:")) {
                    // bad image data from the server, it didn't give us a url
                    image.gone()
                    return
                }
                image.show()
                val params = image.layoutParams as ViewGroup.MarginLayoutParams
                if (position - topItemCount - 1 >= 0 && elements!![position - topItemCount - 1].tagName() != "img") {
                    params.topMargin = image.context.resources
                            .getDimensionPixelSize(R.dimen.article_extraImagePadding)
                } else {
                    params.topMargin = 0
                }
                if (position != itemCount - 1 && elements!![position - topItemCount + 1].tagName() != "img") {
                    params.bottomMargin = image.context.resources
                            .getDimensionPixelSize(R.dimen.article_extraImagePadding)
                } else {
                    params.bottomMargin = 0
                }

                if (requestManager == null) {
                    initRequestManager(image.context)
                }
                Timber.v("loading url at %s", imageSourceUrl)
                holder.url = imageSourceUrl
                GlideApp.with(image.context)
                        .load(imageSourceUrl)
                        .apply(placeholderOf(R.color.article_imageBackground))
                        .apply(diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(image)
            } else if (holder is TextViewHolder) {
                val text = elements!![position - topItemCount].outerHtml()
                val textView = holder.textView

                val params = textView.layoutParams as ViewGroup.MarginLayoutParams
                if (position == itemCount - 1) {
                    params.bottomMargin = textView.context.resources
                            .getDimensionPixelSize(R.dimen.article_extraBottomPadding)
                } else {
                    params.bottomMargin = 0
                }
                textView.text = HtmlCompat.fromHtml(text)
            }
        } else {
            if (holder is HeaderImageViewHolder) {
                val image = holder.image
                if (requestManager == null) {
                    initRequestManager(image.context)
                }
                if (!TextUtils.isEmpty(article!!.imageUrl)) {
                    holder.url = article.imageUrl
                    GlideApp.with(image.context)
                            .load(article.imageUrl)
                            .apply(placeholderOf(R.color.article_imageBackground))
                            .apply(diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .into(image)
                }
            } else if (holder is TitleTextViewHolder) {
                holder.textView.text = article!!.title
                if (TextUtils.isEmpty(article.title)) {
                    holder.author.gone()
                } else {
                    holder.author.text = article.title
                }
                if (TextUtils.isEmpty(article.siteName)) {
                    holder.source.gone()
                } else {
                    holder.source.text = article.siteName
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val topItemCount = topItemCount
        if (position >= topItemCount) {
            val element = elements!![position - topItemCount]
            var tag = element.tagName()
            if (tag == "li") {
                tag = element.parent().tagName() + "." + tag
            }
            return getItemTypeForTag(tag)
        } else {
            return if (position == 0) {
                TYPE_HEADER_IMAGE
            } else {
                TYPE_TITLE
            }
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        count += topItemCount
        if (elements != null) {
            count += elements!!.size
        }
        return count
    }

    private fun getItemTypeForTag(tag: String): Int {
        return when (tag) {
            "p" -> TYPE_PARAGRAPH
            "h1" -> TYPE_HEADER_1
            "h2" -> TYPE_HEADER_2
            "h3" -> TYPE_HEADER_3
            "h4" -> TYPE_HEADER_4
            "h5" -> TYPE_HEADER_5
            "h6" -> TYPE_HEADER_6
            "img" -> TYPE_INLINE_IMAGE
            "blockquote" -> TYPE_BLOCKQUOTE
            "pre" -> TYPE_PRE
            "ul.li" -> TYPE_UNORDERED_LIST_ITEM
            "ol.li" -> TYPE_ORDERED_LIST_ITEM
            else -> TYPE_OTHER
        }
    }

    internal open inner class TextViewHolder(itemView: View, accentColor: Int) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.article_text)

        init {
            this.textView.movementMethod = SuppressiveLinkMovementMethod()
            changeTextSelectionHandleColors(this.textView, accentColor)
        }
    }

    internal inner class BlockQuoteViewHolder(itemView: View, accentColor: Int) : TextViewHolder(itemView, accentColor) {
        init {
            textView.setTextColor(accentColor)
        }
    }

    internal open inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.article_image)
        var url: String? = null

        init {
            this.image.setOnClickListener {
                if (!TextUtils.isEmpty(url)) {
                    val intent = Intent(image.context, ImageViewActivity::class.java)
                    intent.data = Uri.parse(url)
                    image.context.startActivity(intent)
                }
            }
        }
    }

    internal inner class HeaderImageViewHolder(itemView: View) : ImageViewHolder(itemView)

    internal inner class TitleTextViewHolder(itemView: View, accentColor: Int) : TextViewHolder(itemView, accentColor) {
        val author: TextView = itemView.findViewById(R.id.article_author)
        val source: TextView = itemView.findViewById(R.id.article_source)
    }

    internal inner class SubtitleTextViewHolder(itemView: View, accentColor: Int) : TextViewHolder(itemView, accentColor)

    companion object {
        @VisibleForTesting
        val TYPE_HEADER_IMAGE = 1
        @VisibleForTesting
        val TYPE_TITLE = 2
        @VisibleForTesting
        val TYPE_PARAGRAPH = 3
        @VisibleForTesting
        val TYPE_INLINE_IMAGE = 4
        @VisibleForTesting
        val TYPE_HEADER_1 = 5
        @VisibleForTesting
        val TYPE_HEADER_2 = 6
        @VisibleForTesting
        val TYPE_HEADER_3 = 7
        @VisibleForTesting
        val TYPE_HEADER_4 = 8
        @VisibleForTesting
        val TYPE_HEADER_5 = 9
        @VisibleForTesting
        val TYPE_HEADER_6 = 10
        @VisibleForTesting
        val TYPE_BLOCKQUOTE = 11
        @VisibleForTesting
        val TYPE_PRE = 12
        @VisibleForTesting
        val TYPE_UNORDERED_LIST_ITEM = 13
        @VisibleForTesting
        val TYPE_ORDERED_LIST_ITEM = 14
        @VisibleForTesting
        val TYPE_OTHER = 15
    }
}
