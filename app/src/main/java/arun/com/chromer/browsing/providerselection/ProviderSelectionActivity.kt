package arun.com.chromer.browsing.providerselection

import android.annotation.TargetApi
import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.widget.ImageView
import android.widget.TextView
import arun.com.chromer.R
import arun.com.chromer.data.apps.model.Provider
import arun.com.chromer.di.activity.ActivityComponent
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.watch
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.browsingoptions.BrowsingOptionsActivity
import arun.com.chromer.shared.Constants
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.util.RxEventBus
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_provider_selection.*
import rx.subscriptions.CompositeSubscription
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

    private var providerDialog: ProviderDialog? = null

    override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun getLayoutRes() = R.layout.activity_provider_selection

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

        subs.apply {
            providersAdapter.selections.subscribe { onProviderSelected(it) }
            providersAdapter.installClicks.subscribe { onProviderInstallClicked(it) }
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

    @TargetApi(Build.VERSION_CODES.M)
    private fun onProviderSelected(provider: Provider) {
        providerDialog = ProviderDialog(this, provider, preferences).show()
    }

    private fun onProviderInstallClicked(provider: Provider) {
        if (!provider.installed) {
            Utils.openPlayStore(this, provider.packageName)
        }
    }

    override fun onDestroy() {
        providerDialog?.dismiss()
        super.onDestroy()
    }

    inner class ProviderDialog(
            private var activity: Activity?,
            private val provider: Provider,
            private val preferences: Preferences
    ) : DialogInterface.OnDismissListener {
        val subs = CompositeSubscription()
        private lateinit var unBinder: Unbinder
        private var dialog: MaterialDialog? = null

        @BindView(R.id.icon_view)
        @JvmField
        var icon: ImageView? = null
        @BindView(R.id.providerDetails)
        @JvmField
        var providerDetailsTv: TextView? = null
        @BindView(R.id.features)
        @JvmField
        var features: TextView? = null

        fun show(): ProviderDialog? {
            dialog = MaterialDialog.Builder(activity!!)
                    .title(provider.appName)
                    .customView(R.layout.dialog_provider_info, false)
                    .dismissListener(this)
                    .positiveText(if (provider.installed) R.string.use else R.string.install)
                    .onPositive({ _, _ ->
                        if (provider.installed) {
                            preferences.customTabPackage(provider.packageName)
                            rxEventBus.post(BrowsingOptionsActivity.ProviderChanged())
                        } else {
                            Utils.openPlayStore(activity!!, provider.packageName)
                        }
                        dismiss()
                        activity?.finish()
                    }).show()
            unBinder = ButterKnife.bind(this, dialog!!.customView!!)

            GlideApp.with(activity).load(provider.iconUri).into(icon)

            if (provider.features.isNotEmpty()) {
                providerDetailsTv!!.text = provider.features
            } else {
                features!!.gone()
            }
            return this
        }

        fun dismiss() {
            dialog?.dismiss()
        }

        override fun onDismiss(dialogInterface: DialogInterface?) {
            subs.clear()
            activity = null
            unBinder.unbind()
            dialog = null
        }
    }
}
