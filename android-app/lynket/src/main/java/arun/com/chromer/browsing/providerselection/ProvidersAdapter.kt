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

package arun.com.chromer.browsing.providerselection

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import arun.com.chromer.R
import arun.com.chromer.data.apps.model.Provider
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.util.glide.GlideApp
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import dev.arunkumar.android.dagger.activity.PerActivity
import rx.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by arunk on 07-03-2017.
 */
@PerActivity
class ProvidersAdapter @Inject
constructor(
  private val activity: Activity,
  private val requestManager: RequestManager
) : RecyclerView.Adapter<ProvidersAdapter.RecentsViewHolder>() {

  val installClicks: PublishSubject<Provider> = PublishSubject.create()
  val selections: PublishSubject<Provider> = PublishSubject.create()

  var providers = ArrayList<Provider>()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecentsViewHolder(
    LayoutInflater.from(parent.context).inflate(
      R.layout.activity_provider_selection_provider_item_template,
      parent,
      false
    )
  )

  override fun onBindViewHolder(holder: RecentsViewHolder, position: Int) {
    val provider = providers[position]
    holder.bind(provider)
  }

  override fun onViewDetachedFromWindow(holder: RecentsViewHolder) {
    GlideApp.with(holder.itemView.context).clear(holder.icon!!)
  }

  override fun getItemCount(): Int = providers.size

  override fun getItemId(position: Int): Long = providers[position].hashCode().toLong()

  inner class RecentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @BindView(R.id.icon)
    @JvmField
    var icon: ImageView? = null

    @BindView(R.id.label)
    @JvmField
    var label: TextView? = null

    @BindView(R.id.providerInstallButton)
    @JvmField
    var install: TextView? = null

    init {
      ButterKnife.bind(this, itemView)

      install!!.setOnClickListener {
        if (adapterPosition != RecyclerView.NO_POSITION) {
          val provider = providers[adapterPosition]
          installClicks.onNext(provider)
        }
      }

      itemView.setOnClickListener {
        if (adapterPosition != RecyclerView.NO_POSITION) {
          val provider = providers[adapterPosition]
          selections.onNext(provider)
        }
      }
    }

    fun bind(provider: Provider) {
      requestManager.load(provider.iconUri).into(icon!!)
      label?.text = provider.appName

      install?.apply {
        if (provider.installed) {
          gone()
        } else {
          show()
        }
      }
    }
  }
}
