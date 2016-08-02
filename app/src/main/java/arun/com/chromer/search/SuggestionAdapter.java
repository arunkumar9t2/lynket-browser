package arun.com.chromer.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arun on 03/08/2016.
 */

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionItemHolder> {
    private final Context mContext;

    private SuggestionClickListener mCallback = new SuggestionClickListener() {
        @Override
        public void onSuggestionClicked(@NonNull String suggestion) {
            // no op
        }
    };
    @NonNull
    private List<String> mSuggestions = new ArrayList<>();

    public SuggestionAdapter(@NonNull final Context context, @Nullable SuggestionClickListener listener) {
        mContext = context.getApplicationContext();
        setHasStableIds(true);
        mCallback = listener;
    }

    @Override
    public SuggestionItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SuggestionItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(SuggestionItemHolder holder, int position) {
        holder.suggestion.setText(mSuggestions.get(position));
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    @Override
    public long getItemId(int position) {
        return mSuggestions.get(position).hashCode();
    }

    public void updateSuggestions(@NonNull List<String> suggestions) {
        mSuggestions.clear();
        mSuggestions.addAll(suggestions);
        notifyDataSetChanged();
    }

    public void clear() {
        mSuggestions.clear();
        notifyDataSetChanged();
    }

    public class SuggestionItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.suggestions_text)
        TextView suggestion;

        public SuggestionItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onSuggestionClicked(mSuggestions.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface SuggestionClickListener {
        void onSuggestionClicked(@NonNull final String suggestion);
    }
}
