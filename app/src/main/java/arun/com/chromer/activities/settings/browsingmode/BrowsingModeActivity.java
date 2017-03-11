package arun.com.chromer.activities.settings.browsingmode;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BrowsingModeActivity extends SubActivity implements BrowsingModeAdapter.BrowsingModeClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.browsing_mode_list_view)
    RecyclerView browsingModeListView;
    @BindView(R.id.coordinator_layout)
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
