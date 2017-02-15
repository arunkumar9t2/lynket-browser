package arun.com.chromer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;

import arun.com.chromer.R;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_with);
        ButterKnife.bind(this);

        bottomSheet.addOnSheetDismissedListener(new OnSheetDismissedListener() {
            @Override
            public void onDismissed(BottomSheetLayout bottomSheetLayout) {
                finish();
            }
        });

        if (getIntent() != null && getIntent().getDataString() != null) {
            final Intent webSiteIntent = new Intent(ACTION_VIEW, getIntent().getData());
            final IntentPickerSheetView browserPicker = new IntentPickerSheetView(this,
                    webSiteIntent,
                    R.string.open_with,
                    new IntentPickerSheetView.OnIntentPickedListener() {
                        @Override
                        public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                            bottomSheet.dismissSheet();
                            webSiteIntent.setComponent(activityInfo.componentName);
                            webSiteIntent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                            startActivity(webSiteIntent);
                            finish();
                        }
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
