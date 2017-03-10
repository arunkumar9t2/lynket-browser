package arun.com.chromer.activities.settings.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.browsingoptions.BottomBarPreferenceFragment;
import arun.com.chromer.activities.settings.browsingoptions.BrowsingOptionsActivity;
import arun.com.chromer.activities.settings.lookandfeel.PersonalizationPreferenceFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Arun on 19/06/2016.
 */
public class CustomizeFragment extends Fragment {
    private Unbinder mUnbinder;

    @BindView(R.id.bottom_bar_action_list)
    public RecyclerView recyclerView;

    public static CustomizeFragment newInstance() {
        CustomizeFragment fragment = new CustomizeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customize, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_container, PersonalizationPreferenceFragment.newInstance())
                .replace(R.id.bottom_bar_container, BottomBarPreferenceFragment.newInstance())
                .commit();
        initBottomActions();
    }

    private void initBottomActions() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new BrowsingOptionsActivity.BottomActionsAdapter(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

}
