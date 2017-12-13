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

package arun.com.chromer.home.fragment;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.browsing.browserintercept.BrowserInterceptActivity;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.di.scopes.PerFragment;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.glide.GlideApp;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by arunk on 07-03-2017.
 */
@PerFragment
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.RecentsViewHolder> {
    private final List<Website> websites = new ArrayList<>();

    @Inject
    public RecentsAdapter() {
    }

    @Override
    public RecentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_recents_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(RecentsViewHolder holder, int position) {
        final Website website = websites.get(position);
        holder.bind(website);
    }

    @Override
    public int getItemCount() {
        return websites.size();
    }

    void setWebsites(@NonNull List<? extends Website> websites) {
        this.websites.clear();
        this.websites.addAll(websites);
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

        void bind(@Nullable Website website) {
            if (website != null) {
                label.setText(website.safeLabel());
                itemView.setOnClickListener(v -> {
                    final Intent intent = new Intent(itemView.getContext(), BrowserInterceptActivity.class);
                    intent.putExtra(Constants.EXTRA_KEY_FROM_OUR_APP, true);
                    intent.setData(Uri.parse(website.preferredUrl()));
                    itemView.getContext().startActivity(intent);
                });
                GlideApp.with(itemView.getContext())
                        .load(website)
                        .into(icon);
            }
        }
    }
}
