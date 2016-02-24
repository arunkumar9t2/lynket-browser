package arun.com.chromer.views.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.model.App;

/**
 * Created by Arun on 24/01/2016.
 */
public class BlackListAppRender extends RecyclerView.Adapter<BlackListAppRender.ViewHolder> {
    private final Context mContext;
    private final List<App> apps;
    private ItemClickListener externalListener;
    private static Drawable placeHolder;

    public BlackListAppRender(Context mContext, List<App> apps) {
        this.apps = apps;
        this.mContext = mContext;
        this.placeHolder = new IconicsDrawable(mContext)
                .icon(GoogleMaterial.Icon.gmd_android)
                .color(ContextCompat.getColor(mContext, R.color.accent_icon_nofocus))
                .sizeDp(48);
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.externalListener = listener;
    }

    public void add(int position, App item) {
        apps.add(position, item);
        notifyItemInserted(position);
    }

    @Override
    public BlackListAppRender.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.applist_item_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        App currApp = apps.get(position);
        holder.title.setText(currApp.getAppName());
        // holder.subtitle.setText(currApp.getPackageName());
        Drawable icon = null;
        try {
            icon = mContext.getPackageManager().getApplicationIcon(currApp.getPackageName());
            holder.icon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            holder.icon.setImageDrawable(placeHolder);
        }
        holder.checkBox.setChecked(currApp.isBlackListed());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public interface ItemClickListener {
        void onClick(int position, App app, boolean checked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView subtitle;
        public ImageView icon;
        public AppCompatCheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.app_list_name);
            subtitle = (TextView) view.findViewById(R.id.app_list_package);
            // TODO Reconsider showing package
            subtitle.setVisibility(View.GONE);

            icon = (ImageView) view.findViewById(R.id.app_list_icon);
            checkBox = (AppCompatCheckBox) view.findViewById(R.id.app_list_checkbox);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            App app = apps.get(position);
            app.setBlackListed(checkBox.isChecked());
            if (externalListener != null) {
                externalListener.onClick(position, app, checkBox.isChecked());
            }
        }
    }

}