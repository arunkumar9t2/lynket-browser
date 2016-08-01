package arun.com.chromer.customtabs.callbacks;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import arun.com.chromer.R;
import arun.com.chromer.activities.BrowserInterceptActivity;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Util;
import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import timber.log.Timber;

public class AddHomeShortcutService extends IntentService {

    public AddHomeShortcutService() {
        super(AddHomeShortcutService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String urlToAdd = intent.getDataString();
            if (urlToAdd != null) {
                showToast(getString(R.string.add_home_screen_begun));

                HtmlFetcher fetcher = new HtmlFetcher();
                String unShortenedUrl = fetcher.unShortenUrl(urlToAdd);

                JResult res = extractWebsiteData(fetcher, unShortenedUrl);

                if (unShortenedUrl.length() == 0) unShortenedUrl = urlToAdd;

                if (res == null) {
                    legacyAdd(unShortenedUrl);
                    return;
                }

                String shortCutName = getShortcutName(unShortenedUrl, res);
                String faviconUrl = res.getFaviconUrl();
                Bitmap favicon = getFaviconBitmap(faviconUrl);

                if (favicon != null) {
                    if (!isValidFavicon(favicon)) {
                        Palette palette = Palette.from(favicon).generate();
                        int iconColor = ColorUtil.getBestFaviconColor(palette);
                        if (iconColor == -1) {
                            iconColor = ContextCompat.getColor(this, R.color.primary);
                        }
                        favicon = createIcon(iconColor, shortCutName);
                    }
                } else {
                    favicon = createIcon(ContextCompat.getColor(this, R.color.primary), shortCutName);
                }

                Timber.i("Creating shortcut: %s", shortCutName);

                Intent webIntent = getWebIntent(unShortenedUrl);

                Intent addIntent = new Intent(Constants.ACTION_INSTALL_SHORTCUT);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, webIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, favicon);

                sendBroadcast(addIntent);
                showToast(getString(R.string.added) + " " + shortCutName);
            }
        }
    }

    @Nullable
    private JResult extractWebsiteData(HtmlFetcher fetcher, String unShortenedUrl) {
        JResult res = null;
        try {
            res = fetcher.fetchAndExtract(unShortenedUrl, false);
        } catch (Exception ignored) {
        }
        return res;
    }

    private String getShortcutName(String unShortenedUrl, JResult res) {
        return res.getTitle() != null && res.getTitle().length() != 0
                ? res.getTitle() : res.getOriginalUrl() != null && res.getOriginalUrl().length() != 0
                ? res.getOriginalUrl() : unShortenedUrl;
    }

    @Nullable
    private Bitmap getFaviconBitmap(String faviconUrl) {
        Bitmap favicon = null;
        try {
            favicon = Glide.with(this)
                    .load(faviconUrl)
                    .asBitmap()
                    .into(-1, -1)
                    .get();
        } catch (InterruptedException | ExecutionException ignored) {

        }
        return favicon;
    }

    @NonNull
    private Intent getWebIntent(@NonNull String urlToAdd) {
        Intent webIntent = new Intent(this, BrowserInterceptActivity.class);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        webIntent.setData(Uri.parse(urlToAdd));
        return webIntent;
    }

    @NonNull
    private Bitmap createIcon(@ColorInt int color, String shortCutName) {
        int size = Util.dpToPx(48);
        Bitmap icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);
        float shadwR = Util.dpToPx(2);
        float shadwDx = Util.dpToPx(0);
        float shadwDy = Util.dpToPx(1.5);
        float textSize = Util.dpToPx(20);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(color);
        bgPaint.setShadowLayer(shadwR, shadwDx, shadwDy, 0x75000000);

        int padding = Util.dpToPx(5);
        int corner = Util.dpToPx(3);
        canvas.drawRoundRect(new RectF(padding, padding, size - padding, size - padding),
                corner, corner, bgPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextSize(textSize);
        textPaint.setColor(ColorUtil.getForegroundWhiteOrBlack(color));
        textPaint.setStyle(Paint.Style.FILL);

        drawTextInCanvasCentre(canvas, textPaint, getLetter(shortCutName));
        return icon;
    }

    private void drawTextInCanvasCentre(Canvas canvas, Paint paint, String text) {
        int cH = canvas.getClipBounds().height();
        int cW = canvas.getClipBounds().width();
        Rect rect = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), rect);
        float x = cW / 2f - rect.width() / 2f - rect.left;
        float y = cH / 2f + rect.height() / 2f - rect.bottom;
        canvas.drawText(text, x, y, paint);
    }

    private String getLetter(String address) {
        String result = "X";
        if (address != null) {
            try {
                URL url = new URL(address);
                String host = url.getHost();
                if (host != null && host.length() != 0) {
                    if (host.startsWith("www")) {
                        String[] splits = host.split("\\.");
                        if (splits.length > 1) result = String.valueOf(splits[1].charAt(0));
                        else result = String.valueOf(splits[0].charAt(0));
                    } else
                        result = String.valueOf(host.charAt(0));
                } else {
                    if (address.length() != 0) {
                        return String.valueOf(address.charAt(0));
                    }
                }
            } catch (Exception e) {
                if (address.length() != 0) {
                    return String.valueOf(address.charAt(0));
                } else return result;
            }
        }
        return result.toUpperCase();
    }

    private boolean isValidFavicon(Bitmap favicon) {
        return !(favicon.getWidth() == 16 || favicon.getHeight() == 16
                || favicon.getWidth() == 32 || favicon.getHeight() == 32);
    }

    private void legacyAdd(@NonNull String unShortenedUrl) {
        Intent webIntent = getWebIntent(unShortenedUrl);

        String hostName = Uri.parse(unShortenedUrl).getHost();
        String shortcutName = hostName == null ? unShortenedUrl : hostName;

        Intent addIntent = new Intent(Constants.ACTION_INSTALL_SHORTCUT);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, webIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        addIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(
                        getApplicationContext(),
                        R.mipmap.ic_launcher));

        sendBroadcast(addIntent);
    }

    private void showToast(@NonNull final String msgToShow) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(
                        AddHomeShortcutService.this,
                        msgToShow,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}