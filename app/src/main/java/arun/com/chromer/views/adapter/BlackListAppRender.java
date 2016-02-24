package arun.com.chromer.views.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.util.Util;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.IOException;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.model.App;

/**
 * Created by Arun on 24/01/2016.
 */
public class BlackListAppRender extends RecyclerView.Adapter<BlackListAppRender.ViewHolder> {
    private final Context mContext;
    private final List<App> apps;
    private ItemClickListener mExternalListenere;
    private static Drawable mPlaceholder;

    public BlackListAppRender(Context mContext, List<App> apps) {
        this.apps = apps;
        this.mContext = mContext;
        mPlaceholder = new IconicsDrawable(mContext)
                .icon(GoogleMaterial.Icon.gmd_android)
                .color(ContextCompat.getColor(mContext, R.color.accent))
                .sizeDp(48);
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.mExternalListenere = listener;
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
        holder.checkBox.setChecked(currApp.isBlackListed());

        ApplicationInfo info;
        try {
            info = mContext.getPackageManager().getApplicationInfo(currApp.getPackageName(), 0);
            Glide.with(mContext)
                    .using(new PassThroughModelLoader<ApplicationInfo>(), ApplicationInfo.class)
                    .from(ApplicationInfo.class)
                    .as(Drawable.class)
                    .placeholder(mPlaceholder)
                    .decoder(new ApplicationIconDecoder(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(info)
                    .into(holder.icon);
        } catch (Exception e) {
            holder.icon.setImageDrawable(mPlaceholder);
        }
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
            if (mExternalListenere != null) {
                mExternalListenere.onClick(position, app, checkBox.isChecked());
            }
        }
    }

    // see https://groups.google.com/forum/#!topic/glidelibrary/MAqPfuHpjr4
    class PassThroughModelLoader<T> implements ModelLoader<T, T> {
        @Override
        public DataFetcher<T> getResourceFetcher(final T model, int width, int height) {
            return new DataFetcher<T>() {
                @Override
                public T loadData(Priority priority) throws Exception {
                    return model;
                }

                @Override
                public void cleanup() {
                }

                @Override
                public String getId() {
                    return "PassThroughDataFetcherApplicationInfo";
                }

                @Override
                public void cancel() {
                }
            };
        }
    }

    // https://groups.google.com/forum/#!topic/glidelibrary/MAqPfuHpjr4
    class ApplicationIconDecoder implements ResourceDecoder<ApplicationInfo, Drawable> {
        private final Context context;
        private ApplicationInfo source;

        public ApplicationIconDecoder(Context context) {
            this.context = context;
        }

        @Override
        public Resource<Drawable> decode(ApplicationInfo source, int width, int height) throws IOException {
            Drawable icon = context.getPackageManager().getApplicationIcon(source);
            this.source = source;
            return new DrawableResource<Drawable>(icon) {
                @Override
                public int getSize() {
                    if (drawable instanceof BitmapDrawable) {
                        return Util.getBitmapByteSize(((BitmapDrawable) drawable).getBitmap());
                    } else {
                        return 1;
                    }
                }

                @Override
                public void recycle() {
                }
            };
        }

        @Override
        public String getId() {
            return String.valueOf(Math.random());
        }
    }

}