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

package arun.com.chromer.webheads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import arun.com.chromer.webheads.WebHeadService;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.EXTRA_KEY_FROM_NEW_TAB;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_SKIP_EXTRACTION;

public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Cleared");

        if (getIntent() == null || getIntent().getData() == null) {
            finish();
            return;
        }
        boolean isFromNewTab = getIntent().getBooleanExtra(EXTRA_KEY_FROM_NEW_TAB, false);
        boolean launchDirectly = getIntent().getBooleanExtra(EXTRA_KEY_SKIP_EXTRACTION, false);
        final Intent webHeadService = new Intent(this, WebHeadService.class);
        webHeadService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webHeadService.setData(getIntent().getData());
        webHeadService.putExtra(EXTRA_KEY_FROM_NEW_TAB, isFromNewTab);
        webHeadService.putExtra(EXTRA_KEY_SKIP_EXTRACTION, launchDirectly);
        startService(webHeadService);
        finish();
    }
}
