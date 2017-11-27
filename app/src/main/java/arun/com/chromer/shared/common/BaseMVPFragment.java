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

package arun.com.chromer.shared.common;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby3.mvp.MvpFragment;

import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.di.fragment.FragmentComponent;
import arun.com.chromer.di.fragment.FragmentModule;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Arunkumar on 05-04-2017.
 */
public abstract class BaseMVPFragment<V extends Base.View, P extends Base.Presenter<V>>
        extends MvpFragment<V, P> {
    private FragmentComponent fragmentComponent;
    private Unbinder unbinder;

    protected final CompositeSubscription subs = new CompositeSubscription();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentComponent = getActivityComponent().newFragmentComponent(new FragmentModule(this));
        inject(fragmentComponent);
    }

    /**
     * Returns the Activity component the fragment is subcomponent of.
     */
    protected ActivityComponent getActivityComponent() {
        return ((ProvidesActivityComponent) getActivity()).getActivityComponent();
    }

    protected abstract void inject(FragmentComponent fragmentComponent);

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
        unbinder.unbind();
        fragmentComponent = null;
    }

    @LayoutRes
    protected abstract int getLayoutRes();
}