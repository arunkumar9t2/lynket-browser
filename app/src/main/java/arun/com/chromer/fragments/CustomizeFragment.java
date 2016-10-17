package arun.com.chromer.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.LinkedList;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.preferences.BottomBarPreferenceFragment;
import arun.com.chromer.preferences.PersonalizationPreferenceFragment;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Arun on 19/06/2016.
 */
public class CustomizeFragment extends Fragment {
    private Unbinder mUnbinder;

    @BindView(R.id.bottom_bar_action_list)
    public RecyclerView mBottomActionsList;

    public static CustomizeFragment newInstance() {
        CustomizeFragment fragment = new CustomizeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customize_fragment, container, false);
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
        mBottomActionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mBottomActionsList.setAdapter(new BottomActionsAdapter(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public static class BottomActionsAdapter extends RecyclerView.Adapter<BottomActionsAdapter.BottomActionHolder> {
        private static final String NEW_TAB = "NEW_TAB";
        private static final String SHARE = "SHARE";
        private static final String MINIMIZE = "MINIMIZE";
        private final Context mContext;
        private final List<String> mItems = new LinkedList<>();

        BottomActionsAdapter(Context context) {
            mContext = context;
            if (Util.isLollipopAbove()) {
                mItems.add(NEW_TAB);
            }
            mItems.add(SHARE);
            mItems.add(MINIMIZE);
        }

        @Override
        public BottomActionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BottomActionHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_action_template, parent, false));
        }

        @Override
        public void onBindViewHolder(BottomActionHolder holder, int position) {
            final int iconColor = ContextCompat.getColor(mContext, R.color.colorAccentLighter);
            switch (mItems.get(position)) {
                case NEW_TAB:
                    holder.icon.setImageDrawable(new IconicsDrawable(mContext)
                            .icon(CommunityMaterial.Icon.cmd_plus_box)
                            .color(iconColor)
                            .sizeDp(18));
                    holder.action.setText(html(R.string.open_in_new_tab_explanation));
                    break;
                case SHARE:
                    holder.icon.setImageDrawable(new IconicsDrawable(mContext)
                            .icon(CommunityMaterial.Icon.cmd_share_variant)
                            .color(iconColor)
                            .sizeDp(18));
                    holder.action.setText(html(R.string.share_action_explanation));
                    break;
                case MINIMIZE:
                    holder.icon.setImageDrawable(new IconicsDrawable(mContext)
                            .icon(CommunityMaterial.Icon.cmd_flip_to_back)
                            .color(iconColor)
                            .sizeDp(18));
                    holder.action.setText(html(R.string.minimize_action_explanation));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @NonNull
        private Spanned html(@StringRes int res) {
            final String string = mContext.getString(res);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
            } else {
                //noinspection deprecation
                return Html.fromHtml(string);
            }
        }

        class BottomActionHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.bottom_action)
            TextView action;
            @BindView(R.id.bottom_icon)
            ImageView icon;

            BottomActionHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
