package arun.com.chromer.customtabs.bottombar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.util.ColorUtil;

/**
 * Created by Arun on 06/11/2016.
 */
public class BottomBarManager {

    @NonNull
    public static RemoteViews createBottomBarRemoteViews(@NonNull final Context context, final int toolbarColor, final @NonNull Config config) {
        final int iconColor = ColorUtil.getForegroundWhiteOrBlack(toolbarColor);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.bottom_bar_layout);
        remoteViews.setInt(R.id.bottom_bar_root, "setBackgroundColor", toolbarColor);

        if (!config.minimize) {
            remoteViews.setViewVisibility(R.id.bottom_bar_minimize_tab, View.GONE);
        }
        if (!config.openInNewTab) {
            remoteViews.setViewVisibility(R.id.bottom_bar_open_in_new_tab, View.GONE);
        }

        final Bitmap shareImage = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_share_variant)
                .color(iconColor)
                .sizeDp(24).toBitmap();
        final Bitmap openInNewTabImage = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_plus_box)
                .color(iconColor)
                .sizeDp(24).toBitmap();
        final Bitmap minimize = new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_flip_to_back)
                .color(iconColor)
                .sizeDp(24).toBitmap();

        remoteViews.setBitmap(R.id.bottom_bar_open_in_new_tab_img, "setImageBitmap", openInNewTabImage);
        remoteViews.setBitmap(R.id.bottom_bar_share_img, "setImageBitmap", shareImage);
        remoteViews.setBitmap(R.id.bottom_bar_minimize_img, "setImageBitmap", minimize);
        return remoteViews;
    }

    @NonNull
    public static int[] getClickableIDs() {
        return new int[]{
                R.id.bottom_bar_open_in_new_tab,
                R.id.bottom_bar_share,
                R.id.bottom_bar_minimize_tab};
    }

    /**
     * @return The PendingIntent that will be triggered when the user clicks on the Views listed by
     * {@link BottomBarManager#getClickableIDs()}.
     */
    @NonNull
    public static PendingIntent getOnClickPendingIntent(Context context) {
        final Intent broadcastIntent = new Intent(context, BottomBarReceiver.class);
        return PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static class Config {
        public boolean minimize = false;
        public boolean openInNewTab = false;
    }
}
