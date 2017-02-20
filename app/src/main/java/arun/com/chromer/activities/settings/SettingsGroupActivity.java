package arun.com.chromer.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import arun.com.chromer.activities.settings.browsingmode.BrowsingModeActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsGroupActivity extends SubActivity implements SettingsGroupAdapter.GroupItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.settings_list_view)
    RecyclerView settingsListView;
    private SettingsGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new SettingsGroupAdapter(this);
        settingsListView.setLayoutManager(new LinearLayoutManager(this));
        settingsListView.setAdapter(adapter);
        settingsListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setGroupItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        adapter.cleanUp();
        super.onDestroy();
    }

    @Override
    public void onGroupItemClicked(int position, View view) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, BrowsingModeActivity.class));
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

}
