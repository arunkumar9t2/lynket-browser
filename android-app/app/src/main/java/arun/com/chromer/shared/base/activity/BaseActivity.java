/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.shared.base.activity;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import arun.com.chromer.Chromer;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.di.activity.ActivityModule;
import arun.com.chromer.shared.base.ProvidesActivityComponent;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseActivity extends AppCompatActivity implements ProvidesActivityComponent {
    protected final CompositeSubscription subs = new CompositeSubscription();
    protected Unbinder unbinder;
    ActivityComponent activityComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = ((Chromer) getApplication())
                .getAppComponent()
                .newActivityComponent(new ActivityModule(this));
        inject(activityComponent);

        super.onCreate(savedInstanceState);

        @LayoutRes int layoutRes = getLayoutRes();
        if (layoutRes != 0) {
            setContentView(getLayoutRes());
            unbinder = ButterKnife.bind(this);
        }
    }

    @NonNull
    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @LayoutRes
    protected abstract int getLayoutRes();

    @Override
    protected void onDestroy() {
        subs.clear();
        if (unbinder != null) {
            unbinder.unbind();
        }
        activityComponent = null;
        super.onDestroy();
    }
}
