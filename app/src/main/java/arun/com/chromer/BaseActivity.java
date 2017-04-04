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

package arun.com.chromer;

import android.os.Bundle;
import android.support.annotation.LayoutRes;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Arunkumar on 05-04-2017.
 */
public abstract class BaseActivity<V extends Base.View, P extends Base.Presenter<V>>
        extends MvpActivity<V, P> {

    protected Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        unbinder = ButterKnife.bind(this);
    }

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
        presenter.onDestroy();
        unbinder.unbind();
        super.onDestroy();
    }

    @LayoutRes
    protected abstract int getLayoutRes();
}