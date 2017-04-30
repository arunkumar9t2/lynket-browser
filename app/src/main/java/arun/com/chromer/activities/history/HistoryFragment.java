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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.activities.SnackHelper;
import arun.com.chromer.activities.mvp.BaseFragment;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by arunk on 07-04-2017.
 */

public class HistoryFragment extends BaseFragment<History.View, History.Presenter> implements History.View {
    @BindView(R.id.history_list)
    RecyclerView historyList;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.error)
    TextView error;
    private HistoryAdapter historyAdapter;

    @Override
    public void snack(@NonNull String message) {
        ((SnackHelper) getActivity()).snack(message);
    }

    @Override
    public void snackLong(@NonNull String message) {
        ((SnackHelper) getActivity()).snackLong(message);
    }


    @Override
    public void loading(boolean loading) {
        swipeRefreshLayout.setRefreshing(loading);
    }

    @Override
    public void setCursor(@Nullable Cursor cursor) {
        historyList.postDelayed(() -> {
            historyAdapter.setCursor(cursor);
            error.setVisibility(cursor == null || cursor.isClosed() || cursor.getCount() == 0 ? View.VISIBLE : View.GONE);
        }, 100);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_history;
    }

    @NonNull
    @Override
    public History.Presenter createPresenter() {
        return new History.Presenter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        historyAdapter.cleanUp();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        historyList.setLayoutManager(linearLayoutManager);
        historyAdapter = new HistoryAdapter(linearLayoutManager);
        historyList.setAdapter(historyAdapter);
        historyList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                historyAdapter.onRangeChanged();
            }
        });

        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.accent));
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadHistory(getContext()));

        final ItemTouchHelper.SimpleCallback swipeTouch = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.deleteHistory(getContext(), historyAdapter.getWebsiteAt(viewHolder.getAdapterPosition()));
            }
        };
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeTouch);
        itemTouchHelper.attachToRecyclerView(historyList);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadHistory(getContext());
        getActivity().setTitle(R.string.title_history);
    }

    @OnClick(R.id.fab)
    public void onClearAllFabClick() {
        if (historyAdapter.getItemCount() != 0) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.are_you_sure)
                    .content(R.string.history_deletion_confirmation_content)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive((dialog, which) -> presenter.deleteAll(getContext()))
                    .show();
        }
    }
}
