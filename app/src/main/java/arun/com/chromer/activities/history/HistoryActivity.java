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

package arun.com.chromer.activities.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HistoryActivity extends SubActivity implements History.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.history_list)
    RecyclerView historyList;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;

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

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.accent));
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadHistory(this));

        final ItemTouchHelper.SimpleCallback swipeTouch = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.deleteHistory(getApplicationContext(), historyAdapter.getWebsiteAt(viewHolder.getAdapterPosition()));
            }
        };
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeTouch);
        itemTouchHelper.attachToRecyclerView(historyList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loadHistory(this);
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

    @OnClick(R.id.fab)
    public void onFabDeleteClick() {
        if (historyAdapter.getItemCount() != 0) {
            new MaterialDialog.Builder(this)
                    .title(R.string.are_you_sure)
                    .content(R.string.history_deletion_confirmation_content)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive((dialog, which) -> presenter.deleteAll(getApplicationContext()))
                    .show();
        }
    }
}
