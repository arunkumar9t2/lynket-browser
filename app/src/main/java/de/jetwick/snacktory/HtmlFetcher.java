/*
 *  Copyright 2011 Peter Karich 
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetwick.snacktory;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import timber.log.Timber;

/**
 * Class to fetch articles. This class is thread safe.
 *
 * @author Peter Karich
 */
@SuppressWarnings({"WeakerAccess", "UnusedParameters"})
public class HtmlFetcher {
    private static final int TIMEOUT = 10000;

    static {
        SHelper.enableCookieMgmt();
        SHelper.enableUserAgentOverwrite();
        SHelper.enableAnySSL();
    }

    private final AtomicInteger cacheCounter = new AtomicInteger(0);
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Set<String> furtherResolveNecessary = new LinkedHashSet<String>() {
        {
            add("bit.ly");
            add("cli.gs");
            add("deck.ly");
            add("fb.me");
            add("feedproxy.google.com");
            add("flic.kr");
            add("fur.ly");
            add("goo.gl");
            add("is.gd");
            add("ink.co");
            add("j.mp");
            add("lnkd.in");
            add("on.fb.me");
            add("ow.ly");
            add("plurl.us");
            add("sns.mx");
            add("snurl.com");
            add("su.pr");
            add("t.co");
            add("tcrn.ch");
            add("tl.gd");
            add("tiny.cc");
            add("tinyurl.com");
            add("tmi.me");
            add("tr.im");
            add("twurl.nl");
        }
    };
    private String referrer = "Chromer";
    private String userAgent = "Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";
    private String cacheControl = "max-age=0";
    private String language = "en-us";
    private String accept = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
    private String charset = "UTF-8";
    private SCache cache;
    private Proxy proxy = null;
    private int maxTextLength = -1;
    private ArticleTextExtractor extractor = new ArticleTextExtractor();

    public HtmlFetcher() {
    }

    private static String fixUrl(String url, String urlOrPath) {
        return SHelper.useDomainOfFirstArg4Second(url, urlOrPath);
    }

    /**
     * Takes a URI that was decoded as ISO-8859-1 and applies percent-encoding
     * to non-ASCII characters. Workaround for broken origin servers that send
     * UTF-8 in the Location: header.
     */
    static String encodeUriFromHeader(String badLocation) {
        StringBuilder sb = new StringBuilder();

        for (char ch : badLocation.toCharArray()) {
            if (ch < (char) 128) {
                sb.append(ch);
            } else {
                // this is ONLY valid if the uri was decoded using ISO-8859-1
                sb.append(String.format("%%%02X", (int) ch));
            }
        }

        return sb.toString();
    }

    public ArticleTextExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(ArticleTextExtractor extractor) {
        this.extractor = extractor;
    }

    public SCache getCache() {
        return cache;
    }

    public HtmlFetcher setCache(SCache cache) {
        this.cache = cache;
        return this;
    }

    public int getCacheCounter() {
        return cacheCounter.get();
    }

