package arun.com.chromer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;

import arun.com.chromer.R;
import arun.com.chromer.util.DocumentUtils;
import arun.com.chromer.views.IntentPickerSheetView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class OpenIntentWithActivity extends AppCompatActivity {

    @BindView(R.id.bottomsheet)
    BottomSheetLayout bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DocumentUtils.closeRootActivity(this);
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
