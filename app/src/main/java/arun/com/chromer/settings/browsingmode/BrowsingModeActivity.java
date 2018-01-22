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

package arun.com.chromer.settings.browsingmode;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import arun.com.chromer.R;
import arun.com.chromer.settings.Preferences;
import arun.com.chromer.shared.base.activity.SubActivity;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BrowsingModeActivity extends SubActivity implements BrowsingModeAdapter.BrowsingModeClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.browsing_mode_list_view)
    RecyclerView browsingModeListView;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    private BrowsingModeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browsing_mode);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new BrowsingModeAdapter(this);
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
        Preferences.get(this).webHeads(position == 1);
        if (position == 1) {
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
        } else Preferences.get(this).webHeads(false);
        adapter.notifyDataSetChanged();
    }
}
