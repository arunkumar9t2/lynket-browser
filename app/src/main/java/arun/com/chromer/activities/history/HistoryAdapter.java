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

package arun.com.chromer.activities.history;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.AsyncListUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.Utils;
import arun.com.chromer.views.PlaceholderLetterView;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_OUR_APP;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_SKIP_EXTRACTION;

/**
 * Created by Arunkumar on 06-03-2017.
 */
class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final AsyncListUtil<WebSite> asyncWebsiteList;
    private Cursor cursor = null;
    private final LinearLayoutManager linearLayoutManager;

    private final AsyncListUtil.DataCallback<WebSite> dataCallback = new AsyncListUtil.DataCallback<WebSite>() {
        @Override
        public int refreshData() {
            Timber.d("Refresh data");
            if (cursor == null || cursor.isClosed()) {
                return 0;
            }
            return cursor.getCount();
        }

        @Override
        public void fillData(WebSite[] data, int startPosition, int itemCount) {
            if (cursor != null) {
                for (int i = 0; i < itemCount; i++) {
                    cursor.moveToPosition(startPosition + i);
                    data[i] = WebSite.fromCursor(cursor);
                }
            }
        }
    };

    private final AsyncListUtil.ViewCallback viewCallback = new AsyncListUtil.ViewCallback() {
        @Override
        public void getItemRangeInto(int[] outRange) {
            outRange[0] = linearLayoutManager.findFirstVisibleItemPosition();
            outRange[1] = linearLayoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onDataRefresh() {
            Timber.d("onDataRefresh");
            notifyDataSetChanged();
        }

        @Override
        public void onItemLoaded(int position) {
            Timber.d("onItemLoaded, position %d", position);
            notifyItemChanged(position);
        }
    };

    HistoryAdapter(@NonNull LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
        asyncWebsiteList = new AsyncListUtil<>(WebSite.class, 50, dataCallback, viewCallback);
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_history_list_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        final WebSite webSite = asyncWebsiteList.getItem(position);
        holder.bind(webSite);
    }

    @Override
    public void onViewRecycled(HistoryViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.clear(holder.historyFavicon);
    }

    @Override
    public int getItemCount() {
        return asyncWebsiteList.getItemCount();
    }

    @Nullable
    WebSite getItemAt(final int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
            return WebSite.fromCursor(cursor);
        }
        return null;
    }

    @NonNull
    Observable<WebSite> getWebsiteAt(final int position) {
        return Observable.fromCallable(() -> getItemAt(position));
    }

    void setCursor(@Nullable Cursor cursor) {
        if (this.cursor != null) {
            try {
                this.cursor.close();
            } catch (Exception ignored) {
            }
        }
        this.cursor = cursor;
        refresh();
    }

    void onRangeChanged() {
        asyncWebsiteList.onRangeChanged();
    }

    void refresh() {
        asyncWebsiteList.refresh();
    }

    void cleanUp() {
        if (cursor != null) {
            cursor.close();
        }
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.history_title)
        TextView historyTitle;
        @BindView(R.id.history_favicon)
        ImageView historyFavicon;
        @BindView(R.id.history_subtitle)
        TextView historySubtitle;
        @BindView(R.id.history_amp)
        ImageView historyAmp;
        @BindView(R.id.history_placeholder)
        PlaceholderLetterView historyPlaceholder;


        HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                final WebSite webSite = asyncWebsiteList.getItem(position);
                if (webSite != null && position != RecyclerView.NO_POSITION) {
                    final Intent intent = new Intent(itemView.getContext(), BrowserInterceptActivity.class);
                    intent.setData(Uri.parse(webSite.preferredUrl()));
                    intent.putExtra(EXTRA_KEY_FROM_OUR_APP, true);
                    itemView.getContext().startActivity(intent);
                }
            });

            historyAmp.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                final WebSite webSite = asyncWebsiteList.getItem(position);
                if (webSite != null && position != RecyclerView.NO_POSITION) {
                    final Intent intent = new Intent(itemView.getContext(), BrowserInterceptActivity.class);
                    intent.setData(Uri.parse(webSite.ampUrl));
                    intent.putExtra(EXTRA_KEY_FROM_OUR_APP, true);
                    intent.putExtra(EXTRA_KEY_SKIP_EXTRACTION, true);
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        public void bind(@Nullable WebSite webSite) {
            if (webSite == null) {
                historyTitle.setText(R.string.loading);
                historySubtitle.setText(R.string.loading);
                historyFavicon.setImageDrawable(null);
                historyAmp.setVisibility(GONE);
                Glide.clear(historyFavicon);
            } else {
                historyTitle.setText(webSite.safeLabel());
                historySubtitle.setText(webSite.preferredUrl());
                if (!TextUtils.isEmpty(webSite.faviconUrl)) {
                    Glide.with(itemView.getContext())
                            .load(webSite.faviconUrl)
                            .asBitmap()
                            .into(new BitmapImageViewTarget(historyFavicon) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    if (Utils.isValidFavicon(resource)) {
                                        showFavicon(resource);
                                    } else {
                                        showPlaceholder(webSite.safeLabel());
                                    }
                                }
                            });
                } else {
                    Glide.clear(historyFavicon);
                    showPlaceholder(webSite.safeLabel());
                }
                if (!TextUtils.isEmpty(webSite.ampUrl)) {
                    historyAmp.setVisibility(VISIBLE);
                } else {
                    historyAmp.setVisibility(GONE);
                }
            }
        }

        private void showPlaceholder(@NonNull String url) {
            historyFavicon.setImageDrawable(null);
            historyFavicon.setVisibility(GONE);
            historyPlaceholder.setVisibility(VISIBLE);
            historyPlaceholder.setPlaceHolder(url);
        }

        private void showFavicon(Bitmap resource) {
            historyPlaceholder.setVisibility(GONE);
            historyFavicon.setVisibility(VISIBLE);
            historyFavicon.setImageBitmap(resource);
        }
    }


}