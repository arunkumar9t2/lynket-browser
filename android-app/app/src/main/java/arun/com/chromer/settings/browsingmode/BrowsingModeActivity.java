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

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.settings.RxPreferences;
import arun.com.chromer.shared.base.activity.BaseActivity;
import arun.com.chromer.util.Utils;
import butterknife.BindView;

public class BrowsingModeActivity extends BaseActivity implements BrowsingModeAdapter.BrowsingModeClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.browsing_mode_list_view)
    RecyclerView browsingModeListView;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @Inject
    RxPreferences rxPreferences;

    @Inject
    BrowsingModeAdapter adapter;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_browsing_mode;
    }

    @Override
    public void inject(@NonNull ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        browsingModeListView.setLayoutManager(new LinearLayoutManager(this));
        browsingModeListView.setAdapter(adapter);
        adapter.setBrowsingModeClickListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.browse_faster_preferences_container, BrowseFasterPreferenceFragment.newInstance())
                .commit();
    }

    @Override
    protected void onDestroy() {
        adapter.cleanUp();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onModeClicked(int position, View view) {
        boolean webHeadsEnabled = position == BrowsingModeAdapter.WEB_HEADS;
        boolean nativeBubbles = position == BrowsingModeAdapter.NATIVE_BUBBLES;

        rxPreferences.getNativeBubbles().set(nativeBubbles);
        Preferences.get(this).webHeads(webHeadsEnabled);

        if (webHeadsEnabled) {
            if (Utils.isOverlayGranted(this)) {
                Preferences.get(this).webHeads(true);
            } else {
                Preferences.get(this).webHeads(false);
                // Utils.openDrawOverlaySettings(this);
                final Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.overlay_permission_content, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.grant, v -> {
                    snackbar.dismiss();
                    Utils.openDrawOverlaySettings(BrowsingModeActivity.this);
                });
                snackbar.show();
            }
        } else if (nativeBubbles) {
            new MaterialDialog.Builder(this)
                    .title(R.string.browsing_mode_native_bubbles)
                    .content(R.string.browsing_mode_native_bubbles_warning)
                    .positiveText(R.string.browsing_mode_native_bubbles_guide)
                    .icon(new IconicsDrawable(this)
                            .icon(CommunityMaterial.Icon.cmd_android_head)
                            .colorRes(R.color.material_dark_color)
                            .sizeDp(24)
                    ).show();
        }
        adapter.notifyDataSetChanged();
    }
}
