package arun.com.chromer.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.fragments.AboutFragment;
import arun.com.chromer.model.App;

/**
 * Created by Arun on 24/01/2016.
 */
public class SecondaryBrowserAdapter extends ExtendedBaseAdapter {
    List<App> mApps = new ArrayList<>();

    Context mContext;

    public SecondaryBrowserAdapter(Context context, List<App> apps) {
        mApps = apps;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AboutFragment.ViewHolder holder;

        App app = mApps.get(position);
        if (convertView == null) {
            holder = new AboutFragment.ViewHolder();
            convertView = mInflater.inflate(R.layout.listitem_template, parent, false);
            holder.imageView = (ImageView) convertView.findViewById(R.id.about_row_item_image);
            holder.subtitle = (TextView) convertView.findViewById(R.id.about_app_subtitle);
            holder.title = (TextView) convertView.findViewById(R.id.about_app_title);
            convertView.setTag(holder);
        } else {
            holder = (AboutFragment.ViewHolder) convertView.getTag();
        }

        // We don't need subtitle for browsers
        holder.subtitle.setVisibility(View.GONE);

        if (app != null) {
            holder.title.setText(app.getLabel());
            if (app.getAppIcon() != null)
                holder.imageView.setBackground(app.getAppIcon());
            else
                holder.imageView.setBackground(new IconicsDrawable(mContext)
                        .icon(GoogleMaterial.Icon.gmd_android)
                        .color(ContextCompat.getColor(mContext, R.color.accent))
                        .sizeDp(24));
        } else {
            // Shouldn't happen
            holder.title.setText("");
            holder.subtitle.setText("");
            holder.imageView.setBackground(null);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mApps.size();
    }
}
