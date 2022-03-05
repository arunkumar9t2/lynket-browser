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

package arun.com.chromer.intro.fragments

import android.os.Bundle
import androidx.core.content.ContextCompat
import arun.com.chromer.R
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.glide.GlideApp
import butterknife.OnClick
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import kotlinx.android.synthetic.main.fragment_slide_over_intro.*
import javax.inject.Inject

open class ArticleIntroFragment : BaseFragment(), ISlideBackgroundColorHolder {
  override fun getDefaultBackgroundColor(): Int =
    ContextCompat.getColor(context!!, R.color.tutorialBackgrounColor)

  override fun setBackgroundColor(backgroundColor: Int) {
    root.setBackgroundColor(backgroundColor)
  }

  @Inject
  lateinit var tabsManager: TabsManager

  override fun inject(fragmentComponent: FragmentComponent) = fragmentComponent.inject(this)

  override val layoutRes: Int get() = R.layout.fragment_article_intro

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    GlideApp.with(this).load(R.drawable.tutorial_article_mode).into(imageView!!)
  }

  @OnClick(R.id.tryItButton)
  fun onSeeDemoClick() {
    tabsManager.openArticle(context!!, Website("https://en.wikipedia.org/wiki/Web_browser"))
  }
}
