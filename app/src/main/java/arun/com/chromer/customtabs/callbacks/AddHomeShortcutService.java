/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import com.chimbori.crux.articles.Article;

import java.util.concurrent.ExecutionException;

import arun.com.chromer.R;
import arun.com.chromer.activities.browserintercept.BrowserInterceptActivity;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.parser.RxParser;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

import static android.content.Intent.EXTRA_SHORTCUT_ICON;
import static android.content.Intent.EXTRA_SHORTCUT_ICON_RESOURCE;
import static android.content.Intent.EXTRA_SHORTCUT_INTENT;
import static android.content.Intent.EXTRA_SHORTCUT_NAME;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.ShortcutIconResource.fromContext;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Style.FILL;
import static android.widget.Toast.LENGTH_SHORT;
import static arun.com.chromer.shared.Constants.ACTION_INSTALL_SHORTCUT;
import static arun.com.chromer.shared.Constants.NO_COLOR;

public class AddHomeShortcutService extends IntentService {

    public AddHomeShortcutService() {
        super(AddHomeShortcutService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getDataString() != null) {
            final String urlToAdd = intent.getDataString();

            showToast(getString(R.string.add_home_screen_begun));

            final Article article = RxParser.parseUrlSync(urlToAdd);
            if (article == null) {
                legacyAdd(urlToAdd);
                return;
            }
            final WebSite webSite = WebSite.fromArticle(article);
            final String shortCutName = webSite.safeLabel();
            final String faviconUrl = webSite.faviconUrl;

            Bitmap favicon = getFaviconBitmap(faviconUrl);
            if (favicon == null) {
                favicon = createShortcutIcon(ContextCompat.getColor(this, R.color.primary), shortCutName);
            } else if (!Utils.isValidFavicon(favicon)) {
                final Palette palette = Palette.from(favicon).clearFilters().generate();
                int iconColor = ColorUtil.getBestFaviconColor(palette);
                if (iconColor == NO_COLOR) {
                    iconColor = ContextCompat.getColor(this, R.color.primary);
                }
                favicon = createShortcutIcon(iconColor, shortCutName);
            }
            try {
                broadcastShortcutIntent(shortCutName, favicon, getWebIntent(webSite.preferredUrl()));
            } catch (Exception e) {
                Timber.e("Failed to create shortcut");
            }
            showToast(getString(R.string.added) + " " + shortCutName);
        }
    }

    private void broadcastShortcutIntent(final String shortCutName, final Bitmap favicon, final Intent webIntent) {
        final Intent addIntent = new Intent(ACTION_INSTALL_SHORTCUT);
        addIntent.putExtra(EXTRA_SHORTCUT_INTENT, webIntent);
        addIntent.putExtra(EXTRA_SHORTCUT_NAME, shortCutName);
        addIntent.putExtra(EXTRA_SHORTCUT_ICON, favicon);
        sendBroadcast(addIntent);
    }

    @Nullable
    private Bitmap getFaviconBitmap(@NonNull final String faviconUrl) {
        Bitmap favicon = null;
        try {
            favicon = Glide.with(this)
                    .load(faviconUrl)
                    .asBitmap()
                    .into(192, 192)
                    .get();
        } catch (InterruptedException | ExecutionException ignored) {
            Timber.e(ignored.getMessage());
        }
        return favicon;
    }

    @NonNull
    private Intent getWebIntent(@NonNull String urlToAdd) {
        Intent webIntent = new Intent(this, BrowserInterceptActivity.class);
        webIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        webIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        webIntent.setData(Uri.parse(urlToAdd));
        return webIntent;
    }

    @NonNull
    private Bitmap createShortcutIcon(@ColorInt int color, String shortCutName) {
        final int size = Utils.dpToPx(48);
        final Bitmap icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(icon);
        final float shadwR = Utils.dpToPx(1.8);
        final float shadwDx = Utils.dpToPx(0.1);
        final float shadwDy = Utils.dpToPx(1.2);
        final float textSize = Utils.dpToPx(20);

        final Paint bgPaint = new Paint(ANTI_ALIAS_FLAG);
        bgPaint.setStyle(FILL);
        bgPaint.setColor(color);
        bgPaint.setShadowLayer(shadwR, shadwDx, shadwDy, 0x75000000);

        final int padding = Utils.dpToPx(5);
        final int corner = Utils.dpToPx(3);
        canvas.drawRoundRect(new RectF(padding, padding, size - padding, size - padding),
                corner, corner, bgPaint);

        final Paint textPaint = new Paint(ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextSize(textSize);
        textPaint.setColor(ColorUtil.getForegroundWhiteOrBlack(color));
        textPaint.setStyle(FILL);
        drawTextInCanvasCentre(canvas, textPaint, Utils.getFirstLetter(shortCutName));
        return icon;
    }

    private void drawTextInCanvasCentre(Canvas canvas, Paint paint, String text) {
        final int cH = canvas.getClipBounds().height();
        final int cW = canvas.getClipBounds().width();
        final Rect rect = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), rect);
        final float x = cW / 2f - rect.width() / 2f - rect.left;
        final float y = cH / 2f + rect.height() / 2f - rect.bottom;
        canvas.drawText(text, x, y, paint);
    }

    private void legacyAdd(@NonNull String unShortenedUrl) {
        final Intent webIntent = getWebIntent(unShortenedUrl);
        final String hostName = Uri.parse(unShortenedUrl).getHost();
        final String shortcutName = hostName == null ? unShortenedUrl : hostName;
        final Intent addIntent = new Intent(ACTION_INSTALL_SHORTCUT);
        addIntent.putExtra(EXTRA_SHORTCUT_INTENT, webIntent);
        addIntent.putExtra(EXTRA_SHORTCUT_NAME, shortcutName);
        addIntent.putExtra(EXTRA_SHORTCUT_ICON_RESOURCE,
                fromContext(getApplicationContext(), R.mipmap.ic_launcher));
        sendBroadcast(addIntent);
    }

    private void showToast(@NonNull final String msgToShow) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(AddHomeShortcutService.this, msgToShow, LENGTH_SHORT).show());
    }
}