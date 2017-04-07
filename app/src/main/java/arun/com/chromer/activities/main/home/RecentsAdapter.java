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

package arun.com.chromer.activities.main.home;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.shared.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by arunk on 07-03-2017.
 */
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.RecentsViewHolder> {
    private final List<WebSite> webSites = new ArrayList<>();

    public RecentsAdapter() {
    }

    @Override
    public RecentsAdapter.RecentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_recents_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(RecentsViewHolder holder, int position) {
        final WebSite website = position < webSites.size() ? webSites.get(position) : null;
        if (website != null) {
            holder.label.setText(website.safeLabel());
            holder.itemView.setOnClickListener(v -> {
                final Intent intent = new Intent(holder.itemView.getContext(), BrowserInterceptActivity.class);
                intent.putExtra(Constants.EXTRA_KEY_FROM_OUR_APP, true);
                intent.setData(Uri.parse(website.preferredUrl()));
                holder.itemView.getContext().startActivity(intent);
            });
            if (!TextUtils.isEmpty(website.faviconUrl)) {
                Glide.with(holder.itemView.getContext())
                        .load(website.faviconUrl)
                        .crossFade()
                        .into(holder.icon);
            } else {

            }
        } else {
            // holder.label.setText(R.string.loading);
        }
    }

    @Override
    public int getItemCount() {
        return webSites.size();
    }

    public void setWebSites(@NonNull List<WebSite> webSites) {
        this.webSites.clear();
        this.webSites.addAll(webSites);
        notifyDataSetChanged();
    }

    static class RecentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.label)
        TextView label;

        RecentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
