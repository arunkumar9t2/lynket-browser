package arun.com.chromer.fragments;

import android.content.Context;
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
import arun.com.chromer.adapter.ExtendedBaseAdapter;
import arun.com.chromer.util.ChangelogUtil;
import arun.com.chromer.util.Util;

/**
 * Created by Arun on 11/11/2015.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance(int arg) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_fragment, container, false);

        populateData(rootView);
        return rootView;
    }

    private void populateData(View rootView) {
        ListView chromerList = (ListView) rootView.findViewById(R.id.about_app_version_list);
        ListView authorList = (ListView) rootView.findViewById(R.id.about_author_version_list);

        // Loading the header
        chromerList.setAdapter(new ExtendedBaseAdapter() {
            final Context context = getActivity().getApplicationContext();

            @Override
            public int getCount() {
                return 4;
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
                int materialdarkColor = ContextCompat.getColor(context, R.color.material_dark_color);
                holder.subtitle.setVisibility(View.VISIBLE);
                switch (position) {
                    case 0:
                        holder.title.setText("Version");
                        holder.subtitle.setText(Util.getPackageVersion(context));
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_info_outline)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 1:
                        holder.title.setText("Changelog");
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_track_changes)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 2:
                        holder.title.setText("Join Google+ Community");
                        holder.subtitle.setText("Share your ideas");
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_google_circles_communities)
                                .color(materialdarkColor)
                                .sizeDp(24));
                        break;
                    case 3:
                        holder.title.setText("Support Development");
                        holder.subtitle.setText("Consider a donation");
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(GoogleMaterial.Icon.gmd_favorite)
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
                        holder.title.setText("Arunkumar");
                        holder.subtitle.setText("Tamilnadu, India");
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
                        holder.title.setText("Add to Google+ Circles");
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_google_circles)
                                .color(Color.parseColor("#dc4e41"))
                                .sizeDp(24));
                        break;
                    case 2:
                        holder.title.setText("Follow on Twitter");
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_twitter)
                                .color(Color.parseColor("#55acee"))
                                .sizeDp(24));
                        break;
                    case 3:
                        holder.title.setText("Connect on LinkedIn");
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_linkedin_box)
                                .color(Color.parseColor("#0077b5"))
                                .sizeDp(24));
                        break;
                    case 4:
                        holder.title.setText("More apps");
                        holder.subtitle.setVisibility(View.GONE);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_google_play)
                                .color(Color.parseColor("#0f9d58"))
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
                        Intent googleIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://plus.google.com/communities/109754631011301174504"));
                        getActivity().startActivity(googleIntent);
                        break;
                    case 3:
                        // Donation!!!
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
                        Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com/+arunkumar5592"));
                        getActivity().startActivity(googleIntent);
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
