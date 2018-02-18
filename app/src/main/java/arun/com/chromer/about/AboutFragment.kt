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

package arun.com.chromer.about

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.BuildConfig
import arun.com.chromer.R
import arun.com.chromer.about.changelog.Changelog
import arun.com.chromer.extenstions.gone
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.glide.GlideApp
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable

/**
 * Created by Arun on 11/11/2015.
 */
class AboutFragment : Fragment() {
    @BindView(R.id.about_app_version_list)
    @JvmField
    var chromerList: RecyclerView? = null
    @BindView(R.id.about_author_version_list)
    @JvmField
    var authorList: RecyclerView? = null
    @BindView(R.id.creditsRv)
    @JvmField
    var creditsRv: RecyclerView? = null

    private var unBinder: Unbinder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_about, container, false)
        unBinder = ButterKnife.bind(this, rootView)
        populateData()
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder!!.unbind()
    }

    private fun populateData() {
        chromerList?.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            adapter = AppAdapter()
        }
        authorList?.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            adapter = AuthorAdapter()
        }
        creditsRv?.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            adapter = CreditsAdapter()
        }
    }

    internal inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(activity).inflate(R.layout.fragment_about_list_item_template, parent, false)
            return ItemHolder(view)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val materialDark = ContextCompat.getColor(activity!!, R.color.accent)
            holder.subtitle!!.visibility = View.VISIBLE
            when (position) {
                0 -> {
                    holder.title!!.setText(R.string.version)
                    holder.subtitle!!.text = BuildConfig.VERSION_NAME
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_information_outline)
                            .color(materialDark)
                            .sizeDp(24)
                }
                1 -> {
                    holder.title!!.setText(R.string.changelog)
                    holder.subtitle!!.setText(R.string.see_whats_new)
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_chart_line)
                            .color(materialDark)
                            .sizeDp(24)
                }
                2 -> {
                    holder.title!!.setText(R.string.join_google_plus)
                    holder.subtitle!!.setText(R.string.share_ideas)
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_google_circles_communities)
                            .color(materialDark)
                            .sizeDp(24)
                }
                3 -> {
                    holder.title!!.setText(R.string.licenses)
                    holder.subtitle!!.visibility = View.GONE
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_wallet_membership)
                            .color(materialDark)
                            .sizeDp(24)
                }
                4 -> {
                    holder.title!!.setText(R.string.translations)
                    holder.subtitle!!.setText(R.string.help_translations)
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_translate)
                            .color(materialDark)
                            .sizeDp(24)
                }
                5 -> {
                    holder.title!!.setText(R.string.source)
                    holder.subtitle!!.setText(R.string.contribute_to_chromer)
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_source_branch)
                            .color(materialDark)
                            .sizeDp(24)
                }
            }
        }

        override fun getItemCount(): Int {
            return 6
        }

        internal inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            @BindView(R.id.about_row_item_image)
            @JvmField
            var imageView: ImageView? = null
            @BindView(R.id.about_app_subtitle)
            @JvmField
            var subtitle: TextView? = null
            @BindView(R.id.about_app_title)
            @JvmField
            var title: TextView? = null

            init {
                ButterKnife.bind(this, itemView)
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                when (position) {
                    0 -> return
                    1 -> Changelog.show(activity)
                    2 -> {
                        val communityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/109754631011301174504"))
                        activity!!.startActivity(communityIntent)
                    }
                    3 -> {
                        val licenses = Intent(Intent.ACTION_VIEW, Uri.parse("http://htmlpreview.github.com/?https://github.com/arunkumar9t2/chromer/blob/master/notices.html"))
                        activity!!.startActivity(licenses)
                    }
                    4 -> {
                        val oneSkyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://os0l2aw.oneskyapp.com/collaboration/project/62112"))
                        activity!!.startActivity(oneSkyIntent)
                    }
                    5 -> {
                        val sourceIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/arunkumar9t2/chromer"))
                        activity!!.startActivity(sourceIntent)
                    }
                }
            }
        }
    }

    internal inner class AuthorAdapter : RecyclerView.Adapter<AuthorAdapter.ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(activity).inflate(R.layout.fragment_about_list_item_template, parent, false)
            return ItemHolder(view)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            when (position) {
                0 -> {
                    holder.title!!.text = Constants.ME
                    holder.subtitle!!.text = Constants.LOCATION
                    holder.imageView!!.layoutParams.height = resources.getDimension(R.dimen.arun_height).toInt()
                    holder.imageView!!.layoutParams.width = resources.getDimension(R.dimen.arun_width).toInt()
                    val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.arun)
                    val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                    roundedBitmapDrawable.setAntiAlias(true)
                    roundedBitmapDrawable.isCircular = true
                    holder.imageView!!.setImageDrawable(roundedBitmapDrawable)
                }
                1 -> {
                    holder.title!!.setText(R.string.add_to_circles)
                    holder.subtitle!!.visibility = View.GONE
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_google_circles)
                            .color(ContextCompat.getColor(activity!!, R.color.google_plus))
                            .sizeDp(24)
                }
                2 -> {
                    holder.title!!.setText(R.string.follow_twitter)
                    holder.subtitle!!.visibility = View.GONE
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_twitter)
                            .color(ContextCompat.getColor(activity!!, R.color.twitter))
                            .sizeDp(24)
                }
                3 -> {
                    holder.title!!.setText(R.string.connect_linkedIn)
                    holder.subtitle!!.visibility = View.GONE
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_linkedin_box)
                            .color(ContextCompat.getColor(activity!!, R.color.linkedin))
                            .sizeDp(24)
                }
                4 -> {
                    holder.title!!.setText(R.string.fork_on_github)
                    holder.subtitle!!.visibility = View.GONE
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_github_circle)
                            .color(Color.BLACK)
                            .sizeDp(24)
                }
                5 -> {
                    holder.title!!.setText(R.string.more_apps)
                    holder.subtitle!!.visibility = View.GONE
                    holder.imageView!!.background = IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_google_play)
                            .color(ContextCompat.getColor(activity!!, R.color.play_store_green))
                            .sizeDp(24)
                }
            }
        }

        override fun getItemCount(): Int {
            return 6
        }

        internal inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            @BindView(R.id.about_row_item_image)
            @JvmField
            var imageView: ImageView? = null
            @BindView(R.id.about_app_subtitle)
            @JvmField
            var subtitle: TextView? = null
            @BindView(R.id.about_app_title)
            @JvmField
            var title: TextView? = null

            init {
                ButterKnife.bind(this, itemView)
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                when (position) {
                    0 -> return
                    1 -> {
                        val myProfile = Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com/+arunkumar5592"))
                        activity!!.startActivity(myProfile)
                    }
                    2 -> {
                        val twitterIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/arunkumar_9t2"))
                        activity!!.startActivity(twitterIntent)
                    }
                    3 -> {
                        val linkedInIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://in.linkedin.com/in/arunkumar9t2"))
                        activity!!.startActivity(linkedInIntent)
                    }
                    4 -> {
                        val github = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/arunkumar9t2/"))
                        activity!!.startActivity(github)
                    }
                    5 -> try {
                        activity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Arunkumar")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        activity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Arunkumar")))
                    }

                }
            }
        }
    }

    internal inner class CreditsAdapter : RecyclerView.Adapter<CreditsAdapter.ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(activity).inflate(R.layout.fragment_about_list_item_template, parent, false)
            return ItemHolder(view)
        }

        val patryk = "Patryk"
        val max = "Max"
        val beta = "Beta Testers"
        private val items = arrayListOf(
                patryk,
                max,
                beta
        )
        private val patrykProfileImg = "https://lh3.googleusercontent.com/hZdzG3b5epdGAOtQQgwSwBEeGqbIbQGg68lTD7Nvp2caLJ0CeIRksMII52Q8J6SwZbWcbFRCiNYg2ss=w384-h383-rw-no"
        private val maxImg = "https://lh3.googleusercontent.com/lJn5h7sLkNMBlQwbZsyZyPrp0JNv8woEtX0hLg1o1uLmMri1VkVN10DM2XJkI4owV5u5MS5ABPbQ4s4=s1024-rw-no"

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            when (items[position]) {
                patryk -> {
                    holder.title?.text = "Patryk Goworowski"
                    holder.subtitle?.setText(R.string.icon_design)
                    GlideApp.with(holder.itemView.context).load(patrykProfileImg).into(holder.imageView)
                }
                max -> {
                    holder.title?.text = "Max Patchs"
                    holder.subtitle?.setText(R.string.illustrations_and_video)
                    GlideApp.with(holder.itemView.context).load(maxImg).into(holder.imageView)
                }
                beta -> {
                    holder.title?.setText(R.string.beta_testers)
                    holder.subtitle?.gone()
                    holder.imageView!!.setImageDrawable(IconicsDrawable(activity!!)
                            .icon(CommunityMaterial.Icon.cmd_google_plus)
                            .colorRes(R.color.md_red_700)
                            .sizeDp(24))
                }
            }
        }

        override fun getItemCount() = items.size

        internal inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            @BindView(R.id.about_row_item_image)
            @JvmField
            var imageView: ImageView? = null
            @BindView(R.id.about_app_subtitle)
            @JvmField
            var subtitle: TextView? = null
            @BindView(R.id.about_app_title)
            @JvmField
            var title: TextView? = null

            init {
                ButterKnife.bind(this, itemView)
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                when (items[adapterPosition]) {
                    patryk -> {
                        activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/+PatrykGoworowski")))
                    }
                    max -> {
                        activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/+Windows10-tutorialsBlogspot")))
                    }
                    beta -> {
                        activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/109754631011301174504")))
                    }
                }
            }
        }
    }

    class ViewHolder {
        var title: TextView? = null
        var subtitle: TextView? = null
        var imageView: ImageView? = null
    }

    companion object {

        fun newInstance(): AboutFragment {
            val fragment = AboutFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
