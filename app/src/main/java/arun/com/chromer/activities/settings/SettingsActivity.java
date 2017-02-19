package arun.com.chromer.activities.settings;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends SubActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.settings_list_view)
    RecyclerView settingsListView;
    private SettingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new SettingsAdapter(this);
        settingsListView.setLayoutManager(new LinearLayoutManager(this));
        settingsListView.setAdapter(adapter);
        settingsListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

}
