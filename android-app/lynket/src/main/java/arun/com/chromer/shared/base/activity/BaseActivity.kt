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
package arun.com.chromer.shared.base.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.di.app.appComponent
import arun.com.chromer.shared.base.ProvidesActivityComponent
import arun.com.chromer.util.lifecycle.ActivityLifecycleEvents
import butterknife.ButterKnife
import butterknife.Unbinder
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), ProvidesActivityComponent {

  protected val subs = CompositeSubscription()

  protected var unbinder: Unbinder? = null

  override lateinit var activityComponent: ActivityComponent

  @get:LayoutRes
  protected abstract val layoutRes: Int

  @Inject
  protected lateinit var lifecycleEvents: ActivityLifecycleEvents

  override fun onCreate(savedInstanceState: Bundle?) {
    activityComponent = application
      .appComponent()
      .activityComponentFactory()
      .create(this)
      .also(this::inject)
    super.onCreate(savedInstanceState)
    if (layoutRes != 0) {
      setContentView(layoutRes)
      unbinder = ButterKnife.bind(this)
    }
  }

  override fun onDestroy() {
    subs.clear()
    unbinder?.unbind()
    super.onDestroy()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.home) {
      finishWithTransition()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  protected fun finishWithTransition() {
    finishAfterTransition()
  }
}
