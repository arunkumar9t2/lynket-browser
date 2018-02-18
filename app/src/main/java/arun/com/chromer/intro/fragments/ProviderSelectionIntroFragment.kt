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

package arun.com.chromer.intro.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import arun.com.chromer.R
import arun.com.chromer.browsing.providerselection.ProviderSelectionActivity
import arun.com.chromer.di.fragment.FragmentComponent
import arun.com.chromer.shared.base.fragment.BaseFragment
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.util.glide.GlideApp
import butterknife.OnClick
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import kotlinx.android.synthetic.main.fragment_slide_over_intro.*
import javax.inject.Inject

open class ProviderSelectionIntroFragment : BaseFragment(), ISlideBackgroundColorHolder {
    override fun getDefaultBackgroundColor(): Int = Color.parseColor("#A31A33")

    override fun setBackgroundColor(backgroundColor: Int) {
        root.setBackgroundColor(backgroundColor)
    }

    @Inject
    lateinit var tabsManager: DefaultTabsManager

    override fun inject(fragmentComponent: FragmentComponent) = fragmentComponent.inject(this)
    override fun getLayoutRes() = R.layout.fragment_provider_selection_intro

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        GlideApp.with(this).load(R.drawable.tutorial_choose_browser).into(imageView!!)
    }

    @OnClick(R.id.chooseProviderButton)
    fun chooseProviderButton() {
        startActivity(Intent(context, ProviderSelectionActivity::class.java))
    }
}