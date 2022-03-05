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
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import arun.com.chromer.R
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.util.glide.GlideApp
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import kotlinx.android.synthetic.main.fragment_text_intro.*

open class AppIntroFragment : BaseFragment(), ISlideBackgroundColorHolder {

  override fun getDefaultBackgroundColor(): Int =
    ContextCompat.getColor(context!!, R.color.tutorialBackgrounColor)

  override fun setBackgroundColor(backgroundColor: Int) {
    if (root != null) {
      root.setBackgroundColor(backgroundColor)
    }
  }

  override fun inject(fragmentComponent: FragmentComponent) = fragmentComponent.inject(this)

  override val layoutRes: Int get() = R.layout.fragment_text_intro

  private var drawable: Int = 0
  private var bgColor: Int = 0
  private var titleColor: Int = 0
  private var descColor: Int = 0
  private var title: CharSequence? = null
  private var description: CharSequence? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (this.arguments != null && this.arguments!!.size() != 0) {
      this.drawable = this.arguments!!.getInt("drawable")
      this.title = this.arguments!!.getCharSequence("title")
      this.description = this.arguments!!.getCharSequence("desc")
      this.bgColor = this.arguments!!.getInt("bg_color")
      this.titleColor =
        if (this.arguments!!.containsKey("title_color")) this.arguments!!.getInt("title_color") else 0
      this.descColor =
        if (this.arguments!!.containsKey("desc_color")) this.arguments!!.getInt("desc_color") else 0
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    root.setBackgroundColor(this.bgColor)
    titleTv.text = this.title
    if (this.titleColor != 0) {
      titleTv!!.setTextColor(this.titleColor)
    }

    descriptionTv.text = this.description
    if (this.descColor != 0) {
      descriptionTv!!.setTextColor(this.descColor)
    }

    // Use glide to load the drawable
    GlideApp.with(this).load(drawable).into(imageView!!)
  }

  companion object {
    fun newInstance(
      title: CharSequence,
      description: CharSequence,
      @DrawableRes imageDrawable: Int,
      @ColorInt bgColor: Int
    ): AppIntroFragment {
      return newInstance(title, description, imageDrawable, bgColor, 0, 0)
    }

    fun newInstance(
      title: CharSequence,
      description: CharSequence,
      @DrawableRes imageDrawable: Int,
      @ColorInt bgColor: Int,
      @ColorInt titleColor: Int,
      @ColorInt descColor: Int
    ): AppIntroFragment {
      val sampleSlide = AppIntroFragment()
      val args = Bundle()
      args.putCharSequence("title", title)
      args.putCharSequence("desc", description)
      args.putInt("drawable", imageDrawable)
      args.putInt("bg_color", bgColor)
      args.putInt("title_color", titleColor)
      args.putInt("desc_color", descColor)
      sampleSlide.arguments = args
      return sampleSlide
    }
  }
}
