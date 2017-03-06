package arun.com.chromer.activities.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import arun.com.chromer.R;
import arun.com.chromer.activities.SnackHelper;
import arun.com.chromer.activities.base.SubActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends SubActivity implements History.View, SnackHelper {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.history_list)
    RecyclerView historyList;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private HistoryAdapter historyAdapter;

    History.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new History.Presenter(this);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        historyList.setLayoutManager(linearLayoutManager);
        historyAdapter = new HistoryAdapter(linearLayoutManager);
        historyList.setAdapter(historyAdapter);
        historyList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                historyAdapter.onRangeChanged();
            }
        });

        presenter.loadHistory(this);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.accent));
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadHistory(this));
    }

    @Override
    public void loading(boolean loading) {
        swipeRefreshLayout.setRefreshing(loading);
    }

    @Override
    public void setCursor(@Nullable Cursor cursor) {
        historyAdapter.setCursor(cursor);
    }

    @Override
    public void snack(@NonNull String textToSnack) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void snackLong(@NonNull String textToSnack) {
        Snackbar.make(coordinatorLayout, textToSnack, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        historyAdapter.cleanUp();
        presenter.cleanUp();
    }
}
