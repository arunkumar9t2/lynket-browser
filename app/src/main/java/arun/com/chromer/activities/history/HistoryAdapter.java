package arun.com.chromer.activities.history;

import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import arun.com.chromer.R;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.nekocode.badge.BadgeDrawable;
import timber.log.Timber;

/**
 * Created by Arunkumar on 06-03-2017.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final AsyncListUtil<WebSite> asyncWebsiteList;
    private Cursor cursor = null;
    private final LinearLayoutManager linearLayoutManager;

    private final AsyncListUtil.DataCallback<WebSite> dataCallback = new AsyncListUtil.DataCallback<WebSite>() {
        @Override
        public int refreshData() {
            Timber.d("Refresh data");
            if (cursor == null) {
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
        if (webSite == null) {
            holder.historyTitle.setText(R.string.loading);
            holder.historySubtitle.setVisibility(View.GONE);
            holder.historyFavicon.setImageDrawable(null);
            Glide.clear(holder.historyFavicon);
        } else {
            holder.historyTitle.setText(webSite.safeLabel());
            holder.historySubtitle.setVisibility(View.VISIBLE);
            holder.historySubtitle.setText(webSite.preferredUrl());
            if (!TextUtils.isEmpty(webSite.faviconUrl)) {
                Glide.with(holder.itemView.getContext())
                        .load(webSite.faviconUrl)
                        .crossFade()
                        .into(holder.historyFavicon);
            } else {
                final String letter = Utils.getFirstLetter(webSite.safeLabel()).toUpperCase();
                final boolean themeExists = webSite.themeColor() != -1;
                final int bgColor = themeExists ? webSite.themeColor() : ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary);
                final BadgeDrawable drawable = new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                        .badgeColor(bgColor)
                        .textColor(themeExists ? ColorUtil.getClosestAccentColor(bgColor) : Color.WHITE)
                        .text1(" " + letter + " ")
                        .textSize(Utils.dpToPx(24))
                        .build();
                holder.historyFavicon.setImageDrawable(drawable);
            }
        }
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

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.history_title)
        TextView historyTitle;
        @BindView(R.id.history_favicon)
        ImageView historyFavicon;
        @BindView(R.id.history_subtitle)
        TextView historySubtitle;

        HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}