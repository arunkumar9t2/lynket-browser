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

package arun.com.chromer.shared.base.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;

import javax.inject.Inject;

import arun.com.chromer.Chromer;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.di.activity.ActivityModule;
import arun.com.chromer.shared.base.Base;
import arun.com.chromer.shared.base.ProvidesActivityComponent;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by arunk on 11-01-2017.
 */

public abstract class BaseMVPActivity<V extends Base.View, P extends Base.Presenter<V>>
        extends MvpActivity<V, P> implements ProvidesActivityComponent {

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

        @LayoutRes int layoutRes = getLayoutRes();
        if (layoutRes != 0) {
            setContentView(getLayoutRes());
            unbinder = ButterKnife.bind(this);
        }
    }

    @Inject
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

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

    @Override
    protected void onDestroy() {
        subs.clear();
        if (unbinder != null) {
            unbinder.unbind();
        }
        presenter.onDestroy();
        activityComponent = null;
        super.onDestroy();
    }

}
