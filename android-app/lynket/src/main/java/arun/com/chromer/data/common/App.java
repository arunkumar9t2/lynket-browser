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

package arun.com.chromer.data.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Comparator;

import arun.com.chromer.shared.Constants;

/**
 * Created by Arun on 24/01/2016.
 */
public class App implements Parcelable {
  public static final Creator<App> CREATOR = new Creator<App>() {
    @Override
    public App createFromParcel(Parcel in) {
      return new App(in);
    }

    @Override
    public App[] newArray(int size) {
      return new App[size];
    }
  };
  @NonNull
  public String appName;
  @NonNull
  public String packageName;
  public boolean blackListed;
  public boolean incognito;
  @ColorInt
  public int color = Constants.NO_COLOR;

  public App() {

  }

  public App(@NonNull String appName, @NonNull String packageName, boolean blackListed, boolean incognito, int color) {
    this.appName = appName;
    this.packageName = packageName;
    this.blackListed = blackListed;
    this.incognito = incognito;
    this.color = color;
  }

  protected App(Parcel in) {
    appName = in.readString();
    packageName = in.readString();
    blackListed = in.readByte() != 0;
    incognito = in.readByte() != 0;
    color = in.readInt();
  }

  private static int blackListIncognitoAwareComparison(@Nullable App lhs, @Nullable App rhs) {
    final String lhsName = lhs != null ? lhs.appName : null;
    final String rhsName = rhs != null ? rhs.appName : null;

    boolean lhsValueSet = lhs != null && (lhs.blackListed || lhs.incognito);
    boolean rhsValueSet = rhs != null && (rhs.blackListed || rhs.incognito);

    if (lhsValueSet ^ rhsValueSet) return (lhsValueSet) ? -1 : 1;
    if (lhsName == null ^ rhsName == null) return lhs == null ? -1 : 1;
    if (lhsName == null && rhsName == null) return 0;
    return lhsName.compareToIgnoreCase(rhsName);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(appName);
    dest.writeString(packageName);
    dest.writeByte((byte) (blackListed ? 1 : 0));
    dest.writeByte((byte) (incognito ? 1 : 0));
    dest.writeInt(color);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    App app = (App) o;

    if (blackListed != app.blackListed) return false;
    if (incognito != app.incognito) return false;
    if (color != app.color) return false;
    if (!appName.equals(app.appName)) return false;
    return packageName.equals(app.packageName);
  }

  @Override
  public int hashCode() {
    int result = appName.hashCode();
    result = 31 * result + packageName.hashCode();
    result = 31 * result + (blackListed ? 1 : 0);
    result = 31 * result + (incognito ? 1 : 0);
    result = 31 * result + color;
    return result;
  }

  @Override
  public String toString() {
    return "App{" +
      "appName='" + appName + '\'' +
      ", packageName='" + packageName + '\'' +
      ", blackListed=" + blackListed +
      ", incognito=" + incognito +
      ", color=" + color +
      '}';
  }

  public static class PerAppListComparator implements Comparator<App> {

    @Override
    public int compare(App lhs, App rhs) {
      return blackListIncognitoAwareComparison(lhs, rhs);
    }
  }
}
