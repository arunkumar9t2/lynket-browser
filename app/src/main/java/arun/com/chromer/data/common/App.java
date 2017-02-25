package arun.com.chromer.data.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

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
}
