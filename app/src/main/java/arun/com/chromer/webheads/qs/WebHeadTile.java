package arun.com.chromer.webheads.qs;

import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;

/**
 * Created by Arun on 09/09/2016.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class WebHeadTile extends TileService {
    /**
     * Called when the tile is added to the Quick Settings.
     *
     * @return TileService constant indicating tile state
     */

    @Override
    public void onTileAdded() {
        Log.d("QS", "Tile added");
        updateTile();
    }

    /**
     * Called when this tile begins listening for events.
     */
    @Override
    public void onStartListening() {
        Log.d("QS", "Start listening");
        updateTile();
    }

    /**
     * Called when the user taps the tile.
     */
    @Override
    public void onClick() {
        Preferences.webHeads(this, !Preferences.webHeads(this));
        updateTile();
    }

    /**
     * Called when this tile moves out of the listening state.
     */
    @Override
    public void onStopListening() {
        Log.d("QS", "Stop Listening");
    }

    /**
     * Called when the user removes this tile from Quick Settings.
     */
    @Override
    public void onTileRemoved() {
        Log.d("QS", "Tile removed");
    }

    // Changes the appearance of the tile.
    private void updateTile() {
        final Tile tile = getQsTile();
        if (tile != null) {
            boolean isWebHeadActive = Preferences.webHeads(this);
            Icon newIcon;
            String newLabel;
            int newState;
            // Change the tile to match the service status.
            if (isWebHeadActive) {
                newLabel = getString(R.string.web_heads);
                newIcon = Icon.createWithBitmap(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_chart_bubble)
                        .color(Color.WHITE)
                        .sizeDp(24).toBitmap());

                newState = Tile.STATE_ACTIVE;
            } else {
                newLabel = getString(R.string.web_heads);
                newIcon = Icon.createWithBitmap(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_chart_bubble)
                        .color(Color.WHITE)
                        .sizeDp(24).toBitmap());
                newState = Tile.STATE_INACTIVE;
            }
            // Change the UI of the tile.
            tile.setLabel(newLabel);
            tile.setIcon(newIcon);
            tile.setState(newState);

            // Need to call updateTile for the tile to pick up changes.
            tile.updateTile();
        }
    }
}
