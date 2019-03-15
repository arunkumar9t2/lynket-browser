/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.settings.browsingmode;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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
import arun.com.chromer.settings.Preferences;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arunkumar on 19-02-2017.
 */
class BrowsingModeAdapter extends RecyclerView.Adapter<BrowsingModeAdapter.BrowsingModeViewHolder> {
    private final Context context;
    private final List<String> settingsItems = new ArrayList<>();
    private BrowsingModeClickListener browsingModeClickListener = (position, view) -> {
        // no-op
    };

    BrowsingModeAdapter(@NonNull final Context context) {
        setHasStableIds(true);
        this.context = context.getApplicationContext();
        settingsItems.add(context.getString(R.string.browsing_mode_slide_over));
        settingsItems.add(context.getString(R.string.browsing_mode_web_heads));
    }

    @Override
    public BrowsingModeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BrowsingModeViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_browsing_mode_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(final BrowsingModeViewHolder holder, int position) {
        holder.bind(settingsItems.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION)
                browsingModeClickListener.onModeClicked(holder.getAdapterPosition(), holder.itemView);
        });
    }

    @Override
    public long getItemId(int position) {
        return settingsItems.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return settingsItems.size();
    }

    void setBrowsingModeClickListener(@NonNull BrowsingModeClickListener browsingModeClickListener) {
        this.browsingModeClickListener = browsingModeClickListener;
    }

    void cleanUp() {
        browsingModeClickListener = (position, view) -> {
        };
        settingsItems.clear();
    }

    public static class BrowsingModeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.subtitle)
        TextView subtitle;
        @BindView(R.id.browsing_mode_selector)
        ImageView selector;
        @BindView(R.id.browsing_mode_root)
        CardView browsingModeRoot;

        BrowsingModeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(@NonNull final String item) {
            title.setText(item);
            final int position = getAdapterPosition();
            final boolean webHeads = Preferences.get(selector.getContext()).webHeads();
            switch (position) {
                case 0:
                    icon.setImageDrawable(new IconicsDrawable(icon.getContext())
                            .icon(CommunityMaterial.Icon.cmd_open_in_app)
                            .color(Color.WHITE)
                            .sizeDp(24));
                    selector.setImageDrawable(new IconicsDrawable(selector.getContext())
                            .icon(webHeads ? CommunityMaterial.Icon.cmd_checkbox_blank_circle_outline : CommunityMaterial.Icon.cmd_checkbox_marked_circle)
                            .color(Color.WHITE)
                            .sizeDp(24));
                    title.setTextColor(Color.WHITE);
                    subtitle.setTextColor(Color.WHITE);
                    subtitle.setText(R.string.browsing_mode_slide_over_explanation);
                    browsingModeRoot.setCardBackgroundColor(ContextCompat.getColor(browsingModeRoot.getContext(), R.color.md_blue_600));
                    break;
                case 1:
                    icon.setImageDrawable(new IconicsDrawable(icon.getContext())
                            .icon(CommunityMaterial.Icon.cmd_chart_bubble)
                            .color(Color.WHITE)
                            .sizeDp(24));
                    selector.setImageDrawable(new IconicsDrawable(selector.getContext())
                            .icon(!webHeads ? CommunityMaterial.Icon.cmd_checkbox_blank_circle_outline : CommunityMaterial.Icon.cmd_checkbox_marked_circle)
                            .color(Color.WHITE)
                            .sizeDp(24));
                    title.setTextColor(Color.WHITE);
                    subtitle.setTextColor(Color.WHITE);
                    subtitle.setText(R.string.browsing_mode_web_heads_explanation);
                    browsingModeRoot.setCardBackgroundColor(Color.parseColor("#8CC152"));
                    break;
            }
        }
    }

    interface BrowsingModeClickListener {
        void onModeClicked(int position, final View view);
    }
}
