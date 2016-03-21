package arun.com.chromer.fragments;

import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.util.ChangelogUtil;
import arun.com.chromer.util.Licenses;
import arun.com.chromer.util.StringConstants;
import arun.com.chromer.util.Util;
import arun.com.chromer.views.adapter.ExtendedBaseAdapter;
import de.psdev.licensesdialog.LicensesDialog;

/**
 * Created by Arun on 11/11/2015.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_fragment, container, false);

        setupCredits(rootView);
        populateData(rootView);
        return rootView;
    }

    private void setupCredits(View rootView) {
        View daniel = rootView.findViewById(R.id.daniel);
        daniel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent danielProfile = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://plus.google.com/u/0/+DanielCiao/about"));
                getActivity().startActivity(danielProfile);
            }
        });

        View patryk = rootView.findViewById(R.id.patryk);
        patryk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent patrykProfile = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://plus.google.com/u/0/109304801957014561872/about"));
                getActivity().startActivity(patrykProfile);
            }
        });
    }

    private void populateData(View rootView) {
        ListView chromerList = (ListView) rootView.findViewById(R.id.about_app_version_list);
        ListView authorList = (ListView) rootView.findViewById(R.id.about_author_version_list);

        // Loading the header
        chromerList.setAdapter(new ExtendedBaseAdapter() {
            final Context context = getActivity().getApplicationContext();

            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = mInflater.inflate(R.layout.fragment_about_listview_template, parent, false);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.about_row_item_image);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.about_app_subtitle);
                    holder.title = (TextView) convertView.findViewById(R.id.about_app_title);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                int materialdarkColor = ContextCompat.getColor(context, R.color.accent);
                holder.subtitle.setVisibility(View.VISIBLE);
                switch (position) {
                    case 0:
                        holder.title.setText(R.string.version);
                        holder.subtitle.setText(Util.getPackageVersion(context));
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_info_outline)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 1:
                        holder.title.setText(R.string.changelog);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_track_changes)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 2:
                        holder.title.setText(R.string.join_google_plus);
                        holder.subtitle.setText(R.string.share_ideas);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_google_circles_communities)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 3:
                        holder.title.setText(R.string.licenses);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_card_membership)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 4:
                        holder.title.setText(R.string.translations);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_translate)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                }
                return convertView;
            }
        });

        authorList.setAdapter(new ExtendedBaseAdapter() {
            final Context context = getActivity().getApplicationContext();

            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = mInflater.inflate(R.layout.fragment_about_listview_template, parent, false);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.about_row_item_image);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.about_app_subtitle);
                    holder.title = (TextView) convertView.findViewById(R.id.about_app_title);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                switch (position) {
                    case 0:
                        holder.title.setText(StringConstants.ME);
                        holder.subtitle.setText(StringConstants.LOCATION);
                        holder.imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.arun_height);
                        holder.imageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.arun_width);
                        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arun);
                        RoundedBitmapDrawable roundedBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        roundedBitmapDrawable.setAntiAlias(true);
                        roundedBitmapDrawable.setCircular(true);
                        holder.imageView.setImageDrawable(roundedBitmapDrawable);
                        break;
                    case 1:
                        holder.title.setText(R.string.add_to_circles);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_google_circles)
                                .color(ContextCompat.getColor(getActivity(), R.color.google_plus))
                                .sizeDp(24));
                        break;
                    case 2:
                        holder.title.setText(R.string.follow_twitter);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_twitter)
                                .color(ContextCompat.getColor(getActivity(), R.color.twitter))
                                .sizeDp(24));
                        break;
                    case 3:
                        holder.title.setText(R.string.connect_linkedIn);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_linkedin_box)
                                .color(ContextCompat.getColor(getActivity(), R.color.linkedin))
                                .sizeDp(24));
                        break;
                    case 4:
                        holder.title.setText(R.string.more_apps);
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_google_play)
                                .color(ContextCompat.getColor(getActivity(), R.color.playstore_green))
                                .sizeDp(24));
                        break;
                }
                return convertView;
            }
        });

        chromerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        return;
                    case 1:
                        ChangelogUtil.showChangelogDialog(getActivity());
                        break;
                    case 2:
                        Intent communityIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://plus.google.com/communities/109754631011301174504"));
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
                        Intent oneSkyIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://os0l2aw.oneskyapp.com/collaboration/project/62112"));
                        getActivity().startActivity(oneSkyIntent);
                        break;
                }
            }
        });

        authorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                        Intent linkedinIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://in.linkedin.com/in/arunkumar9t2"));
                        getActivity().startActivity(linkedinIntent);
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
        });

    }

    public static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ImageView imageView;
    }
}
