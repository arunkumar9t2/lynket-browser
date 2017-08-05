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

package arun.com.chromer.activities.main.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SearchEvent;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.activities.Snackable;
import arun.com.chromer.activities.base.BaseFragment;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.customtabs.CustomTabs;
import arun.com.chromer.data.website.WebsiteRepository;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.di.components.FragmentComponent;
import arun.com.chromer.search.SuggestionItem;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.RxUtils;
import arun.com.chromer.util.Utils;
import arun.com.chromer.views.searchview.MaterialSearchView;
import arun.com.chromer.webheads.WebHeadService;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static arun.com.chromer.shared.Constants.REQUEST_CODE_VOICE;

/**
 * Created by Arunkumar on 07-04-2017.
 */
public class HomeFragment extends BaseFragment<Home.View, Home.Presenter> implements Home.View {

    @BindView(R.id.material_search_view)
    MaterialSearchView materialSearchView;
    @BindView(R.id.incognito_mode)
    CheckBox incognitoMode;
    @BindView(R.id.recent_missing_text)
    TextView recentMissingText;
    @BindView(R.id.recents_list)
    RecyclerView recentsList;
    @BindView(R.id.recents_header)
    TextView recentsHeader;


    private CustomTabManager customTabManager;

    @Inject
    RecentsAdapter recentsAdapter;

    @NonNull
    @Override
    public Home.Presenter createPresenter() {
        return new Home.Presenter();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMaterialSearch();
        setupCustomTab();
        setupRecents();
    }

    @Override
    public void onStart() {
        super.onStart();
        customTabManager.bindCustomTabsService(getActivity());
    }

    @Override
    protected void inject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateState();
        getActivity().setTitle(R.string.app_name);
    }


    @Override
    public void onStop() {
        super.onStop();
        customTabManager.unbindCustomTabsService(getActivity());
    }


    @Override
    public void snack(@NonNull String message) {
        ((Snackable) getActivity()).snack(message);
    }

    @Override
    public void snackLong(@NonNull String message) {
        ((Snackable) getActivity()).snackLong(message);
    }

    @Override
    public void setSuggestions(@NonNull List<SuggestionItem> suggestions) {
        materialSearchView.setSuggestions(suggestions);
    }

    @Override
    public void setRecents(@NonNull List<WebSite> webSites) {
        recentsAdapter.setWebSites(webSites);
        if (webSites.isEmpty()) {
            recentMissingText.setVisibility(VISIBLE);
        } else {
            recentMissingText.setVisibility(GONE);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_home;
    }

    private void setupMaterialSearch() {
        materialSearchView.clearFocus();
        presenter.registerSearch(materialSearchView.getEditText());
        materialSearchView.setInteractionListener(new MaterialSearchView.SearchViewInteractionListener() {
            @Override
            public void onVoiceIconClick() {
                Answers.getInstance().logSearch(new SearchEvent().putCustomAttribute("Mode", "Voice"));
                if (Utils.isVoiceRecognizerPresent(getActivity())) {
                    startActivityForResult(Utils.getRecognizerIntent(getActivity()), REQUEST_CODE_VOICE);
                } else {
                    snack(getString(R.string.no_voice_rec_apps));
                }
            }

            @Override
            public void onSearchPerformed(@NonNull String url) {
                launchCustomTab(url);
                Answers.getInstance().logSearch(new SearchEvent());
            }
        });
    }

    private void invalidateState() {
        incognitoMode.setChecked(Preferences.get(getContext()).incognitoMode());
        incognitoMode.setCompoundDrawablePadding(Utils.dpToPx(5));
        incognitoMode.setCompoundDrawables(new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_incognito)
                .colorRes(R.color.material_dark_color)
                .sizeDp(24), null, null, null);
        incognitoMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                Preferences.get(getContext()).incognitoMode(isChecked));
        presenter.loadRecents(getContext());
    }

    private void setupRecents() {
        recentsHeader.setCompoundDrawablePadding(Utils.dpToPx(22));
        recentsHeader.setCompoundDrawables(new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_history)
                .colorRes(R.color.accent)
                .sizeDp(20), null, null, null);
        recentsList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        recentsList.setAdapter(recentsAdapter);
    }

    private void launchCustomTab(@Nullable String url) {
        if (url != null) {
            if (Preferences.get(getContext()).webHeads()) {
                if (Utils.isOverlayGranted(getContext())) {
                    final Intent webHeadService = new Intent(getContext(), WebHeadService.class);
                    webHeadService.setData(Uri.parse(url));
                    getActivity().startService(webHeadService);
                } else {
                    Utils.openDrawOverlaySettings(getActivity());
                }
            } else {
                CustomTabs.from(getActivity())
                        .forUrl(url)
                        .withSession(customTabManager.getSession())
                        .prepare()
                        .launch();
                WebsiteRepository.getInstance(getActivity())
                        .getWebsite(url)
                        .compose(RxUtils.applySchedulers())
                        .doOnError(Timber::e)
                        .subscribe();
            }
        }
    }

    private void setupCustomTab() {
        customTabManager = new CustomTabManager();
        customTabManager.setConnectionCallback(
                new CustomTabManager.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Timber.d("Connected to custom tabs");
                        try {
                            customTabManager.mayLaunchUrl(Uri.parse(Constants.GOOGLE_URL), null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VOICE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    final List<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultList != null && !resultList.isEmpty()) {
                        launchCustomTab(Utils.getSearchUrl(resultList.get(0)));
                    }
                    break;
                default:
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @OnClick(R.id.fab)
    public void onFabClick() {
        if (materialSearchView.hasFocus() && materialSearchView.getText().length() > 0) {
            launchCustomTab(materialSearchView.getURL());
        } else {
            launchCustomTab(Constants.GOOGLE_URL);
        }
    }
}
