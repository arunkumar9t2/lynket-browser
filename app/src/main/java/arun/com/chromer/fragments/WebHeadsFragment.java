package arun.com.chromer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arun.com.chromer.R;
import arun.com.chromer.preferences.WebHeadPreferenceFragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Arun on 19/06/2016.
 */
public class WebHeadsFragment extends Fragment {
    private Unbinder unbinder;

    public static WebHeadsFragment newInstance() {
        WebHeadsFragment fragment = new WebHeadsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.web_head_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.webhead_container, WebHeadPreferenceFragment.newInstance())
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
