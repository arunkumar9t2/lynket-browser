/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.bubbles.webheads.ui.context;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.util.glide.GlideApp;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arun on 05/09/2016.
 */

class WebsiteAdapter extends RecyclerView.Adapter<WebsiteAdapter.WebSiteHolder> {
  private final Context context;
  private final List<Website> websites = new ArrayList<>();
  private final WebSiteAdapterListener listener;

  WebsiteAdapter(@NonNull Context context, @NonNull WebSiteAdapterListener listener) {
    this.context = context.getApplicationContext();
    this.listener = listener;
    setHasStableIds(true);
  }

  @Override
  public WebSiteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new WebSiteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_web_head_context_item_template, parent, false));
  }

  @Override
  public void onBindViewHolder(WebSiteHolder holder, int position) {
    final Website website = websites.get(position);
    holder.deleteIcon.setImageDrawable(new IconicsDrawable(context)
      .icon(CommunityMaterial.Icon.cmd_close)
      .color(ContextCompat.getColor(context, R.color.accent_icon_no_focus))
      .sizeDp(16));
    holder.shareIcon.setImageDrawable(new IconicsDrawable(context)
      .icon(CommunityMaterial.Icon.cmd_share_variant)
      .color(ContextCompat.getColor(context, R.color.accent_icon_no_focus))
      .sizeDp(16));
    holder.url.setText(website.preferredUrl());
    holder.title.setText(website.safeLabel());
    GlideApp.with(context)
      .load(website.faviconUrl)
      .into(holder.icon);
  }

  @Override
  public int getItemCount() {
    return websites.size();
  }

  @Override
  public long getItemId(int position) {
    return websites.get(position).hashCode();
  }

  List<Website> getWebsites() {
    return websites;
  }

  void setWebsites(ArrayList<Website> websites) {
    this.websites.clear();
    this.websites.addAll(websites);
    notifyDataSetChanged();
  }

  void delete(@NonNull Website website) {
    final int index = websites.indexOf(website);
    if (index != -1) {
      websites.remove(index);
      notifyItemRemoved(index);
      listener.onWebSiteDelete(website);
    }
  }

  void update(@NonNull Website web) {
    final int index = websites.indexOf(web);
    if (index != -1) {
      websites.remove(index);
      websites.add(index, web);
      notifyItemChanged(index);
    }
  }

  interface WebSiteAdapterListener {
    void onWebSiteItemClicked(@NonNull Website website);

    void onWebSiteDelete(@NonNull Website website);

    void onWebSiteShare(@NonNull Website website);

    void onWebSiteLongClicked(@NonNull Website website);
  }

  class WebSiteHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.web_site_icon)
    ImageView icon;
    @BindView(R.id.web_site_title)
    TextView title;
    @BindView(R.id.web_site_sub_title)
    TextView url;
    @BindView(R.id.delete_icon)
    ImageView deleteIcon;
    @BindView(R.id.share_icon)
    ImageView shareIcon;

    WebSiteHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      itemView.setOnClickListener(v -> {
        final Website website = getWebsite();
        if (website != null) {
          listener.onWebSiteItemClicked(website);
        }
      });

      itemView.setOnLongClickListener(v -> {
        final Website website = getWebsite();
        if (website != null) {
          listener.onWebSiteLongClicked(website);
        }
        return true;
      });

      deleteIcon.setOnClickListener(v -> {
        final Website website = getWebsite();
        if (website != null) {
          websites.remove(website);
          listener.onWebSiteDelete(website);
          notifyDataSetChanged();
        }
      });

      shareIcon.setOnClickListener(v -> {
        final Website website = getWebsite();
        if (website != null) {
          listener.onWebSiteShare(website);
        }
      });
    }

    @Nullable
    private Website getWebsite() {
      final int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        return websites.get(position);
      } else return null;
    }
  }
}
