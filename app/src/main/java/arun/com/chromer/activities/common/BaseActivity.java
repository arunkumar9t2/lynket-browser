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

package arun.com.chromer.activities.common;

import android.os.Bundle;
import android.support.annotation.LayoutRes;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;

import arun.com.chromer.Chromer;
import arun.com.chromer.di.components.ActivityComponent;
import arun.com.chromer.di.modules.ActivityModule;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by arunk on 11-01-2017.
 */

public abstract class BaseActivity<V extends Base.View, P extends Base.Presenter<V>>
        extends MvpActivity<V, P> {

    protected Unbinder unbinder;

    ActivityComponent activityComponent;

    protected final CompositeSubscription subs = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = ((Chromer) getApplication())
                .getAppComponent()
                .newActivityComponent(new ActivityModule(this));
        inject(activityComponent);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        unbinder = ButterKnife.bind(this);
    }

    protected abstract void inject(ActivityComponent activityComponent);

    @LayoutRes
    protected abstract int getLayoutRes();

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }


    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    protected void onDestroy() {
        subs.clear();
        unbinder.unbind();
        presenter.onDestroy();
        activityComponent = null;
        super.onDestroy();
    }

}
