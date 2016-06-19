package arun.com.chromer.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.blacklist.BlacklistManagerActivity;
import arun.com.chromer.preferences.PrefetchPreferenceFragment;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.preferences.widgets.AppPreferenceCardView;
import arun.com.chromer.shared.Constants;
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
    @BindView(R.id.merge_tabs_switch)
    public SwitchCompat mMergeSwitch;
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
        mSetDefaultIcon.setImageDrawable(new IconicsDrawable(mAppContext)
                .icon(GoogleMaterial.Icon.gmd_new_releases)
                .color(ContextCompat.getColor(mAppContext, R.color.colorAccentText))
                .sizeDp(30));
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.prefetch_fragment_container, PrefetchPreferenceFragment.newInstance())
                .commit();
        setupSwitches();
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
        final boolean mergeTabs = Preferences.mergeTabs(mAppContext);
        mMergeSwitch.setChecked(mergeTabs);
        mMergeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.mergeTabs(mAppContext, isChecked);
            }
        });
    }

    @OnClick(R.id.customtab_preference_view)
    public void onDefaultProviderClick() {
        mListener.onDefaultCustomTabProviderClick(mCustomTabPreferenceView);
    }

    @OnClick(R.id.browser_preference_view)
    public void onSecondaryBrowserPreferenceClicked() {
        mListener.onSecondaryBrowserClick(mBrowserPreferenceView);
    }

    @OnClick(R.id.favshare_preference_view)
    public void onFavSharePreferenceClicked() {
        mListener.onFavoriteShareAppClick(mFavSharePreferenceView);
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
