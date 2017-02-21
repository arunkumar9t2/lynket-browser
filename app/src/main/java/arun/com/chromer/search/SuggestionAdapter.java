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

/**
 * Created by Arun on 03/08/2016.
 */
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionItemHolder> {
    private final Context context;

    private SuggestionClickListener mCallback = new SuggestionClickListener() {
        @Override
        public void onSuggestionClicked(@NonNull String suggestion) {
            // no op
        }
    };
    @NonNull
    private final List<SuggestionItem> suggestionItems = new ArrayList<>();

    private static Drawable searchIcon;

    public SuggestionAdapter(@NonNull final Context context, @Nullable SuggestionClickListener listener) {
        this.context = context.getApplicationContext();
        setHasStableIds(true);
        mCallback = listener;

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
        holder.suggestion.setText(suggestionItems.get(position).suggestion);
        SuggestionItem item = suggestionItems.get(position);
        switch (item.type) {
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
        //Benchmark.start("Diff Calculation");
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(suggestionDiff, true);
        // Benchmark.end();

        suggestionItems.clear();
        suggestionItems.addAll(newSuggestions);

        diffResult.dispatchUpdatesTo(this);
    }

    public void clear() {
        suggestionItems.clear();
        notifyDataSetChanged();
    }

    class SuggestionItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.suggestions_text)
        TextView suggestion;
        @BindView(R.id.suggestion_icon)
        ImageView icon;

        SuggestionItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mCallback.onSuggestionClicked(suggestionItems.get(position).suggestion);
                    }
                }
            });
        }
    }

    public interface SuggestionClickListener {
        void onSuggestionClicked(@NonNull final String suggestion);
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
