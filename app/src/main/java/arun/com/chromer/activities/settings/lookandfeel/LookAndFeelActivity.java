package arun.com.chromer.activities.settings.lookandfeel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

import arun.com.chromer.R;
import arun.com.chromer.activities.SnackHelper;
import butterknife.BindView;
import butterknife.ButterKnife;

import static arun.com.chromer.shared.Constants.ACTION_TOOLBAR_COLOR_SET;
import static arun.com.chromer.shared.Constants.ACTION_WEBHEAD_COLOR_SET;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_TOOLBAR_COLOR;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_WEBHEAD_COLOR;

public class LookAndFeelActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, SnackHelper {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    private PersonalizationPreferenceFragment preferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_and_feel);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferenceFragment = PersonalizationPreferenceFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.toolbar_options_preferences_container, preferenceFragment)
                .replace(R.id.web_head_options_preferences_container, WebHeadPreferenceFragment.newInstance())
                .commit();
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.default_toolbar_color:
                final Intent toolbarColorIntent = new Intent(ACTION_TOOLBAR_COLOR_SET);
                toolbarColorIntent.putExtra(EXTRA_KEY_TOOLBAR_COLOR, selectedColor);
                LocalBroadcastManager.getInstance(this).sendBroadcast(toolbarColorIntent);
                break;
            case R.string.web_heads_color:
                final Intent webHeadColorIntent = new Intent(ACTION_WEBHEAD_COLOR_SET);
                webHeadColorIntent.putExtra(EXTRA_KEY_WEBHEAD_COLOR, selectedColor);
                LocalBroadcastManager.getInstance(this).sendBroadcast(webHeadColorIntent);
                break;
        }
    }

    @Override
    public void snack(@NonNull String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void snackLong(@NonNull String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}
