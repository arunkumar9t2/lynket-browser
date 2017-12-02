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

package arun.com.chromer.search;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.view.ViewCompat.setTransitionName;

/**
 * Created by Arun on 03/08/2016.
 */
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionItemHolder> {
    private static Drawable searchIcon;
    private final Context context;
    @NonNull
    private final List<SuggestionItem> suggestionItems = new ArrayList<>();
    private SuggestionClickListener callBack = suggestion -> {
        // no op
    };

    public SuggestionAdapter(@NonNull final Context context, @Nullable SuggestionClickListener listener) {
        this.context = context.getApplicationContext();
        setHasStableIds(true);
        callBack = listener;
        if (searchIcon == null)
            searchIcon = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_magnify)
                    .color(ContextCompat.getColor(context, R.color.material_dark_light))
                    .sizeDp(18);
    }

    @Override
    public SuggestionItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SuggestionItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_suggestions_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(SuggestionItemHolder holder, int position) {
        final SuggestionItem suggestionItem = suggestionItems.get(position);
        holder.suggestion.setText(suggestionItem.suggestion);
        setTransitionName(holder.suggestion, suggestionItem.type + suggestionItem.suggestion);
        switch (suggestionItem.type) {
            case SuggestionItem.COPY:
                holder.icon.setImageDrawable(new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_content_copy)
                        .color(ContextCompat.getColor(context, R.color.md_cyan_600))
                        .sizeDp(18));
                break;
            case SuggestionItem.GOOGLE:
                holder.icon.setImageDrawable(searchIcon);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return suggestionItems.size();
    }

    @Override
    public long getItemId(int position) {
        return suggestionItems.get(position).hashCode();
    }

    public void updateSuggestions(@NonNull List<SuggestionItem> newSuggestions) {
        final SuggestionDiff suggestionDiff = new SuggestionDiff(suggestionItems, newSuggestions);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(suggestionDiff, true);
        suggestionItems.clear();
        suggestionItems.addAll(newSuggestions);
        diffResult.dispatchUpdatesTo(this);
    }

    public void clear() {
        suggestionItems.clear();
        notifyDataSetChanged();
    }

    public interface SuggestionClickListener {
        void onSuggestionClicked(@NonNull final String suggestion);
    }

    class SuggestionItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.suggestions_text)
        TextView suggestion;
        @BindView(R.id.suggestion_icon)
        ImageView icon;

        SuggestionItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(view1 -> {
                final int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    callBack.onSuggestionClicked(suggestionItems.get(position).suggestion);
                }
            });
        }
    }

    private class SuggestionDiff extends DiffUtil.Callback {

        private final List<SuggestionItem> newList;
        private final List<SuggestionItem> oldList;

        SuggestionDiff(@NonNull List<SuggestionItem> oldList, @NonNull List<SuggestionItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).suggestion.
                    equalsIgnoreCase(newList.get(newItemPosition).suggestion);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).suggestion.
                    equalsIgnoreCase(newList.get(newItemPosition).suggestion);
        }
    }
}
