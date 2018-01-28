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

package arun.com.chromer.browsing.customtabs.bottombar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Random;

import arun.com.chromer.R;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;

/**
 * Created by Arun on 06/11/2016.
 */
public class BottomBarManager {
    private static IconicsDrawable shareImageDrawable;
    private static IconicsDrawable newTabDrawable;
    private static IconicsDrawable minimizeDrawable;
    private static IconicsDrawable articleDrawable;
    private static IconicsDrawable tabsDrawable;

    @NonNull
    public static RemoteViews createBottomBarRemoteViews(@NonNull final Context context, final int toolbarColor) {
        final int iconColor = ColorUtil.getForegroundWhiteOrBlack(toolbarColor);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_bottom_bar_layout);
        remoteViews.setInt(R.id.bottom_bar_root, "setBackgroundColor", toolbarColor);

        if (!Utils.ANDROID_LOLLIPOP) {
            remoteViews.setViewVisibility(R.id.bottom_bar_minimize_tab, View.GONE);
            remoteViews.setViewVisibility(R.id.bottom_bar_tabs, View.GONE);
        }

        if (shareImageDrawable == null) {
            shareImageDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_share_variant)
                    .sizeDp(24);
        }
        if (newTabDrawable == null) {
            newTabDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_plus)
                    .sizeDp(24);
        }
        if (minimizeDrawable == null) {
            minimizeDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_arrow_down)
                    .sizeDp(24);
        }

        if (articleDrawable == null) {
            articleDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_file_image)
                    .sizeDp(24);
        }

        if (tabsDrawable == null) {
            tabsDrawable = new IconicsDrawable(context)
                    .icon(CommunityMaterial.Icon.cmd_view_agenda)
                    .sizeDp(24);
        }


        final Bitmap shareImage = shareImageDrawable.color(iconColor).toBitmap();
        final Bitmap openInNewTabImage = newTabDrawable.color(iconColor).toBitmap();
        final Bitmap minimize = minimizeDrawable.color(iconColor).toBitmap();
        final Bitmap article = articleDrawable.color(iconColor).toBitmap();
        final Bitmap tab = tabsDrawable.color(iconColor).toBitmap();

        remoteViews.setBitmap(R.id.bottom_bar_open_in_new_tab_img, "setImageBitmap", openInNewTabImage);
        remoteViews.setTextColor(R.id.bottom_bar_open_in_new_tab_text, iconColor);
        remoteViews.setBitmap(R.id.bottom_bar_share_img, "setImageBitmap", shareImage);
        remoteViews.setTextColor(R.id.bottom_bar_share_text, iconColor);
        remoteViews.setBitmap(R.id.bottom_bar_tabs_img, "setImageBitmap", tab);
        remoteViews.setTextColor(R.id.bottom_bar_tabs_text, iconColor);
        remoteViews.setBitmap(R.id.bottom_bar_minimize_img, "setImageBitmap", minimize);
        remoteViews.setTextColor(R.id.bottom_bar_minimize_text, iconColor);
        remoteViews.setBitmap(R.id.bottom_bar_article_view_img, "setImageBitmap", article);
        remoteViews.setTextColor(R.id.bottom_bar_article_view_text, iconColor);
        return remoteViews;
    }

    @NonNull
    public static int[] getClickableIDs() {
        return new int[]{
                R.id.bottom_bar_open_in_new_tab,
                R.id.bottom_bar_share,
                R.id.bottom_bar_tabs,
                R.id.bottom_bar_minimize_tab,
                R.id.bottom_bar_article_view};
    }

    /**
     * @return The PendingIntent that will be triggered when the user clicks on the Views listed by
     * {@link BottomBarManager#getClickableIDs()}.
     */
    @NonNull
    public static PendingIntent getOnClickPendingIntent(Context context, String url) {
        final Intent broadcastIntent = new Intent(context, BottomBarReceiver.class);
        broadcastIntent.putExtra(Constants.EXTRA_KEY_ORIGINAL_URL, url);
        return PendingIntent.getBroadcast(context, new Random().nextInt(), broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
