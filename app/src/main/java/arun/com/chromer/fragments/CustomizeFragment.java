package arun.com.chromer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arun.com.chromer.R;
import arun.com.chromer.preferences.BottomBarPreferenceFragment;
import arun.com.chromer.preferences.PersonalizationPreferenceFragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Arun on 19/06/2016.
 */
public class CustomizeFragment extends Fragment {
    private Context mAppContext;
    private Unbinder mUnbinder;

    public static CustomizeFragment newInstance() {
        CustomizeFragment fragment = new CustomizeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAppContext = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.customize_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_container, PersonalizationPreferenceFragment.newInstance())
                .replace(R.id.bottom_bar_container, BottomBarPreferenceFragment.newInstance())
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
