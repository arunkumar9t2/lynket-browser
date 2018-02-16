package arun.com.chromer.browsing.providerselection

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import arun.com.chromer.R
import arun.com.chromer.data.apps.model.Provider
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.watch
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_provder_selection.*
import java.util.*
import javax.inject.Inject

class ProviderSelectionActivity : BaseActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var providersAdapter: ProvidersAdapter

    private lateinit var providerSelectionViewModel: ProviderSelectionViewModel

    override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun getLayoutRes() = R.layout.activity_provder_selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupWebViewCard()
        setupCustomTabProvidersCard()

        observeViewModel(savedInstanceState)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.article_ic_close)
        }
    }

    private fun setupCustomTabProvidersCard() {
        providerRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ProviderSelectionActivity, 4)
            adapter = providersAdapter
        }
    }

    private fun setupWebViewCard() {
        GlideApp.with(this)
                .load(ApplicationIcon.createUri(Constants.SYSTEM_WEBVIEW))
                .error(IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_web)
                        .colorRes(R.color.primary)
                        .sizeDp(56))
                .into(webViewImg)
    }

    private fun observeViewModel(savedInstanceState: Bundle?) {
        providerSelectionViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(ProviderSelectionViewModel::class.java)
                .apply {
                    providersLiveData.watch(
                            this@ProviderSelectionActivity,
                            { value ->
                                providersAdapter.providers = value as ArrayList<Provider>
                            })


                    if (savedInstanceState == null) {
                        loadProviders()
                    }
                }
    }
}
