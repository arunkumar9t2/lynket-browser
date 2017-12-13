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

package arun.com.chromer.browsing.shareintercept;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.browsing.browserintercept.BrowserInterceptActivity;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.SafeIntent;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

@SuppressLint("GoogleAppIndexingApiWarning")
public class ShareInterceptActivity extends AppCompatActivity {


    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SafeIntent safeIntent = new SafeIntent(getIntent());
        try {
            final String action = safeIntent.getAction();
            String text = null;
            switch (action) {
                case Intent.ACTION_SEND:
                    if (safeIntent.hasExtra(Intent.EXTRA_TEXT)) {
                        text = getIntent().getExtras().getCharSequence(Intent.EXTRA_TEXT).toString();
                    }
                    break;
                case Intent.ACTION_PROCESS_TEXT:
                    text = safeIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                    break;
            }
            findAndOpenLink(text);
        } catch (Exception exception) {
            invalidLink();
            Timber.e(exception.getMessage());
        } finally {
            finish();
        }
    }


    private void findAndOpenLink(@Nullable String text) {
        if (text == null) return;
        final List<String> urls = Utils.findURLs(text);
        if (!urls.isEmpty()) {
            // use only the first link
            final String url = urls.get(0);
            // TODO launch group of web heads if web heads enabled
            openLink(url);
        } else {
            // No urls were found, so lets do a google search with the text received.
            text = Constants.G_SEARCH_URL + text.replace(" ", "+");
            openLink(text);
        }
    }

    private void openLink(@Nullable String url) {
        if (url == null) {
            invalidLink();
        }
        final Intent websiteIntent = new Intent(this, BrowserInterceptActivity.class);
        websiteIntent.setData(Uri.parse(url));
        startActivity(websiteIntent);
        finish();
    }

    private void invalidLink() {
        Toast.makeText(this, getString(R.string.invalid_link), Toast.LENGTH_SHORT).show();
        finish();
    }
}
