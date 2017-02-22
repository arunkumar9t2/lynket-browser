package arun.com.chromer.activities.base;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import arun.com.chromer.R;

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
        overridePendingTransition(R.anim.slide_in_left_medium, R.anim.slide_out_right_medium);
    }
}
