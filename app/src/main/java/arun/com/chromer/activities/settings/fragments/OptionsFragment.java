package arun.com.chromer.activities.settings.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.browsingoptions.BehaviorPreferenceFragment;
import arun.com.chromer.activities.settings.preferences.PrefetchPreferenceFragment;
import arun.com.chromer.activities.settings.widgets.AppPreferenceCardView;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class OptionsFragment extends Fragment {
    @BindView(R.id.customtab_preference_view)
    public AppPreferenceCardView customTabProviderView;
    @BindView(R.id.browser_preference_view)
    public AppPreferenceCardView browserPreferenceView;
    @BindView(R.id.favshare_preference_view)
    public AppPreferenceCardView favSharePreferenceView;
    @BindView(R.id.set_default_card)
    public CardView setDefaultCard;
    @BindView(R.id.set_default_image)
    public ImageView setDefaultIcon;

    private Unbinder unbinder;
    private Context context;
    private FragmentInteractionListener fragmentInteractionListener;

    public static OptionsFragment newInstance() {
        OptionsFragment fragment = new OptionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        setDefaultIcon.setImageDrawable(new IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_auto_fix)
                .color(ContextCompat.getColor(context, R.color.colorAccent))
                .sizeDp(30));
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.behaviour_fragment_container, BehaviorPreferenceFragment.newInstance())
                .replace(R.id.prefetch_fragment_container, PrefetchPreferenceFragment.newInstance())
                .commit();
        updateDefaultBrowserCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        context = null;
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
            fragmentInteractionListener = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement FragmentInteractionListener");
        }
        super.onAttach(context);
    }

    private void updateDefaultBrowserCard() {
        if (!Utils.isDefaultBrowser(getActivity())) {
            setDefaultCard.setVisibility(View.VISIBLE);
        } else
            setDefaultCard.setVisibility(View.GONE);
    }

    @OnClick(R.id.customtab_preference_view)
    public void onDefaultProviderClick() {
        fragmentInteractionListener.onDefaultCustomTabProviderClick(customTabProviderView);
    }

    @OnClick(R.id.browser_preference_view)
    public void onSecondaryBrowserPreferenceClicked() {
        fragmentInteractionListener.onSecondaryBrowserClick(browserPreferenceView);
    }

    @OnClick(R.id.favshare_preference_view)
    public void onFavSharePreferenceClicked() {
        fragmentInteractionListener.onFavoriteShareAppClick(favSharePreferenceView);
    }

    @OnClick(R.id.set_default_card)
    public void onSetDefaultClick() {
        final String defaultBrowser = Utils.getDefaultBrowserPackage(getActivity());
        if (defaultBrowser.equalsIgnoreCase("android")
                || defaultBrowser.startsWith("org.cyanogenmod")) {
            // TODO Change this detection such that "if defaultBrowserPackage is not a compatible browser" condition is used
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_URL)));
        } else {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + defaultBrowser));
            Toast.makeText(getActivity(),
                    Utils.getAppNameWithPackage(getActivity(), defaultBrowser)
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
