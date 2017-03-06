package arun.com.chromer.data.website.model;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.chimbori.crux.articles.Article;

import arun.com.chromer.data.history.model.HistoryTable;
import xyz.klinker.android.article.data.webarticle.model.WebArticle;

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
    public String ampUrl;
    public boolean bookmarked;
    public long createdAt;
    public int count;

    public WebSite() {

    }

    public WebSite(@NonNull String url) {
        this.url = url;
    }

    protected WebSite(Parcel in) {
        title = in.readString();
        url = in.readString();
        faviconUrl = in.readString();
        canonicalUrl = in.readString();
        themeColor = in.readString();
        ampUrl = in.readString();
        bookmarked = in.readByte() != 0;
        createdAt = in.readLong();
        count = in.readInt();
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
        webSite.canonicalUrl = !TextUtils.isEmpty(article.canonicalUrl) ? article.canonicalUrl : article.url;
        webSite.faviconUrl = article.faviconUrl;
        webSite.themeColor = article.themeColor;
        webSite.ampUrl = !TextUtils.isEmpty(article.ampUrl) ? article.ampUrl : webSite.canonicalUrl;
        return webSite;
    }

    @NonNull
    public static WebSite fromArticle(@NonNull WebArticle article) {
        final WebSite webSite = new WebSite();
        webSite.title = article.title;
        webSite.url = article.url;
        webSite.canonicalUrl = !TextUtils.isEmpty(article.canonicalUrl) ? article.canonicalUrl : article.url;
        webSite.faviconUrl = article.faviconUrl;
        webSite.themeColor = article.themeColor;
        webSite.ampUrl = !TextUtils.isEmpty(article.ampUrl) ? article.ampUrl : webSite.canonicalUrl;
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
        dest.writeString(ampUrl);
        dest.writeByte((byte) (bookmarked ? 1 : 0));
        dest.writeLong(createdAt);
        dest.writeInt(count);
    }

    @Override
    public String toString() {
        return "WebSite{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", faviconUrl='" + faviconUrl + '\'' +
                ", canonicalUrl='" + canonicalUrl + '\'' +
                ", themeColor='" + themeColor + '\'' +
                ", ampUrl='" + ampUrl + '\'' +
                ", bookmarked=" + bookmarked +
                ", createdAt=" + createdAt +
                ", count=" + count +
                '}';
    }

    public static WebSite fromCursor(@NonNull Cursor cursor) {
        final WebSite webSite = new WebSite();
        webSite.title = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_TITLE));
        webSite.url = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_URL));
        webSite.faviconUrl = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_FAVICON));
        webSite.canonicalUrl = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_CANONICAL));
        webSite.themeColor = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_COLOR));
        webSite.ampUrl = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_AMP));
        webSite.bookmarked = cursor.getInt(cursor.getColumnIndex(HistoryTable.COLUMN_BOOKMARKED)) == 1;
        webSite.createdAt = cursor.getLong(cursor.getColumnIndex(HistoryTable.COLUMN_CREATED_AT));
        webSite.count = cursor.getInt(cursor.getColumnIndex(HistoryTable.COLUMN_VISITED));
        return webSite;
    }
}

