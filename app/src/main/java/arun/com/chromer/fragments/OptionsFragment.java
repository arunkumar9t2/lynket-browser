package arun.com.chromer.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.blacklist.BlacklistManagerActivity;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.AppPreferenceCardView;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class OptionsFragment extends Fragment {
    @BindView(R.id.customtab_preference_view)
    public AppPreferenceCardView mCustomTabPreferenceView;
    @BindView(R.id.browser_preference_view)
    public AppPreferenceCardView mBrowserPreferenceView;
    @BindView(R.id.favshare_preference_view)
    public AppPreferenceCardView mFavSharePreferenceView;
    @BindView(R.id.warm_up_switch)
    public SwitchCompat mWarmUpSwitch;
    @BindView(R.id.pre_fetch_switch)
    public SwitchCompat mPrefetchSwitch;
    @BindView(R.id.merge_tabs_switch)
    public SwitchCompat mMergeSwitch;
    @BindView(R.id.only_wifi_switch)
    public AppCompatCheckBox mWifiCheckBox;
    @BindView(R.id.show_notification_checkbox)
    public AppCompatCheckBox mNotificationCheckBox;
    @BindView(R.id.merge_tabs_apps_layout)
    public LinearLayout mMergeTabsLayout;
    @BindView(R.id.set_default_card)
    public CardView mSetDefaultCard;
    @BindView(R.id.set_default_image)
    public ImageView mSetDefaultIcon;

    private Unbinder mUnbinder;

    private Context mAppContext;

    private FragmentInteractionListener mListener;

    public static OptionsFragment newInstance() {
        OptionsFragment fragment = new OptionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAppContext = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.options_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMergeTabsLayout.setVisibility(View.VISIBLE);
        }
        setupSwitches();
        setupDefaultBrowser();
        updateDefaultBrowserCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mAppContext = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePrefetchIfPermissionGranted();
        updateSubPreferences(Preferences.preFetch(getActivity()));
        updateDefaultBrowserCard();
    }

    @Override
    public void onAttach(Context context) {
        Timber.d("On Attached");
        try {
            mListener = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement FragmentInteractionListener");
        }
        super.onAttach(context);
    }

    private void setupDefaultBrowser() {
        mSetDefaultIcon.setImageDrawable(new IconicsDrawable(mAppContext)
                .icon(GoogleMaterial.Icon.gmd_new_releases)
                .color(ContextCompat.getColor(mAppContext, R.color.colorAccentText))
                .sizeDp(30));
    }

    private void updateDefaultBrowserCard() {
        if (!Util.isDefaultBrowser(getActivity())) {
            mSetDefaultCard.setVisibility(View.VISIBLE);
            if (Util.isLollipop()) {
                float elevation = Util.dpToPx(3);
                ViewCompat.setElevation(mSetDefaultCard, 0);
                mSetDefaultCard
                        .animate()
                        .withLayer()
                        .z(elevation)
                        .translationZ(elevation)
                        .start();
            }
        } else
            mSetDefaultCard.setVisibility(View.GONE);
    }

    private void setupSwitches() {
        updatePrefetchIfPermissionGranted();
        setupCheckBoxes();

        final boolean preFetch = Preferences.preFetch(mAppContext);
        final boolean warmUpBrowser = Preferences.warmUp(mAppContext);
        final boolean mergeTabs = Preferences.mergeTabs(mAppContext);

        mWarmUpSwitch.setChecked(preFetch || warmUpBrowser);
        mWarmUpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.warmUp(mAppContext, isChecked);
                ServiceUtil.takeCareOfServices(mAppContext);
            }
        });

        mPrefetchSwitch.setChecked(preFetch);
        enableDisableWarmUpSwitch(preFetch);
        updateSubPreferences(preFetch);
        mPrefetchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean warmUp = !isChecked && Preferences.warmUp(mAppContext);

                if (!Util.isAccessibilityServiceEnabled(mAppContext)) {
                    mPrefetchSwitch.setChecked(false);
                    guideUserToAccessibilitySettings();
                } else {
                    mWarmUpSwitch.setChecked(!warmUp);
                    Preferences.warmUp(mAppContext, warmUp);
                    enableDisableWarmUpSwitch(isChecked);
                }
                Preferences.preFetch(mAppContext, isChecked);

                if (!isChecked) {
                    // Since pre fetch is not active, the  warm up preference should properly reflect what's on the
                    // UI, hence setting the preference to the checked value of the warm up switch.
                    Preferences.warmUp(mAppContext, mWarmUpSwitch.isChecked());

                    // Ask user to revoke accessibility permission
                    Toast.makeText(mAppContext, R.string.revoke_accessibility_permission, Toast.LENGTH_LONG).show();
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                }

                ServiceUtil.takeCareOfServices(mAppContext);
                updateSubPreferences(isChecked);
            }
        });

        mMergeSwitch.setChecked(mergeTabs);
        mMergeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.mergeTabs(mAppContext, isChecked);
            }
        });
    }

    private void setupCheckBoxes() {
        mWifiCheckBox.setChecked(Preferences.wifiOnlyPrefetch(getActivity()));
        mWifiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.wifiOnlyPrefetch(mAppContext, isChecked);
                ServiceUtil.takeCareOfServices(mAppContext);
            }
        });

        mNotificationCheckBox.setChecked(Preferences.preFetchNotification(getActivity()));
        mNotificationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.preFetchNotification(mAppContext, isChecked);
            }
        });
    }

    private void updateSubPreferences(boolean isChecked) {
        if (isChecked && Util.isAccessibilityServiceEnabled(getActivity())) {
            mWifiCheckBox.setEnabled(true);
            mWifiCheckBox.setChecked(Preferences.wifiOnlyPrefetch(getActivity()));
            mNotificationCheckBox.setEnabled(true);
            mNotificationCheckBox.setChecked(Preferences.preFetchNotification(getActivity()));
        } else {
            mWifiCheckBox.setEnabled(false);
            mWifiCheckBox.setChecked(false);
            mNotificationCheckBox.setEnabled(false);
            mNotificationCheckBox.setChecked(false);
        }
    }

    private void enableDisableWarmUpSwitch(boolean isChecked) {
        if (isChecked) {
            mWarmUpSwitch.setEnabled(false);
        } else {
            mWarmUpSwitch.setEnabled(true);
        }
    }

    private void updatePrefetchIfPermissionGranted() {
        if (Util.isAccessibilityServiceEnabled(getActivity())) {
            Timber.d("Scanning permission granted");
            if (mPrefetchSwitch != null)
                mPrefetchSwitch.setChecked(Preferences.preFetch(mAppContext));
        } else {
            // Turn off preference
            if (mPrefetchSwitch != null)
                mPrefetchSwitch.setChecked(false);
            Preferences.preFetch(mAppContext, false);
        }
    }

    private void guideUserToAccessibilitySettings() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.accessibility_dialog_title)
                .content(R.string.accessibility_dialog_desc)
                .positiveText(R.string.open_settings)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
                    }
                })
                .show();
    }

    @OnClick(R.id.customtab_preference_view)
    public void onDefaultProviderClick() {
        if (mListener != null) {
            mListener.onDefaultCustomTabProviderClick(mCustomTabPreferenceView);
        }
    }

    @OnClick(R.id.browser_preference_view)
    public void onSecondaryBrowserPreferenceClicked() {
        if (mListener != null) {
            mListener.onSecondaryBrowserClick(mBrowserPreferenceView);
        }
    }

    @OnClick(R.id.favshare_preference_view)
    public void onFavSharePreferenceClicked() {
        if (mListener != null) {
            mListener.onFavoriteShareAppClick(mFavSharePreferenceView);
        }
    }

    @OnClick(R.id.blacklisted_target)
    public void blacklistClick() {
        Intent blackList = new Intent(getActivity(), BlacklistManagerActivity.class);
        startActivity(blackList,
                ActivityOptions.makeCustomAnimation(getActivity(),
                        R.anim.slide_in_right_medium,
                        R.anim.slide_out_left_medium).toBundle()
        );
    }

    @OnClick(R.id.set_default_card)
    public void onSetDefaultClick() {
        final String defaultBrowser = Util.getDefaultBrowserPackage(getActivity());
        if (defaultBrowser.equalsIgnoreCase("android")
                || defaultBrowser.startsWith("org.cyanogenmod")) {
            // TODO Change this detection such that "if defaultBrowserPackage is not a compatible browser" condition is used
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_URL)));
        } else {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + defaultBrowser));
            Toast.makeText(getActivity(),
                    Util.getAppNameWithPackage(getActivity(), defaultBrowser)
                            + " "
                            + getString(R.string.default_clear_msg), Toast.LENGTH_LONG).show();
            startActivity(intent);
        }
    }

    public interface FragmentInteractionListener {
        void onDefaultCustomTabProviderClick(AppPreferenceCardView customTabPreferenceCard);

        void onSecondaryBrowserClick(AppPreferenceCardView browserPreferenceCard);

        void onFavoriteShareAppClick(AppPreferenceCardView favShareAppPreferenceCard);
    }

}
