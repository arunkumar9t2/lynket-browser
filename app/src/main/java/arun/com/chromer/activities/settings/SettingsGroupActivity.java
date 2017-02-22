package arun.com.chromer.activities.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import arun.com.chromer.activities.settings.browsingmode.BrowsingModeActivity;
import arun.com.chromer.activities.settings.browsingoptions.BrowsingOptionsActivity;
import arun.com.chromer.activities.settings.lookandfeel.LookAndFeelActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.ACTION_CLOSE_ROOT;

public class SettingsGroupActivity extends SubActivity implements SettingsGroupAdapter.GroupItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.settings_list_view)
    RecyclerView settingsListView;
    private SettingsGroupAdapter adapter;
    private BroadcastReceiver closeReceiver;

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
        registerCloseReceiver();
    }

    @Override
    protected void onDestroy() {
        adapter.cleanUp();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
        super.onDestroy();
    }

    private void registerCloseReceiver() {
        closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Finished from receiver");
                SettingsGroupActivity.this.finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(closeReceiver, new IntentFilter(ACTION_CLOSE_ROOT));
    }

    @Override
    public void onGroupItemClicked(int position, View view) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, BrowsingModeActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, LookAndFeelActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, BrowsingOptionsActivity.class));
                break;
        }
    }

}
