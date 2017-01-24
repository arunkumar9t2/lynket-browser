package arun.com.chromer.activities.blacklist;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.activities.blacklist.model.App;
import arun.com.chromer.glide.AppIconModelLoader;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arun on 24/01/2016.
 */
class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.BlackListItemViewHolder> {
    private WeakReference<Activity> activityRef = new WeakReference<>(null);
    private final List<App> apps = new ArrayList<>();

    private BlackListItemClickedListener listener = new BlackListItemClickedListener() {
        @Override
        public void onBlackListItemClick(App app) {
            // no-op
        }
    };

    BlacklistAdapter(@NonNull Activity activity, @Nullable BlackListItemClickedListener listener) {
        this.activityRef = new WeakReference<>(activity);
        if (listener != null) {
            this.listener = listener;
        }
        setHasStableIds(true);
    }

    @Override
    public BlackListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BlackListItemViewHolder(LayoutInflater.from(activityRef.get()).inflate(R.layout.blacklist_applist_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(final BlackListItemViewHolder holder, int position) {
        final App currApp = apps.get(position);
        holder.appName.setText(currApp.getAppName());
        holder.appPackage.setText(currApp.getPackageName());
        holder.appCheckbox.setChecked(currApp.isBlackListed());

        if (activityRef.get() != null)
            Glide.with(activityRef.get())
                    .using(new AppIconModelLoader(activityRef.get()))
                    .load(currApp.getPackageName())
                    .crossFade()
                    .into(holder.appIcon);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    @Override
    public long getItemId(int position) {
        return apps.get(position).getPackageName().hashCode();
    }

    public void setApps(@NonNull List<App> apps) {
        this.apps.clear();
        this.apps.addAll(apps);
        notifyDataSetChanged();
    }

    void cleanUp() {
        if (activityRef != null) {
            activityRef.clear();
            activityRef = null;
        }
    }

    interface BlackListItemClickedListener {
        void onBlackListItemClick(App app);
    }

    class BlackListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.app_list_icon)
        ImageView appIcon;
        @BindView(R.id.app_list_name)
        TextView appName;
        @BindView(R.id.app_list_package)
        TextView appPackage;
        @BindView(R.id.app_list_checkbox)
        AppCompatCheckBox appCheckbox;
        @BindView(R.id.blacklist_template_root)
        LinearLayout blacklistTemplateRoot;

        BlackListItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            appPackage.setVisibility(View.GONE);
            appCheckbox.setOnClickListener(this);
            blacklistTemplateRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appCheckbox.performClick();
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                final App app = apps.get(position);
                app.setBlackListed(appCheckbox.isChecked());
                listener.onBlackListItemClick(app);
            }
        }
    }
}