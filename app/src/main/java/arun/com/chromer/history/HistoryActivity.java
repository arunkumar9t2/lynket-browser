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

package arun.com.chromer.history;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import arun.com.chromer.R;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.shared.base.Snackable;
import arun.com.chromer.shared.base.activity.BaseActivity;
import butterknife.BindView;

public class HistoryActivity extends BaseActivity implements Snackable {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HistoryFragment())
                .commit();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_history;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishWithTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithTransition();
        super.onBackPressed();
    }

    private void finishWithTransition() {
        finish();
    }

    @Override
    public void inject(ActivityComponent activityComponent) {
        activityComponent.inject(activityComponent);
    }
}
