package arun.com.chromer.activities.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arunkumar on 19-02-2017.
 */
class SettingsGroupAdapter extends RecyclerView.Adapter<SettingsGroupAdapter.SettingsItemViewHolder> {
    private final Context context;
    private final List<String> settingsItems = new ArrayList<>();
    private GroupItemClickListener groupItemClickListener = new GroupItemClickListener() {
        @Override
        public void onGroupItemClicked(int position, View view) {
            // no-op
        }
    };

    SettingsGroupAdapter(@NonNull final Context context) {
        setHasStableIds(true);
        this.context = context.getApplicationContext();
        settingsItems.add(context.getString(R.string.settings_browsing_mode));
        settingsItems.add(context.getString(R.string.settings_look_and_feel));
        settingsItems.add(context.getString(R.string.settings_browsing_options));
    }

    @Override
    public SettingsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SettingsItemViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_settings_list_item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(final SettingsItemViewHolder holder, int position) {
        holder.bind(settingsItems.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() != RecyclerView.NO_POSITION)
                    groupItemClickListener.onGroupItemClicked(holder.getAdapterPosition(), holder.itemView);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return settingsItems.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return settingsItems.size();
    }

    void setGroupItemClickListener(@NonNull GroupItemClickListener groupItemClickListener) {
        this.groupItemClickListener = groupItemClickListener;
    }

    void cleanUp() {
        groupItemClickListener = new GroupItemClickListener() {
            @Override
            public void onGroupItemClicked(int position, View view) {

            }
        };
        settingsItems.clear();
    }

    public class SettingsItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.settins_list_icon)
        ImageView icon;
        @BindView(R.id.settings_list_title)
        TextView title;
        @BindView(R.id.settings_list_subtitle)
        TextView subtitle;

        SettingsItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(@NonNull final String item) {
            final int position = getAdapterPosition();
            title.setText(item);
            switch (position) {
                case 0:
                    icon.setImageDrawable(new IconicsDrawable(icon.getContext())
                            .icon(CommunityMaterial.Icon.cmd_earth)
                            .colorRes(R.color.colorAccent)
                            .sizeDp(24));
                    subtitle.setVisibility(View.GONE);
                    break;
                case 1:
                    icon.setImageDrawable(new IconicsDrawable(icon.getContext())
                            .icon(CommunityMaterial.Icon.cmd_format_paint)
                            .colorRes(R.color.colorAccent)
                            .sizeDp(24));
                    subtitle.setVisibility(View.GONE);
                    break;
                case 2:
                    icon.setImageDrawable(new IconicsDrawable(icon.getContext())
                            .icon(CommunityMaterial.Icon.cmd_settings)
                            .colorRes(R.color.colorAccent)
                            .sizeDp(24));
                    subtitle.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public interface GroupItemClickListener {
        void onGroupItemClicked(int position, final View view);
    }
}
