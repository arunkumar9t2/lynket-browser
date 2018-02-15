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

package arun.com.chromer.browsing.article.adapter

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import arun.com.chromer.R
import arun.com.chromer.browsing.article.ImageViewActivity
import arun.com.chromer.browsing.article.util.ArticleUtil.changeTextSelectionHandleColors
import arun.com.chromer.browsing.article.util.SuppressiveLinkMovementMethod
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.extenstions.gone
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.HtmlCompat
import arun.com.chromer.util.Utils
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf
import com.bumptech.glide.request.RequestOptions.placeholderOf
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import rx.Observable
import rx.subjects.PublishSubject

/**
 * Recycler adapter responsible for displaying the article in a recycler view. This will
 * implement a variety of different view types including:
 *
 * 1. Header Image
 * 2. Title
 * 3. Author
 * 4. Paragraph
 * 5. Inline Image
 * 6. Paragraph Headers
 * 7. Block quotes
 */
internal class ArticleAdapter(
        private val article: WebArticle,
        private var accentColor: Int,
        private val requestManager: RequestManager,
        articleTextSizeIncrement: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var elements: Elements = Elements()

    private val keywordClicks = PublishSubject.create<String>()
    fun keywordsClicks(): Observable<String> = keywordClicks.asObservable()

    private val manualItemsOffset: Int
        get() {
            var offset = 0
            offset += 1 // header image always present at top, even when one isn't available.
            if (!TextUtils.isEmpty(article.title) || !TextUtils.isEmpty(article.siteName)) {
                offset += 1
            }
            if (article.keywords != null && article.keywords.isNotEmpty()) {
                offset += 1 // keywords
            }
            return offset
        }

    var textSizeIncrementSp: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        textSizeIncrementSp = articleTextSizeIncrement
    }
    
    fun setAccentColor(accentColor: Int) {
        this.accentColor = accentColor
        notifyDataSetChanged()
    }

    fun setElements(elements: Elements) {
        this.elements = elements
        notifyItemRangeInserted(manualItemsOffset, elements.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(getItemResourceFromType(viewType), parent, false)
        return when (viewType) {
            TYPE_HEADER_IMAGE -> HeaderImageViewHolder(view)
            TYPE_TITLE -> TitleTextViewHolder(view)
            TYPE_KEYWORDS -> KeywordsViewHolder(view)
            TYPE_INLINE_IMAGE -> ImageViewHolder(view)
            TYPE_BLOCKQUOTE -> BlockQuoteViewHolder(view)
            TYPE_HEADER_1, TYPE_HEADER_2, TYPE_HEADER_3, TYPE_HEADER_4, TYPE_HEADER_5, TYPE_HEADER_6 -> SubtitleTextViewHolder(view)
            TYPE_UNORDERED_LIST_ITEM, TYPE_ORDERED_LIST_ITEM, TYPE_PARAGRAPH, TYPE_PRE -> TextViewHolder(view)
            else -> TextViewHolder(view)
        }
    }

    private fun getItemResourceFromType(viewType: Int): Int {
        when (viewType) {
            TYPE_HEADER_IMAGE -> return R.layout.layout_article_item_header
            TYPE_TITLE -> return R.layout.layout_article_item_title
            TYPE_KEYWORDS -> return R.layout.layout_article_item_keywords
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
        val topItemCount = manualItemsOffset
        if (position >= topItemCount) {
            when (holder) {
                is ImageViewHolder -> {
                    /*val imageSourceUrl = ""
                    val image = holder.image
                    if (imageSourceUrl.startsWith("data:")) {
                        // bad image data from the server, it didn't give us a url
                        image.gone()
                        return
                    }
                    image.show()
                    Timber.v("loading url at %s", imageSourceUrl)
                    holder.url = imageSourceUrl
                    GlideApp.with(image.context)
                            .load(imageSourceUrl)
                            .apply(placeholderOf(R.color.article_imageBackground))
                            .apply(diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .into(image)*/
                }
                is TextViewHolder -> holder.bind(elements[position - topItemCount])
            }
        } else {
            when (holder) {
                is HeaderImageViewHolder -> {
                    val image = holder.image
                    if (!TextUtils.isEmpty(article.imageUrl)) {
                        holder.url = article.imageUrl
                        requestManager.load(article.imageUrl)
                                .apply(placeholderOf(R.color.article_imageBackground))
                                .apply(diskCacheStrategyOf(DiskCacheStrategy.ALL))
                                .into(image)
                    }
                }
                is TitleTextViewHolder -> {
                    holder.textView.text = article.title
                    if (TextUtils.isEmpty(article.title)) {
                        holder.title.gone()
                    } else {
                        holder.title.text = article.title
                    }
                    if (TextUtils.isEmpty(article.siteName)) {
                        holder.siteName.gone()
                    } else {
                        holder.siteName.text = article.siteName
                    }
                }
                is KeywordsViewHolder -> {
                    holder.bind(article.keywords as ArrayList<String>)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val topItemCount = manualItemsOffset
        return if (position >= topItemCount) {
            val element = elements[position - topItemCount]
            var tag = element.tagName()
            if (tag == "li") {
                tag = element.parent().tagName() + "." + tag
            }
            getItemTypeForTag(tag)
        } else {
            when (position) {
                0 -> TYPE_HEADER_IMAGE
                1 -> TYPE_TITLE
                else -> TYPE_KEYWORDS
            }
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        count += manualItemsOffset
        count += elements.size
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

    internal open inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.article_image)
        var url: String? = null

        init {
            this.image.setOnClickListener {
                if (!TextUtils.isEmpty(url)) {
                    val intent = Intent(image.context, ImageViewActivity::class.java)
                    intent.data = Uri.parse(url)
                    try {
                        image.context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(image.context, R.string.unsupported_link, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    internal inner class HeaderImageViewHolder(itemView: View) : ImageViewHolder(itemView)

    internal open inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.article_text)

        private var originalTextSize: Float = 0f

        internal var calculatedSize: Float = 0f
            get() = originalTextSize + textSizeIncrementSp

        init {
            textView.movementMethod = SuppressiveLinkMovementMethod
            changeTextSelectionHandleColors(textView, accentColor)
            textView.setLinkTextColor(accentColor)
            originalTextSize = Utils.pxTosp(itemView.context, textView.textSize)
        }

        open fun bind(element: Element) {
            val text = element.outerHtml()
            val params = textView.layoutParams as ViewGroup.MarginLayoutParams
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition == itemCount - 1) {
                params.bottomMargin = textView.context.resources.getDimensionPixelSize(R.dimen.article_extraBottomPadding)
            } else {
                params.bottomMargin = 0
            }
            textView.text = HtmlCompat.fromHtml(text)
            textView.textSize = calculatedSize
        }
    }

    internal inner class BlockQuoteViewHolder(itemView: View) : TextViewHolder(itemView) {
        init {
            textView.setTextColor(accentColor)
        }
    }

    internal inner class TitleTextViewHolder(itemView: View) : TextViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.articleTitle)
        val siteName: TextView = itemView.findViewById(R.id.articleSiteName)
        override fun bind(element: Element) {
            super.bind(element)
            title.textSize = calculatedSize
            siteName.textSize = calculatedSize
        }
    }

    /**
     * [RecyclerView.ViewHolder] for the entire keywords item containing a list and title.
     */
    internal inner class KeywordsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val keywordsListView: RecyclerView = itemView.findViewById(R.id.keywordsList)
        private val keywordsAdapter = KeywordsAdapter()

        init {
            keywordsListView.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = keywordsAdapter
            }
        }

        fun bind(keywords: ArrayList<String>) {
            keywordsAdapter.setKeywords(keywords)
        }

        /**
         * Keywords adapter
         */
        inner class KeywordsAdapter : RecyclerView.Adapter<KeywordsAdapter.KeywordsItemViewHolder>() {
            private var keywords = ArrayList<String>()

            fun setKeywords(keywords: ArrayList<String>) {
                this.keywords.clear()
                this.keywords.addAll(keywords)
                this.notifyDataSetChanged()
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordsItemViewHolder {
                return KeywordsItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_article_item_keywords_template, parent, false))
            }

            override fun getItemCount(): Int {
                return keywords.size
            }

            override fun onBindViewHolder(holder: KeywordsItemViewHolder, position: Int) {
                holder.bind(keywords[position])
            }

            /**
             * [RecyclerView.ViewHolder] for individual items
             */
            inner class KeywordsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private val keywordItem: TextView = itemView.findViewById(R.id.keywordsItem)

                private var originalTextSize: Float = 0f

                init {
                    keywordItem.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            keywordClicks.onNext(keywords[position])
                        }
                    }
                    originalTextSize = Utils.pxTosp(itemView.context, keywordItem.textSize)
                }

                fun bind(keyword: String) {
                    keywordItem.apply {
                        text = keyword
                        (background as GradientDrawable).setColor(accentColor)
                        setTextColor(ColorUtil.getForegroundWhiteOrBlack(accentColor))
                        textSize = originalTextSize + textSizeIncrementSp
                    }
                }
            }
        }
    }


    internal inner class SubtitleTextViewHolder(itemView: View) : TextViewHolder(itemView)

    companion object {
        const val TYPE_HEADER_IMAGE = 1
        const val TYPE_TITLE = 2
        const val TYPE_PARAGRAPH = 3
        const val TYPE_INLINE_IMAGE = 4
        const val TYPE_HEADER_1 = 5
        const val TYPE_HEADER_2 = 6
        const val TYPE_HEADER_3 = 7
        const val TYPE_HEADER_4 = 8
        const val TYPE_HEADER_5 = 9
        const val TYPE_HEADER_6 = 10
        const val TYPE_BLOCKQUOTE = 11
        const val TYPE_PRE = 12
        const val TYPE_UNORDERED_LIST_ITEM = 13
        const val TYPE_ORDERED_LIST_ITEM = 14
        const val TYPE_OTHER = 15
        const val TYPE_KEYWORDS = 16
    }

}
