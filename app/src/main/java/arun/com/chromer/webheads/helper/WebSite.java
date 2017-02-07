package arun.com.chromer.webheads.helper;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.chimbori.crux.Article;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Created by Arun on 05/09/2016.
 */
public class WebSite implements Parcelable {
    public String title;
    public String url;
    public String faviconUrl;
    public String canonicalUrl;
    public String themeColor;
    public int extractedColor = NO_COLOR;
    public String ampUrl;

    public WebSite() {

    }

    protected WebSite(Parcel in) {
        title = in.readString();
        url = in.readString();
        faviconUrl = in.readString();
        canonicalUrl = in.readString();
        themeColor = in.readString();
        extractedColor = in.readInt();
        ampUrl = in.readString();
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

    @NonNull
    public static WebSite fromArticle(@NonNull Article article) {
        final WebSite webSite = new WebSite();
        webSite.title = article.title;
        webSite.url = article.url;
        webSite.canonicalUrl = article.canonicalUrl != null && !article.canonicalUrl.isEmpty() ? article.canonicalUrl : article.url;
        webSite.faviconUrl = article.faviconUrl;
        webSite.themeColor = article.themeColor;
        return webSite;
    }

    @NonNull
    public String preferredUrl() {
        return canonicalUrl != null && !canonicalUrl.isEmpty() ? canonicalUrl : url;
    }

    @NonNull
    public String safeLabel() {
        return title != null && !title.isEmpty() ? title : preferredUrl();
    }

    @ColorInt
    public int themeColor() {
        try {
            return Color.parseColor(themeColor);
        } catch (Exception e) {
            return NO_COLOR;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebSite webSite = (WebSite) o;
        return url.equals(webSite.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return "WebSite{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", canonicalUrl='" + canonicalUrl + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(faviconUrl);
        dest.writeString(canonicalUrl);
        dest.writeString(themeColor);
        dest.writeInt(extractedColor);
        dest.writeString(ampUrl);
    }
}

