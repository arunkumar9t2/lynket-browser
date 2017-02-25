package arun.com.chromer.data.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;

/**
 * Created by Arun on 24/01/2016.
 */
public class App implements Parcelable {
    @NonNull
    public String appName;
    @NonNull
    public String packageName;
    public boolean blackListed;
    @ColorInt
    public int color = -1;

    public App() {

    }

    public App(@NonNull String appName, @NonNull String packageName, boolean blackListed) {
        this.appName = appName;
        this.packageName = packageName;
        this.blackListed = blackListed;
    }

    protected App(Parcel in) {
        appName = in.readString();
        packageName = in.readString();
        blackListed = in.readByte() != 0;
        color = in.readInt();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return packageName.equals(app.packageName);

    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
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
        dest.writeInt(color);
    }

    private static int blackListAwareComparison(@Nullable App lhs, @Nullable App rhs) {
        final String lhsName = lhs != null ? lhs.appName : null;
        final String rhsName = rhs != null ? rhs.appName : null;

        boolean lhsBlacklist = lhs != null && lhs.blackListed;
        boolean rhsBlacklist = rhs != null && rhs.blackListed;

        if (lhsBlacklist ^ rhsBlacklist) return (lhsBlacklist) ? -1 : 1;
        if (lhsName == null ^ rhsName == null) return lhs == null ? -1 : 1;
        //noinspection ConstantConditions
        if (lhsName == null && rhsName == null) return 0;

        return lhsName.compareToIgnoreCase(rhsName);
    }

    public static class BlackListComparator implements Comparator<App> {

        @Override
        public int compare(App lhs, App rhs) {
            return blackListAwareComparison(lhs, rhs);
        }
    }
}
