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

package arun.com.chromer.data.webarticle.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.chimbori.crux.articles.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arun.com.chromer.shared.Constants;

/**
 * Parcelable clone of {@link Article}. Not fully complete though, there
 * is a limitation of not being able to parcel {@link Elements} from JSoup. This implementation has
 * partial work around for this by storing the said {@link Elements} as plain string and then during
 * unmarshalling we get the original elements list by running Jsoup over it. This process is considerably
 * faster than parsing the whole HTML content of the page.
 */
public class WebArticle implements Parcelable {
  public static final Creator<WebArticle> CREATOR = new Creator<WebArticle>() {
    @Override
    public WebArticle createFromParcel(Parcel in) {
      return new WebArticle(in);
    }

    @Override
    public WebArticle[] newArray(int size) {
      return new WebArticle[size];
    }
  };
  public String url = "";
  public String originalUrl = "";
  public String title = "";
  public String description = "";
  public String siteName = "";
  public String themeColor = "";
  public String ampUrl = "";
  public String canonicalUrl = "";
  public String imageUrl = "";
  public String videoUrl = "";
  public String feedUrl = "";
  public String faviconUrl = "";
  public List<String> keywords = new ArrayList<>();
  public Elements elements;

  public WebArticle() {

  }

  public WebArticle(@NonNull String url) {
    this.url = url;
  }

  protected WebArticle(Parcel in) {
    url = in.readString();
    originalUrl = in.readString();
    title = in.readString();
    description = in.readString();
    siteName = in.readString();
    themeColor = in.readString();
    ampUrl = in.readString();
    canonicalUrl = in.readString();
    imageUrl = in.readString();
    videoUrl = in.readString();
    feedUrl = in.readString();
    faviconUrl = in.readString();

    final String html = in.readString();
    final Elements rawElements = Jsoup.parse(html).body().children();
    for (Iterator<Element> iterator = rawElements.iterator(); iterator.hasNext(); ) {
      final Element element = iterator.next();
      if (element.text().isEmpty()) {
        iterator.remove();
      }
    }
    elements = rawElements;

    keywords = in.createStringArrayList();
  }

  @NonNull
  public static WebArticle fromArticle(@NonNull Article article) {
    final WebArticle webArticle = new WebArticle();
    webArticle.url = article.url;
    webArticle.originalUrl = article.originalUrl;
    webArticle.title = article.title;
    webArticle.description = article.description;
    webArticle.siteName = article.siteName;
    webArticle.themeColor = article.themeColor;
    webArticle.ampUrl = article.ampUrl;
    webArticle.canonicalUrl = article.canonicalUrl;
    webArticle.imageUrl = article.imageUrl;
    webArticle.videoUrl = article.videoUrl;
    webArticle.feedUrl = article.feedUrl;
    webArticle.faviconUrl = article.faviconUrl;
    webArticle.elements = article.document.children();
    webArticle.keywords = new ArrayList<>();
    if (article.keywords != null) {
      webArticle.keywords.addAll(article.keywords);
    }
    return webArticle;
  }

  @Override
  public int describeContents() {
    return 0;
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
      //noinspection Range
      return Color.parseColor(themeColor);
    } catch (Exception e) {
      return Constants.NO_COLOR;
    }
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(url);
    dest.writeString(originalUrl);
    dest.writeString(title);
    dest.writeString(description);
    dest.writeString(siteName);
    dest.writeString(themeColor);
    dest.writeString(ampUrl);
    dest.writeString(canonicalUrl);
    dest.writeString(imageUrl);
    dest.writeString(videoUrl);
    dest.writeString(feedUrl);
    dest.writeString(faviconUrl);
    dest.writeString(elements.toString());
    dest.writeStringList(keywords);
  }
}
