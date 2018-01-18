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

package arun.com.chromer.browsing.article;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arun.com.chromer.R;
import arun.com.chromer.data.webarticle.model.WebArticle;
import arun.com.chromer.util.HtmlCompat;

/**
 * Recycler adapter responsible for displaying the article in a recycler view. This will
 * implement a variety of different view types including:
 * <p>
 * 1. Header Image
 * 2. Title
 * 3. Author
 * 4. Paragraph
 * 5. Inline Image
 * 6. Paragraph Headers
 * 7. Block quotes
 */
@SuppressWarnings("WeakerAccess")
final class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @VisibleForTesting
    static final int TYPE_HEADER_IMAGE = 1;
    @VisibleForTesting
    static final int TYPE_TITLE = 2;
    @VisibleForTesting
    static final int TYPE_PARAGRAPH = 3;
    @VisibleForTesting
    static final int TYPE_INLINE_IMAGE = 4;
    @VisibleForTesting
    static final int TYPE_HEADER_1 = 5;
    @VisibleForTesting
    static final int TYPE_HEADER_2 = 6;
    @VisibleForTesting
    static final int TYPE_HEADER_3 = 7;
    @VisibleForTesting
    static final int TYPE_HEADER_4 = 8;
    @VisibleForTesting
    static final int TYPE_HEADER_5 = 9;
    @VisibleForTesting
    static final int TYPE_HEADER_6 = 10;
    @VisibleForTesting
    static final int TYPE_BLOCKQUOTE = 11;
    @VisibleForTesting
    static final int TYPE_PRE = 12;
    @VisibleForTesting
    static final int TYPE_UNORDERED_LIST_ITEM = 13;
    @VisibleForTesting
    static final int TYPE_ORDERED_LIST_ITEM = 14;
    @VisibleForTesting
    static final int TYPE_OTHER = 15;
    private static final int MIN_IMAGE_WIDTH = 200; // px
    private static final int MIN_IMAGE_HEIGHT = 100; // px

    private final WebArticle article;
    private Elements elements;
    private final int accentColor;
    private int imageWidth;
    private int imageHeight;

    private RequestManager requestManager;

    ArticleAdapter(WebArticle article, int accentColor) {
        this.article = article;
        this.accentColor = accentColor;
    }

    private void initRequestManager(Context context) {
        final Resources resources = context.getResources();
        imageWidth = resources.getDimensionPixelSize(R.dimen.article_articleWidth);
        if (imageWidth <= 0) {
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            imageWidth = size.x;
        }
        imageHeight = resources.getDimensionPixelSize(R.dimen.article_imageParallax) +
                resources.getDimensionPixelSize(R.dimen.article_imageHeight);
    }

    void addElements(Elements elements) {
        this.elements = elements;
        notifyItemRangeInserted(getTopItemCount(), elements.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final View view = LayoutInflater.from(context).inflate(getItemResourceFromType(viewType), parent, false);
        switch (viewType) {
            case TYPE_HEADER_IMAGE:
                return new HeaderImageViewHolder(view);
            case TYPE_TITLE:
                return new TitleTextViewHolder(view, accentColor);
            case TYPE_INLINE_IMAGE:
                return new ImageViewHolder(view);
            case TYPE_BLOCKQUOTE:
                return new BlockQuoteViewHolder(view, accentColor);
            case TYPE_HEADER_1:
            case TYPE_HEADER_2:
            case TYPE_HEADER_3:
            case TYPE_HEADER_4:
            case TYPE_HEADER_5:
            case TYPE_HEADER_6:
                return new SubtitleTextViewHolder(view, accentColor);
            case TYPE_UNORDERED_LIST_ITEM:
            case TYPE_ORDERED_LIST_ITEM:
            case TYPE_PARAGRAPH:
            case TYPE_PRE:
            default:
                return new TextViewHolder(view, accentColor);
        }
    }

    @VisibleForTesting
    int getItemResourceFromType(int viewType) {
        switch (viewType) {
            case TYPE_HEADER_IMAGE:
                return R.layout.article_item_header;
            case TYPE_TITLE:
                return R.layout.article_item_title;
            case TYPE_PARAGRAPH:
                return R.layout.article_item_paragraph;
            case TYPE_INLINE_IMAGE:
                return R.layout.article_item_image;
            case TYPE_HEADER_1:
                return R.layout.article_item_header_1;
            case TYPE_HEADER_2:
                return R.layout.article_item_header_2;
            case TYPE_HEADER_3:
                return R.layout.article_item_header_3;
            case TYPE_HEADER_4:
                return R.layout.article_item_header_4;
            case TYPE_HEADER_5:
                return R.layout.article_item_header_5;
            case TYPE_HEADER_6:
                return R.layout.article_item_header_6;
            case TYPE_BLOCKQUOTE:
                return R.layout.article_item_blockquote;
            case TYPE_PRE:
                return R.layout.article_item_pre;
            case TYPE_UNORDERED_LIST_ITEM:
                return R.layout.article_item_unordered_list_item;
            case TYPE_ORDERED_LIST_ITEM:
                return R.layout.article_item_ordered_list_item;
            default:
                return R.layout.article_item_other;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int topItemCount = getTopItemCount();
        if (position >= topItemCount) {
            if (holder instanceof ImageViewHolder) {
                String src = "";
                final ImageView image = ((ImageViewHolder) holder).image;
                if (src.startsWith("data:")) {
                    // bad image data from the server, it didn't give us a url
                    image.setVisibility(View.GONE);
                    return;
                }
                image.setVisibility(View.VISIBLE);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) image.getLayoutParams();
                if (position - topItemCount - 1 >= 0 &&
                        !elements.get(position - topItemCount - 1).tagName().equals("img")) {
                    params.topMargin = image.getContext().getResources()
                            .getDimensionPixelSize(R.dimen.article_extraImagePadding);
                } else {
                    params.topMargin = 0;
                }
                if (position != getItemCount() - 1 &&
                        !elements.get(position - topItemCount + 1).tagName().equals("img")) {
                    params.bottomMargin = image.getContext().getResources()
                            .getDimensionPixelSize(R.dimen.article_extraImagePadding);
                } else {
                    params.bottomMargin = 0;
                }
                requestManager = Glide.with(image.getContext());

                if (requestManager == null) {
                    initRequestManager(image.getContext());
                }
                Log.v("ArticleAdapter", "loading url at " + src);
                ((ImageViewHolder) holder).url = src;
                Glide.with(image.getContext())
                        .load(src)
                        .apply(RequestOptions.placeholderOf(R.color.article_imageBackground))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(image);

            } else if (holder instanceof TextViewHolder) {
                final TextViewHolder tvHolder = (TextViewHolder) holder;
                final String text = elements.get(position - topItemCount).outerHtml();
                final TextView textView = tvHolder.textView;

                final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
                if (position == getItemCount() - 1) {
                    params.bottomMargin = textView.getContext().getResources()
                            .getDimensionPixelSize(R.dimen.article_extraBottomPadding);
                } else {
                    params.bottomMargin = 0;
                }
                textView.setText(HtmlCompat.fromHtml(text));
            }
        } else {
            if (holder instanceof HeaderImageViewHolder) {
                ImageView image = ((HeaderImageViewHolder) holder).image;
                if (requestManager == null) {
                    initRequestManager(image.getContext());
                }
                if (!TextUtils.isEmpty(article.imageUrl)) {
                    ((HeaderImageViewHolder) holder).url = article.imageUrl;
                    Glide.with(image.getContext())
                            .load(article.imageUrl)
                            .apply(RequestOptions.placeholderOf(R.color.article_imageBackground))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .into(image);
                }
            } else if (holder instanceof TitleTextViewHolder) {
                ((TitleTextViewHolder) holder).textView.setText(article.title);
                if (TextUtils.isEmpty(article.title)) {
                    ((TitleTextViewHolder) holder).author.setVisibility(View.GONE);
                } else {
                    ((TitleTextViewHolder) holder).author.setText(article.title);
                }
                if (TextUtils.isEmpty(article.siteName)) {
                    ((TitleTextViewHolder) holder).source.setVisibility(View.GONE);
                } else {
                    ((TitleTextViewHolder) holder).source.setText(article.siteName);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        int topItemCount = getTopItemCount();
        if (position >= topItemCount) {
            Element element = elements.get(position - topItemCount);
            String tag = element.tagName();
            if (tag.equals("li")) {
                tag = element.parent().tagName() + "." + tag;
            }

            return getItemTypeForTag(tag);
        } else {
            if (position == 0) {
                return TYPE_HEADER_IMAGE;
            } else {
                return TYPE_TITLE;
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        count += getTopItemCount();
        if (elements != null) {
            count += elements.size();
        }
        return count;
    }

    private int getTopItemCount() {
        int count = 0;
        if (article != null) {
            count += 1; // header image always present at top, even when one isn't available.
            if (!TextUtils.isEmpty(article.title) || !TextUtils.isEmpty(article.siteName)) {
                count += 1;
            }
        }
        return count;
    }

    @VisibleForTesting
    int getItemTypeForTag(String tag) {
        switch (tag) {
            case "p":
                return TYPE_PARAGRAPH;
            case "h1":
                return TYPE_HEADER_1;
            case "h2":
                return TYPE_HEADER_2;
            case "h3":
                return TYPE_HEADER_3;
            case "h4":
                return TYPE_HEADER_4;
            case "h5":
                return TYPE_HEADER_5;
            case "h6":
                return TYPE_HEADER_6;
            case "img":
                return TYPE_INLINE_IMAGE;
            case "blockquote":
                return TYPE_BLOCKQUOTE;
            case "pre":
                return TYPE_PRE;
            case "ul.li":
                return TYPE_UNORDERED_LIST_ITEM;
            case "ol.li":
                return TYPE_ORDERED_LIST_ITEM;
            default:
                return TYPE_OTHER;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @VisibleForTesting
    class TextViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;

        private TextViewHolder(View itemView, int accentColor) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.article_text);
            this.textView.setTextIsSelectable(true);
            this.textView.setMovementMethod(LinkMovementMethod.getInstance());
            ArticleUtil.changeTextSelectionHandleColors(this.textView, accentColor);
        }
    }

    @VisibleForTesting
    class BlockQuoteViewHolder extends TextViewHolder {
        private BlockQuoteViewHolder(View itemView, int accentColor) {
            super(itemView, accentColor);
            textView.setTextColor(accentColor);
        }
    }

    @VisibleForTesting
    class ImageViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;
        public String url;

        private ImageViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.article_image);
            this.image.setOnClickListener(view -> {
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(image.getContext(), ImageViewActivity.class);
                    intent.setData(Uri.parse(url));
                    image.getContext().startActivity(intent);
                }
            });
        }
    }

    @VisibleForTesting
    class HeaderImageViewHolder extends ImageViewHolder {
        private HeaderImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    @VisibleForTesting
    class TitleTextViewHolder extends TextViewHolder {
        public final TextView author;
        public final TextView source;

        private TitleTextViewHolder(View itemView, int accentColor) {
            super(itemView, accentColor);
            author = itemView.findViewById(R.id.article_author);
            source = itemView.findViewById(R.id.article_source);
        }
    }

    @VisibleForTesting
    class SubtitleTextViewHolder extends TextViewHolder {
        private SubtitleTextViewHolder(View itemView, int accentColor) {
            super(itemView, accentColor);
        }
    }
}
