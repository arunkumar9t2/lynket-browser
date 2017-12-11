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

package arun.com.chromer.activities.newtab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.activities.browserintercept.BrowserInterceptActivity;
import arun.com.chromer.di.activity.ActivityComponent;
import arun.com.chromer.shared.common.BaseActivity;
import arun.com.chromer.util.Utils;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.widget.Toast.LENGTH_SHORT;


public class NewTabDialogActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String clipboard = Utils.getClipBoardText(this);
        final String value = getString(R.string.search_or_type_url);
        new MaterialDialog.Builder(this)
                .title(R.string.new_tab)
                .inputType(TYPE_CLASS_TEXT)
                .positiveText(R.string.open)
                .iconRes(R.drawable.ic_shortcut_add)
                .input(value, clipboard, false, (dialog, input) -> {
                    // Do something
                    final String url = Utils.getSearchUrl(input.toString());
                    Toast.makeText(NewTabDialogActivity.this, url, LENGTH_SHORT).show();
                    final Intent websiteIntent = new Intent(NewTabDialogActivity.this, BrowserInterceptActivity.class);
                    websiteIntent.setData(Uri.parse(url));
                    startActivity(websiteIntent);
                    finish();
                })
                .dismissListener(dialog -> finish()).show();
    }

    @Override
    protected int getLayoutRes() {
        return 0;
    }

    @Override
    public void inject(@NonNull ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }
}
