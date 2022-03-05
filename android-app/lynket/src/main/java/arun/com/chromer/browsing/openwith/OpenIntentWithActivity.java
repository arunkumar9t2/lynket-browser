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

package arun.com.chromer.browsing.openwith;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.flipboard.bottomsheet.BottomSheetLayout;

import arun.com.chromer.R;
import arun.com.chromer.shared.views.IntentPickerSheetView;
import butterknife.BindView;
import butterknife.ButterKnife;


public class OpenIntentWithActivity extends AppCompatActivity {

  @BindView(R.id.bottomsheet)
  BottomSheetLayout bottomSheet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_open_with);
    ButterKnife.bind(this);

    bottomSheet.addOnSheetDismissedListener(bottomSheetLayout -> finish());

    if (getIntent() != null && getIntent().getDataString() != null) {
      final Intent webSiteIntent = new Intent(ACTION_VIEW, getIntent().getData());
      final IntentPickerSheetView browserPicker = new IntentPickerSheetView(this,
        webSiteIntent,
        R.string.open_with,
        activityInfo -> {
          bottomSheet.dismissSheet();
          webSiteIntent.setComponent(activityInfo.componentName);
          webSiteIntent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
          startActivity(webSiteIntent);
          finish();
        });
      browserPicker.setFilter(IntentPickerSheetView.selfPackageExcludeFilter(this));
      bottomSheet.showWithSheetView(browserPicker);
    } else {
      invalidLink();
      finish();
    }
  }

  private void invalidLink() {
    Toast.makeText(this, getString(R.string.invalid_link), Toast.LENGTH_SHORT).show();
    finish();
  }
}
