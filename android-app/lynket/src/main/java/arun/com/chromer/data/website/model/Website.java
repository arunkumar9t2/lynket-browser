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

package arun.com.chromer.data.website.model;

import static arun.com.chromer.shared.Constants.NO_COLOR;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.chimbori.crux.articles.Article;

import java.util.Objects;

import arun.com.chromer.data.history.model.HistoryTable;
import arun.com.chromer.data.webarticle.model.WebArticle;

/**
 * Created by Arun on 05/09/2016.
 */
public class Website implements Parcelable {
  public static final Creator<Website> CREATOR = new Creator<Website>() {
    @Override
    public Website createFromParcel(Parcel in) {
      return new Website(in);
    }

    @Override
    public Website[] newArray(int size) {
      return new Website[size];
    }
  };
  public static DiffUtil.ItemCallback<Website> DIFFER = new DiffUtil.ItemCallback<Website>() {
    @Override
    public boolean areItemsTheSame(@NonNull Website oldItem, @NonNull Website newItem) {
      return oldItem.url.equals(newItem.url);
    }

    @Override
    public boolean areContentsTheSame(@NonNull Website oldItem, @NonNull Website newItem) {
      return oldItem.equals(newItem);
    }
  };
  public String title;
  public String url;
  public String faviconUrl;
  public String canonicalUrl;
  public String themeColor;
  public String ampUrl;
  public boolean bookmarked;
  public long createdAt;
  public int count;

  public Website() {

  }

  public Website(@NonNull String url) {
    this.url = url;
  }

  public Website(String title, String url, String faviconUrl,
                 String canonicalUrl, String themeColor,
                 String ampUrl, boolean bookmarked,
                 long createdAt, int count) {
    this.title = title;
    this.url = url;
    this.faviconUrl = faviconUrl;
    this.canonicalUrl = canonicalUrl;
    this.themeColor = themeColor;
    this.ampUrl = ampUrl;
    this.bookmarked = bookmarked;
    this.createdAt = createdAt;
    this.count = count;
  }

  protected Website(Parcel in) {
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

  @NonNull
  public static Website Ampify(@NonNull Website from) {
    final Website website = new Website();
    website.title = from.title;
    final String ampUrl = from.hasAmp() ? from.ampUrl : from.url;
    website.url = ampUrl;
    website.ampUrl = ampUrl;
    website.canonicalUrl = ampUrl;
    website.faviconUrl = from.faviconUrl;
    website.themeColor = from.themeColor;
    website.bookmarked = from.bookmarked;
    website.count = from.count;
    website.createdAt = from.createdAt;
    return website;
  }

  @NonNull
  public static Website fromArticle(@NonNull Article article) {
    final Website website = new Website();
    website.title = article.title;
    website.url = article.url;
    website.canonicalUrl = !TextUtils.isEmpty(article.canonicalUrl) ? article.canonicalUrl : article.url;
    website.faviconUrl = article.faviconUrl;
    website.themeColor = article.themeColor;
    website.ampUrl = !TextUtils.isEmpty(article.ampUrl) ? article.ampUrl : "";
    return website;
  }

  @NonNull
  public static Website fromArticle(@NonNull WebArticle article) {
    final Website website = new Website();
    website.title = article.title;
    website.url = article.url;
    website.canonicalUrl = !TextUtils.isEmpty(article.canonicalUrl) ? article.canonicalUrl : article.url;
    website.faviconUrl = article.faviconUrl;
    website.themeColor = article.themeColor;
    website.ampUrl = !TextUtils.isEmpty(article.ampUrl) ? article.ampUrl : "";
    return website;
  }

  public static Website fromCursor(@NonNull Cursor cursor) {
    final Website website = new Website();
    website.title = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_TITLE));
    website.url = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_URL));
    website.faviconUrl = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_FAVICON));
    website.canonicalUrl = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_CANONICAL));
    website.themeColor = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_COLOR));
    website.ampUrl = cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_AMP));
    website.bookmarked = cursor.getInt(cursor.getColumnIndex(HistoryTable.COLUMN_BOOKMARKED)) == 1;
    website.createdAt = cursor.getLong(cursor.getColumnIndex(HistoryTable.COLUMN_CREATED_AT));
    website.count = cursor.getInt(cursor.getColumnIndex(HistoryTable.COLUMN_VISITED));
    return website;
  }

  public boolean hasAmp() {
    return !TextUtils.isEmpty(ampUrl);
  }

  @NonNull
  public String preferredUrl() {
    return url;
  }

  public Uri preferredUri() {
    return Uri.parse(preferredUrl());
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

    Website website = (Website) o;

    if (bookmarked != website.bookmarked) return false;
    if (createdAt != website.createdAt) return false;
    if (count != website.count) return false;
    if (!Objects.equals(title, website.title)) return false;
    if (!url.equals(website.url)) return false;
    if (!Objects.equals(faviconUrl, website.faviconUrl))
      return false;
    if (!Objects.equals(canonicalUrl, website.canonicalUrl))
      return false;
    if (!Objects.equals(themeColor, website.themeColor))
      return false;
    return Objects.equals(ampUrl, website.ampUrl);
  }

  @Override
  public int hashCode() {
    int result = title != null ? title.hashCode() : 0;
    result = 31 * result + url.hashCode();
    result = 31 * result + (faviconUrl != null ? faviconUrl.hashCode() : 0);
    result = 31 * result + (canonicalUrl != null ? canonicalUrl.hashCode() : 0);
    result = 31 * result + (themeColor != null ? themeColor.hashCode() : 0);
    result = 31 * result + (ampUrl != null ? ampUrl.hashCode() : 0);
    result = 31 * result + (bookmarked ? 1 : 0);
    result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
    result = 31 * result + count;
    return result;
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
    return "Website{" +
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

  public boolean matches(String url) {
    return url.equalsIgnoreCase(this.url)
      || url.equalsIgnoreCase(this.ampUrl)
      || url.equalsIgnoreCase(preferredUrl());
  }
}

