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

package arun.com.chromer.shared.base.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arun.com.chromer.di.fragment.FragmentComponent;
import arun.com.chromer.di.fragment.FragmentModule;
import arun.com.chromer.shared.base.ProvidesActivityComponent;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Arunkumar on 05-04-2017.
 */
public abstract class BaseFragment extends Fragment {
    protected final CompositeSubscription subs = new CompositeSubscription();
    private FragmentComponent fragmentComponent;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        fragmentComponent = ((ProvidesActivityComponent) getActivity())
                .getActivityComponent()
                .newFragmentComponent(new FragmentModule(this));
        inject(fragmentComponent);
        super.onAttach(context);
    }

    protected abstract void inject(FragmentComponent fragmentComponent);

    @Override
    public void onDestroy() {
        subs.clear();
        if (unbinder != null) {
            unbinder.unbind();
        }
        fragmentComponent = null;
        super.onDestroy();
    }

    @LayoutRes
    protected abstract int getLayoutRes();
}