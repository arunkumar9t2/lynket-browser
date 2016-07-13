package arun.com.chromer.activities.about;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;
import arun.com.chromer.activities.about.changelog.Changelog;
import arun.com.chromer.activities.about.licenses.Licenses;
import arun.com.chromer.shared.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.psdev.licensesdialog.LicensesDialog;

/**
 * Created by Arun on 11/11/2015.
 */
public class AboutFragment extends Fragment {

    @BindView(R.id.about_app_version_list)
    public RecyclerView mChromerList;
    @BindView(R.id.about_author_version_list)
    public RecyclerView mAuthorList;
    private Unbinder mUnBinder;

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
        mUnBinder = ButterKnife.bind(this, rootView);
        populateData();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
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
        mChromerList.setNestedScrollingEnabled(false);
        mChromerList.setLayoutManager(new LinearLayoutManager(getContext()));
        mChromerList.setAdapter(new AppAdapter());
        mAuthorList.setNestedScrollingEnabled(false);
        mAuthorList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuthorList.setAdapter(new AuthorAdapter());
    }

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.ItemHolder> {
        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.about_fragment_listview_template, parent, false);
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
                            .icon(GoogleMaterial.Icon.gmd_info_outline)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 1:
                    holder.title.setText(R.string.changelog);
                    holder.subtitle.setText(R.string.see_whats_new);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(GoogleMaterial.Icon.gmd_track_changes)
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
                            .icon(GoogleMaterial.Icon.gmd_card_membership)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
                case 4:
                    holder.title.setText(R.string.translations);
                    holder.subtitle.setText(R.string.help_translations);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(GoogleMaterial.Icon.gmd_translate)
                            .color(materialDark)
                            .sizeDp(24));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 5;
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
                        break;
                    case 3:
                        new LicensesDialog.Builder(getActivity())
                                .setNotices(Licenses.getNotices())
                                .setTitle(R.string.licenses)
                                .build()
                                .showAppCompat();
                        break;
                    case 4:
                        Intent oneSkyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://os0l2aw.oneskyapp.com/collaboration/project/62112"));
                        getActivity().startActivity(oneSkyIntent);
                        break;
                }
            }
        }
    }

    class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.ItemHolder> {
        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.about_fragment_listview_template, parent, false);
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
                    holder.title.setText(R.string.more_apps);
                    holder.subtitle.setVisibility(View.GONE);
                    holder.imageView.setBackground(new IconicsDrawable(getActivity())
                            .icon(CommunityMaterial.Icon.cmd_google_play)
                            .color(ContextCompat.getColor(getActivity(), R.color.playstore_green))
                            .sizeDp(24));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 5;
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
                        break;
                    case 2:
                        Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/arunkumar_9t2"));
                        getActivity().startActivity(twitterIntent);
                        break;
                    case 3:
                        Intent linkedInIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://in.linkedin.com/in/arunkumar9t2"));
                        getActivity().startActivity(linkedInIntent);
                        break;
                    case 4:
                        try {
                            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Arunkumar")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Arunkumar")));
                        }
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
