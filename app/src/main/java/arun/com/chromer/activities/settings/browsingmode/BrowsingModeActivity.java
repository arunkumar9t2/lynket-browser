package arun.com.chromer.activities.settings.browsingmode;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BrowsingModeActivity extends SubActivity implements BrowsingModeAdapter.BrowsingModeClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.browsing_mode_list_view)
    RecyclerView browsingModeListView;
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
    }

    @Override
    protected void onDestroy() {
        adapter.cleanUp();
        super.onDestroy();
    }

    @Override
    public void onModeClicked(int position, View view) {
        Preferences.get(this).webHeads(position == 1);
        adapter.notifyDataSetChanged();
    }
}
