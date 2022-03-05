/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package arun.com.chromer.shared.base.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.shared.base.ProvidesActivityComponent
import butterknife.ButterKnife
import butterknife.Unbinder
import rx.subscriptions.CompositeSubscription

/**
 * Created by Arunkumar on 05-04-2017.
 */
abstract class BaseFragment : Fragment() {

  protected val subs = CompositeSubscription()
  private lateinit var fragmentComponent: FragmentComponent
  private lateinit var unbinder: Unbinder

  protected abstract fun inject(fragmentComponent: FragmentComponent)

  @get:LayoutRes
  protected abstract val layoutRes: Int

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(
    layoutRes,
    container,
    false
  ).also { view ->
    unbinder = ButterKnife.bind(this, view)
  }

  override fun onAttach(context: Context) {
    fragmentComponent = (activity as ProvidesActivityComponent)
      .activityComponent
      .fragmentComponentFactory()
      .create(this)
      .also(::inject)
    super.onAttach(context)
  }

  override fun onDestroy() {
    subs.clear()
    unbinder.unbind()
    super.onDestroy()
  }
}
