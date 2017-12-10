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

package arun.com.chromer.activities.about;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;
import arun.com.chromer.activities.about.changelog.Changelog;
import arun.com.chromer.shared.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Arun on 11/11/2015.
 */
public class AboutFragment extends Fragment {

    @BindView(R.id.about_app_version_list)
    public RecyclerView chromerList;
    @BindView(R.id.about_author_version_list)
    public RecyclerView authorList;
    private Unbinder unBinder;

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        final AboutFragment fragment = new AboutFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        unBinder = ButterKnife.bind(this, rootView);
        populateData();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unBinder.unbind();
    }

    @OnClick(R.id.patryk)
    public void patrtykClick() {
        Intent patrykProfile = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/109304801957014561872/about"));
        getActivity().startActivity(patrykProfile);
    }

    @OnClick(R.id.daniel)
    public void danielClick() {
        Intent danielProfile = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+DanielCiao/about"));
        getActivity().startActivity(danielProfile);
    }

    private void populateData() {
        chromerList.setNestedScrollingEnabled(false);
        chromerList.setLayoutManager(new LinearLayoutManager(getContext()));
        chromerList.setAdapter(new AppAdapter());
        authorList.setNestedScrollingEnabled(false);
        authorList.setLayoutManager(new LinearLayoutManager(getContext()));
        authorList.setAdapter(new AuthorAdapter());
    }

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.ItemHolder> {
        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_about_list_item_template, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            int materialDark = ContextCompat.getColor(getActivity(), R.color.accent);
            holder.subtitle.setVisibility(View.VISIBLE);
            switch (position) {
                case 0:
                    holder.title.setText(R.string.version);
                    holder.subtitle.setText(BuildConfig.VERSION_NAME);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_information_outline)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 1:
                    holder.title.setText(R.string.changelog);
                    holder.subtitle.setText(R.string.see_whats_new);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_chart_line)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 2:
                    holder.title.setText(R.string.join_google_plus);
                    holder.subtitle.setText(R.string.share_ideas);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_google_circles_communities)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 3:
                    holder.title.setText(R.string.licenses);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_wallet_membership)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 4:
                    holder.title.setText(R.string.translations);
                    holder.subtitle.setText(R.string.help_translations);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_translate)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 5:
                    holder.title.setText(R.string.source);
                    holder.subtitle.setText(R.string.contribute_to_chromer);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_source_branch)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 6;
        }

        class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.about_row_item_image)
            public ImageView imageView;
            @BindView(R.id.about_app_subtitle)
            public TextView subtitle;
            @BindView(R.id.about_app_title)
            public TextView title;

            ItemHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                switch (position) {
                    case 0:
                        return;
                    case 1:
                        Changelog.show(getActivity());
                        break;
                    case 2:
                        Intent communityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/109754631011301174504"));
                        getActivity().startActivity(communityIntent);
                        Answers.getInstance().logCustom(new CustomEvent("Join Community"));
                        break;
                    case 3:
                        Intent licenses = new Intent(Intent.ACTION_VIEW, Uri.parse("http://htmlpreview.github.com/?https://github.com/arunkumar9t2/chromer/blob/master/notices.html"));
                        getActivity().startActivity(licenses);
                        Answers.getInstance().logCustom(new CustomEvent("Licenses"));
                        break;
                    case 4:
                        Intent oneSkyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://os0l2aw.oneskyapp.com/collaboration/project/62112"));
                        getActivity().startActivity(oneSkyIntent);
                        Answers.getInstance().logCustom(new CustomEvent("Translations"));
                        break;
                    case 5:
                        Intent sourceIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/arunkumar9t2/chromer"));
                        getActivity().startActivity(sourceIntent);
                        Answers.getInstance().logCustom(new CustomEvent("Source clicked"));
                        break;
                }
            }
        }
    }

    class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.ItemHolder> {
        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_about_list_item_template, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            switch (position) {
                case 0:
                    holder.title.setText(Constants.ME);
                    holder.subtitle.setText(Constants.LOCATION);
                    holder.imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.arun_height);
                    holder.imageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.arun_width);
                    final Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arun);
                    final RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                    roundedBitmapDrawable.setAntiAlias(true);
                    roundedBitmapDrawable.setCircular(true);
                    holder.imageView.setImageDrawable(roundedBitmapDrawable);
                    break;
                case 1:
                    holder.title.setText(R.string.add_to_circles);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_google_circles)
                            .color(ContextCompat.getColor(getActivity(), R.color.google_plus))
                            .sizeDp(24));
                    break;
                case 2:
                    holder.title.setText(R.string.follow_twitter);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_twitter)
                            .color(ContextCompat.getColor(getActivity(), R.color.twitter))
                            .sizeDp(24));
                    break;
                case 3:
                    holder.title.setText(R.string.connect_linkedIn);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_linkedin_box)
                            .color(ContextCompat.getColor(getActivity(), R.color.linkedin))
                            .sizeDp(24));
                    break;
                case 4:
                    holder.title.setText(R.string.fork_on_github);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_github_circle)
                            .color(Color.BLACK)
                            .sizeDp(24));
                    break;
                case 5:
                    holder.title.setText(R.string.more_apps);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_google_play)
                            .color(ContextCompat.getColor(getActivity(), R.color.play_store_green))
                            .sizeDp(24));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 6;
        }

        class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.about_row_item_image)
            public ImageView imageView;
            @BindView(R.id.about_app_subtitle)
            public TextView subtitle;
            @BindView(R.id.about_app_title)
            public TextView title;

            public ItemHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                switch (position) {
                    case 0:
                        return;
                    case 1:
                        Intent myProfile = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com/+arunkumar5592"));
                        getActivity().startActivity(myProfile);
                        Answers.getInstance().logCustom(new CustomEvent("Google+"));
                        break;
                    case 2:
                        Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/arunkumar_9t2"));
                        getActivity().startActivity(twitterIntent);
                        Answers.getInstance().logCustom(new CustomEvent("Twitter"));
                        break;
                    case 3:
                        Intent linkedInIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://in.linkedin.com/in/arunkumar9t2"));
                        getActivity().startActivity(linkedInIntent);
                        Answers.getInstance().logCustom(new CustomEvent("LinkedIn"));
                        break;
                    case 4:
                        Intent github = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/arunkumar9t2/"));
                        getActivity().startActivity(github);
                        Answers.getInstance().logCustom(new CustomEvent("GitHub"));
                        break;
                    case 5:
                        try {
                            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Arunkumar")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Arunkumar")));
                        }
                        Answers.getInstance().logCustom(new CustomEvent("More apps"));
                        break;
                }
            }
        }
    }

    public static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ImageView imageView;
    }
}