    public HtmlFetcher clearCacheCounter() {
        cacheCounter.set(0);
        return this;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public HtmlFetcher setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReferrer() {
        return referrer;
    }

    public HtmlFetcher setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Proxy getProxy() {
        return (proxy != null ? proxy : Proxy.NO_PROXY);
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public boolean isProxySet() {
        return getProxy() != null;
    }

    @SuppressLint("CustomWarning")
    @SuppressWarnings({"SameParameterValue", "StatementWithEmptyBody"})
    public JResult fetchAndExtract(String url, boolean resolve) throws Exception {
        String originalUrl = url;
        url = SHelper.removeHashbang(url);
        String gUrl = SHelper.getUrlFromUglyGoogleRedirect(url);
        if (gUrl != null)
            url = gUrl;
        else {
            gUrl = SHelper.getUrlFromUglyFacebookRedirect(url);
            if (gUrl != null)
                url = gUrl;
        }

        if (resolve) url = unShortenUrl(url);

        final JResult result = new JResult();
        // or should we use? <link rel="canonical" href="http://www.N24.de/news/newsitem_6797232.html"/>
        result.setUrl(url);
        result.setOriginalUrl(originalUrl);
        result.setDate(SHelper.estimateDate(url));

        String lowerUrl = url.toLowerCase();
        if (SHelper.isDoc(lowerUrl) || SHelper.isApp(lowerUrl) || SHelper.isPackage(lowerUrl)) {
            // skip
        } else if (SHelper.isVideo(lowerUrl) || SHelper.isAudio(lowerUrl)) {
            result.setVideoUrl(url);
        } else if (SHelper.isImage(lowerUrl)) {
            result.setImageUrl(url);
        } else {
            extractor.extractContent(result, fetchAsString(url));

            if (result.getFaviconUrl().isEmpty())
                result.setFaviconUrl(SHelper.getDefaultFavicon(url));

            // some links are relative to root and do not include the domain of the url :(
            result.setFaviconUrl(fixUrl(url, result.getFaviconUrl()));
            result.setImageUrl(fixUrl(url, result.getImageUrl()));
            result.setVideoUrl(fixUrl(url, result.getVideoUrl()));
            result.setRssUrl(fixUrl(url, result.getRssUrl()));
        }
        result.setText(lessText(result.getText()));
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (result) {
            result.notifyAll();
        }
        return result;
    }

    public String lessText(String text) {
        if (text == null)
            return "";

        if (maxTextLength >= 0 && text.length() > maxTextLength)
            return text.substring(0, maxTextLength);

        return text;
    }

    public String fetchAsString(String urlAsString)
            throws IOException {
        return fetchAsString(urlAsString, TIMEOUT, false);
    }

    @SuppressWarnings("SameParameterValue")
    public String fetchAsString(String urlAsString, int timeout, boolean includeSomeGooseOptions)
            throws IOException {
        HttpURLConnection connection = createUrlConnection(urlAsString, timeout, false);
        connection.setInstanceFollowRedirects(true);
        connection.connect();

        String encoding = connection.getContentEncoding();
        InputStream is;
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(connection.getInputStream());
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            is = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
        } else {
            is = connection.getInputStream();
        }

        String enc = Converter.extractEncoding(connection.getContentType());
        return createConverter(urlAsString).streamToString(is, enc);
    }

    public Converter createConverter(String url) {
        return new Converter(url);
    }

    public String unShortenUrl(String url) {
        final int maxRedirects = 5;
        String unShortenedUrl = url;
        for (int i = 0; i < maxRedirects; i++) {
            url = getRedirectUrl(url);
            Timber.d("Redirect: %s", url);
            if (url.equalsIgnoreCase(unShortenedUrl)) {
                return unShortenedUrl;
            }
            unShortenedUrl = url;
        }
        return unShortenedUrl;
    }

    @NonNull  // https://github.com/GermainZ/CrappaLinks
    private String getRedirectUrl(@NonNull final String url) {
        HttpURLConnection conn = null;
        try {
            conn = createUrlConnection(url, 10000, false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("HEAD");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 300 && responseCode < 400) {
                return fixUrl(url, conn.getHeaderField("Location"));
            } else if (responseCode >= 200 && responseCode < 300) {
                final String html = fetchAsString(url);
                final Document doc = Jsoup.parse(html);
                final Elements refresh = doc.select("meta[http-equiv=Refresh]");
                if (!refresh.isEmpty()) {
                    final Element refreshElement = refresh.first();
                    if (refreshElement.hasAttr("url"))
                        return fixUrl(url, refreshElement.attr("url"));
                    else if (refreshElement.hasAttr("content") && refreshElement.attr("content").contains("URL="))
                        return fixUrl(url, refreshElement.attr("content").split("URL=")[1].replaceAll("^'|'$", ""));
                    else if (refreshElement.hasAttr("content") && refreshElement.attr("content").contains("url="))
                        return fixUrl(url, refreshElement.attr("content").split("url=")[1].replaceAll("^'|'$", ""));
                }
            }
        } catch (Exception ex) {
            return url;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return url;
    }

    @SuppressWarnings("SameParameterValue")
    protected HttpURLConnection createUrlConnection(String urlAsStr, int timeout, boolean includeSomeGooseOptions) throws IOException {
        URL url = new URL(urlAsStr);
        Proxy proxy = getProxy();
        HttpURLConnection hConn = (HttpURLConnection) url.openConnection(proxy);
        hConn.setRequestProperty("User-Agent", userAgent);
        hConn.setRequestProperty("Accept", accept);

        if (includeSomeGooseOptions) {
            hConn.setRequestProperty("Accept-Language", language);
            hConn.setRequestProperty("content-charset", charset);
            hConn.addRequestProperty("Referer", referrer);
            // avoid the cache for testing purposes only?
            hConn.setRequestProperty("Cache-Control", cacheControl);
        }

        // suggest respond to be gzipped or deflated (which is just another compression)
        // http://stackoverflow.com/q/3932117
        hConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        hConn.setConnectTimeout(timeout);
        hConn.setReadTimeout(timeout);
        return hConn;
    }
}
