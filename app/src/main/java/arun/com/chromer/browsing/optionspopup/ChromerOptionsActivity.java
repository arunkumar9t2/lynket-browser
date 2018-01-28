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

package arun.com.chromer.browsing.optionspopup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.browsing.openwith.OpenIntentWithActivity;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.history.HistoryActivity;
import arun.com.chromer.settings.SettingsGroupActivity;
import arun.com.chromer.shared.base.activity.BaseActivity;
import arun.com.chromer.shortcuts.HomeScreenShortcutCreatorActivity;
import arun.com.chromer.tabs.DefaultTabsManager;
import butterknife.BindView;
import butterknife.ButterKnife;

import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_ARTICLE;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_ORIGINAL_URL;


public class ChromerOptionsActivity extends BaseActivity {
    @BindView(R.id.menu_header)
    TextView menuHeader;
    @BindView(R.id.menu_list)
    RecyclerView menuList;
    @BindView(R.id.more_menu_card)
    CardView moreMenuCard;
    private MenuListAdapter adapter;
    private boolean fromArticle;

    @Inject
    DefaultTabsManager tabsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromArticle = getIntent().getBooleanExtra(EXTRA_KEY_FROM_ARTICLE, false);
        menuList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenuListAdapter();
        menuList.setAdapter(adapter);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_more_menu;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void inject(@NonNull ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.MenuItemHolder> {
        MenuListAdapter() {
        }

        @Override
        public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MenuItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_more_menu_item_template, parent, false));
        }

        @Override
        public void onBindViewHolder(MenuItemHolder holder, int position) {
            switch (position) {
                case 0:
                    holder.menuImage.setImageDrawable(new IconicsDrawable(holder.itemView.getContext())
                            .icon(CommunityMaterial.Icon.cmd_settings)
                            .colorRes(R.color.accent)
                            .sizeDp(24));
                    holder.menuText.setText(R.string.settings);
                    holder.itemView.setOnClickListener(v -> startActivity(new Intent(ChromerOptionsActivity.this, SettingsGroupActivity.class)));
                    break;
                case 1:
                    holder.menuImage.setImageDrawable(new IconicsDrawable(holder.itemView.getContext())
                            .icon(CommunityMaterial.Icon.cmd_history)
                            .colorRes(R.color.accent)
                            .sizeDp(24));
                    holder.menuText.setText(R.string.title_history);
                    holder.itemView.setOnClickListener(v -> startActivity(new Intent(ChromerOptionsActivity.this, HistoryActivity.class)));
                    break;
                case 2:
                    holder.menuImage.setImageDrawable(new IconicsDrawable(holder.itemView.getContext())
                            .icon(CommunityMaterial.Icon.cmd_home_variant)
                            .colorRes(R.color.accent)
                            .sizeDp(24));
                    holder.menuText.setText(R.string.add_to_homescreen);
                    holder.itemView.setOnClickListener(v -> startActivity(new Intent(ChromerOptionsActivity.this, HomeScreenShortcutCreatorActivity.class).setData(getIntent().getData())));
                    break;
                case 3:
                    holder.menuImage.setImageDrawable(new IconicsDrawable(holder.itemView.getContext())
                            .icon(CommunityMaterial.Icon.cmd_open_in_new)
                            .colorRes(R.color.accent)
                            .sizeDp(24));
                    holder.menuText.setText(R.string.open_with);
                    holder.itemView.setOnClickListener(v -> {
                        final Intent openWithActivity = new Intent(ChromerOptionsActivity.this, OpenIntentWithActivity.class);
                        openWithActivity.setData(getIntent().getData());
                        openWithActivity.putExtra(EXTRA_KEY_ORIGINAL_URL, getIntent().getDataString());
                        startActivity(openWithActivity);
                    });
                    break;
                case 4:
                    holder.menuImage.setImageDrawable(new IconicsDrawable(holder.itemView.getContext())
                            .icon(CommunityMaterial.Icon.cmd_file_image)
                            .colorRes(R.color.accent)
                            .sizeDp(24));
                    holder.menuText.setText(R.string.open_article_view);
                    holder.itemView.setOnClickListener(v -> tabsManager.openArticle(ChromerOptionsActivity.this, new Website(getIntent().getDataString()), false));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            if (fromArticle) {
                return 4;
            } else {
                return 5;
            }
        }

        class MenuItemHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.menu_image)
            ImageView menuImage;
            @BindView(R.id.menu_text)
            TextView menuText;

            MenuItemHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
