package arun.com.chromer.webheads.helper;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import arun.com.chromer.webheads.ui.BaseWebHead;

/**
 * Created by Arun on 05/09/2016.
 */

public class WebSite implements Parcelable {
    public String title;
    public String url;
    public Bitmap icon;
    public String faviconUrl;
    public String longUrl;
    @ColorInt
    public int color;

    public WebSite() {

    }

    public static WebSite fromWebHead(@NonNull BaseWebHead webHead) {
        final WebSite webSite = new WebSite();
        webSite.title = webHead.getTitle();
        webSite.url = webHead.getUrl();
        webSite.longUrl = webHead.getUnShortenedUrl();
        webSite.faviconUrl = webHead.getFaviconUrl();
        webSite.color = webHead.getWebHeadColor(true);
        return webSite;
    }

    private WebSite(Parcel in) {
        title = in.readString();
        url = in.readString();
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        faviconUrl = in.readString();
        longUrl = in.readString();
        color = in.readInt();
    }

    public static final Creator<WebSite> CREATOR = new Creator<WebSite>() {
        @Override
        public WebSite createFromParcel(Parcel in) {
            return new WebSite(in);
        }

        @Override
        public WebSite[] newArray(int size) {
            return new WebSite[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeParcelable(icon, flags);
        dest.writeString(faviconUrl);
        dest.writeString(longUrl);
        dest.writeInt(color);
    }

    @Override
    public String toString() {
        return "WebSite{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", icon=" + icon +
                ", faviconUrl='" + faviconUrl + '\'' +
                ", longUrl='" + longUrl + '\'' +
                ", color=" + color +
                '}';
    }
}

