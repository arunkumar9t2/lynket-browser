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

package arun.com.chromer.shared.base.activity;

import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Arunkumar on 19-02-2017.
 */
public abstract class SubActivity extends AppCompatActivity {
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finishWithTransition();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    finishWithTransition();
    super.onBackPressed();
  }

  private void finishWithTransition() {
    finish();
    // overridePendingTransition(R.anim.slide_in_left_medium, R.anim.slide_out_right_medium);
  }
}
