package arun.com.chromer.tips

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.inflate
import arun.com.chromer.extenstions.show
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.util.Utils
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.activity_tips.*
import javax.inject.Inject

class TipsActivity : BaseActivity() {
    override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun getLayoutRes() = R.layout.activity_tips

    @Inject
    lateinit var requestManager: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupTipsList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setTitle(R.string.tips)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.article_ic_close)
        }
    }

    private fun setupTipsList() {
        tips_recycler_view.layoutManager = LinearLayoutManager(this)
        tips_recycler_view.setHasFixedSize(true)
        tips_recycler_view.adapter = TipsRecyclerViewAdapter()
    }

    inner class TipsRecyclerViewAdapter : RecyclerView.Adapter<TipsItemHolder>() {
        private val provider = 0
        private val secBrowser = 1
        private val perApp = 2
        private val bottomBar = 3
        private val articleKeywords = 4
        private val quicksettings = 5

        private val items = ArrayList<Int>()

        init {
            items.add(provider)
            items.add(secBrowser)
            items.add(perApp)
            if (Utils.ANDROID_LOLLIPOP) {
                items.add(bottomBar)
            }
            items.add(articleKeywords)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                items.add(quicksettings)
            }
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: TipsItemHolder, position: Int) {
            holder.image?.gone()
            when (items[position]) {
                provider -> {
                    holder.title?.setText(R.string.choose_provider)
                    holder.subtitle?.setText(R.string.choose_provider_tip)
                    holder.image?.show()
                    requestManager.load(R.drawable.tips_providers).into(holder.image)
                }
                secBrowser -> {
                    holder.title?.setText(R.string.choose_secondary_browser)
                    holder.subtitle?.setText(R.string.tips_secondary_browser)
                    holder.image?.show()
                    requestManager.load(R.drawable.tip_secondary_browser).into(holder.image)
                }
                perApp -> {
                    holder.title?.setText(R.string.per_app_settings)
                    holder.subtitle?.setText(R.string.per_app_settings_explanation)
                    holder.image?.show()
                    requestManager.load(R.drawable.tips_per_app_settings).into(holder.image)
                }
                bottomBar -> {
                    holder.title?.setText(R.string.bottom_bar)
                    holder.subtitle?.setText(R.string.tips_bottom_bar)
                    holder.image?.show()
                    requestManager.load(R.drawable.tips_bottom_bar).into(holder.image)
                }
                articleKeywords -> {
                    holder.title?.setText(R.string.article_mode)
                    holder.subtitle?.setText(R.string.tips_article_mode)
                    holder.image?.show()
                    requestManager.load(R.drawable.tips_article_keywords).into(holder.image)
                }
                quicksettings -> {
                    holder.title?.setText(R.string.quick_settings)
                    holder.subtitle?.setText(R.string.quick_settings_tip)
                    holder.image?.show()
                    requestManager.load(R.drawable.tips_quick_settings).into(holder.image)
                }
            }
        }

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ) = TipsItemHolder(parent.inflate(R.layout.layout_tips_card))
    }

    class TipsItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.icon)
        @JvmField
        var iconView: ImageView? = null
        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null
        @BindView(R.id.subtitle)
        @JvmField
        var subtitle: TextView? = null
        @BindView(R.id.image)
        @JvmField
        var image: ImageView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
