/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.shared.views;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arun.com.chromer.util.Utils;
import flipboard.bottomsheet.commons.R;

@SuppressLint("ViewConstructor")
public class IntentPickerSheetView extends FrameLayout {

  private final Intent intent;
  private final GridView appGrid;
  @SuppressWarnings("FieldCanBeLocal")
  private final TextView titleView;
  private final List<ActivityInfo> mixins = new ArrayList<>();
  private Adapter adapter;
  private Filter filter = new FilterNone();
  private Comparator<ActivityInfo> sortMethod = new SortAlphabetically();
  private int columnWidthDp = 100;

  public IntentPickerSheetView(Context context, Intent intent, @StringRes int titleRes, OnIntentPickedListener listener) {
    this(context, intent, context.getString(titleRes), listener);
  }

  @SuppressWarnings("WeakerAccess")
  public IntentPickerSheetView(Context context, final Intent intent, final String title, final OnIntentPickedListener listener) {
    super(context);
    this.intent = intent;

    inflate(context, R.layout.grid_sheet_view, this);
    appGrid = findViewById(R.id.grid);
    titleView = findViewById(R.id.title);

    titleView.setText(title);
    appGrid.setOnItemClickListener((parent, view, position, id) -> listener.onIntentPicked(adapter.getItem(position)));

    ViewCompat.setElevation(this, Utils.dpToPx(16));
  }

  @NonNull
  public static Filter selfPackageExcludeFilter(@NonNull final Context context) {
    return info -> !info.componentName.getPackageName().equalsIgnoreCase(context.getPackageName());
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    for (ActivityInfo activityInfo : adapter.activityInfos) {
      if (activityInfo.iconLoadTask != null) {
        activityInfo.iconLoadTask.cancel(true);
        activityInfo.iconLoadTask = null;
      }
    }
  }

  public void setSortMethod(Comparator<ActivityInfo> sortMethod) {
    this.sortMethod = sortMethod;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setColumnWidthDp(int columnWidthDp) {
    this.columnWidthDp = columnWidthDp;
  }

  public List<ActivityInfo> getMixins() {
    return this.mixins;
  }

  /**
   * Adds custom mixins to the resulting picker sheet
   *
   * @param infos Custom ActivityInfo classes to mix in
   */
  public void setMixins(@NonNull List<ActivityInfo> infos) {
    mixins.clear();
    mixins.addAll(infos);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    this.adapter = new Adapter(getContext(), intent, mixins);
    appGrid.setAdapter(this.adapter);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    final float density = getResources().getDisplayMetrics().density;
    getResources().getDimensionPixelSize(R.dimen.bottomsheet_default_sheet_width);
    appGrid.setNumColumns((int) (width / (columnWidthDp * density)));
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    setOutlineProvider(new Utils.ShadowOutline(w, h));
  }

  public interface Filter {
    boolean include(ActivityInfo info);
  }

  public interface OnIntentPickedListener {
    void onIntentPicked(ActivityInfo activityInfo);
  }

  /**
   * Represents an item in the picker grid
   */
  public static class ActivityInfo {
    public final String label;
    public final ComponentName componentName;
    public final ResolveInfo resolveInfo;
    public Drawable icon;
    public Object tag;
    private AsyncTask<Void, Void, Drawable> iconLoadTask;

    public ActivityInfo(Drawable icon, String label, Context context, Class<?> clazz) {
      this.icon = icon;
      resolveInfo = null;
      this.label = label;
      this.componentName = new ComponentName(context, clazz.getName());
    }

    public ActivityInfo(ResolveInfo resolveInfo, CharSequence label, ComponentName componentName) {
      this.resolveInfo = resolveInfo;
      this.label = label.toString();
      this.componentName = componentName;
    }

    public Intent getConcreteIntent(Intent intent) {
      Intent concreteIntent = new Intent(intent);
      concreteIntent.setComponent(componentName);
      return concreteIntent;
    }
  }

  private static class SortAlphabetically implements Comparator<ActivityInfo> {
    @Override
    public int compare(ActivityInfo lhs, ActivityInfo rhs) {
      return lhs.label.compareTo(rhs.label);
    }
  }

  private static class FilterNone implements Filter {
    @Override
    public boolean include(ActivityInfo info) {
      return true;
    }
  }

  private class Adapter extends BaseAdapter {

    final List<ActivityInfo> activityInfos;
    final LayoutInflater inflater;
    private final PackageManager packageManager;

    public Adapter(Context context, Intent intent, List<ActivityInfo> mixins) {
      inflater = LayoutInflater.from(context);
      packageManager = context.getPackageManager();
      @SuppressLint("InlinedApi") List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
      activityInfos = new ArrayList<>(infos.size() + mixins.size());
      activityInfos.addAll(mixins);
      for (ResolveInfo info : infos) {
        ComponentName componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        ActivityInfo activityInfo = new ActivityInfo(info, info.loadLabel(packageManager), componentName);
        if (filter.include(activityInfo)) {
          activityInfos.add(activityInfo);
        }
      }
      Collections.sort(activityInfos, sortMethod);
    }

    @Override
    public int getCount() {
      return activityInfos.size();
    }

    @Override
    public ActivityInfo getItem(int position) {
      return activityInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
      return activityInfos.get(position).componentName.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      final ViewHolder holder;

      if (convertView == null) {
        convertView = inflater.inflate(R.layout.sheet_grid_item, parent, false);
        holder = new ViewHolder(convertView);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      final ActivityInfo info = activityInfos.get(position);
      if (info.iconLoadTask != null) {
        info.iconLoadTask.cancel(true);
        info.iconLoadTask = null;
      }
      if (info.icon != null) {
        holder.icon.setImageDrawable(info.icon);
      } else {
        //noinspection deprecation
        holder.icon.setImageDrawable(getResources().getDrawable(R.color.divider_gray));
        info.iconLoadTask = new AsyncTask<Void, Void, Drawable>() {
          @Override
          protected Drawable doInBackground(@NonNull Void... params) {
            return info.resolveInfo.loadIcon(packageManager);
          }

          @Override
          protected void onPostExecute(@NonNull Drawable drawable) {
            info.icon = drawable;
            info.iconLoadTask = null;
            holder.icon.setImageDrawable(drawable);
          }
        };
        info.iconLoadTask.execute();
      }
      holder.label.setText(info.label);

      return convertView;
    }

    class ViewHolder {
      final ImageView icon;
      final TextView label;

      ViewHolder(View root) {
        icon = root.findViewById(R.id.icon);
        label = root.findViewById(R.id.label);
      }
    }

  }
}
