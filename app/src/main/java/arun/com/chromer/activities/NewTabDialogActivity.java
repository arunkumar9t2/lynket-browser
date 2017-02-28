package arun.com.chromer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import arun.com.chromer.R;
import arun.com.chromer.util.Utils;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.widget.Toast.LENGTH_SHORT;


public class NewTabDialogActivity extends AppCompatActivity {

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
}
