package arun.com.chromer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.extra.Licenses;
import arun.com.chromer.fragments.PreferenceFragment;
import arun.com.chromer.intro.AppIntroMy;
import de.psdev.licensesdialog.LicensesDialog;

public class MainActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {
    private static final String GOOGLE_URL = "http://www.google.com/";

    private static final String CUSTOM_TAB_URL = "https://developer.chrome.com/multidevice/android/customtabs#whentouse";
    private static final String TAG = MainActivity.class.getSimpleName();

    private MyCustomActivityHelper mCustomTabActivityHelper;

    private SharedPreferences preferences;
    private View colorView;

    private boolean isFirstRun() {
        if (preferences.getBoolean("firstrun", true)) {
            preferences.edit().putBoolean("firstrun", false).apply();
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        if (isFirstRun()) {
            startActivity(new Intent(this, AppIntroMy.class));
        }

        setupDrawer(toolbar);

        findViewById(R.id.set_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(GOOGLE_URL);
            }
        });

        setupFAB();

        setupCustomTab();

        setupColorPicker();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_fragment, new PreferenceFragment())
                .commit();
    }

    private void setupFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCustomTab(GOOGLE_URL);
            }
        });
    }

    private void setupColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final int choosenColor = sharedPreferences.getInt("toolbar_color",
                getResources().getColor(R.color.primary));
        findViewById(R.id.color_picker_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorChooserDialog.Builder(MainActivity.this, R.string.md_choose_label)
                        .titleSub(R.string.md_presets_label)
                        .doneButton(R.string.md_done_label)  // changes label of the done button
                        .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                        .backButton(R.string.md_back_label)  // changes label of the back button
                        .allowUserColorInputAlpha(false)
                        .preselect(choosenColor)
                        .dynamicButtonColor(false)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show();
            }
        });
        colorView = findViewById(R.id.color_preview);
        colorView.setBackgroundColor(choosenColor);
    }

    private void launchCustomTab(String url) {
        CustomTabsIntent mCustomTabsIntent = Util.getCutsomizedTabIntent(getApplicationContext(), url);
        MyCustomActivityHelper.openCustomTab(this, mCustomTabsIntent, Uri.parse(url),
                TabActivity.mCustomTabsFallback);
    }

    private void openWebPage(String url) {
        Uri googleURI = Uri.parse(url);
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, googleURI);
        if (!isDefaultSet(activityIntent)) {
            if (activityIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(activityIntent);
            }
        } else {
            Toast.makeText(this, "Already set!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDefaultSet(Intent web) {
        ResolveInfo defaultViewHandlerInfo = getPackageManager().resolveActivity(web, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }
        if (defaultViewHandlerPackageName != null) {
            if (defaultViewHandlerPackageName.trim().equalsIgnoreCase(getPackageName())) {
                Log.d(TAG, "Chromer defaulted");
                return true;
            }
        }
        return false;
    }

    private void setupDrawer(Toolbar toolbar) {
        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.chromer)
                        .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        .withDividerBelowHeader(true)
                        .build())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Intro").withIdentifier(4)
                                .withIcon(GoogleMaterial.Icon.gmd_assignment)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName("More Apps").withIdentifier(1)
                                .withIcon(GoogleMaterial.Icon.gmd_android)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName("Feedback").withIdentifier(2)
                                .withIcon(GoogleMaterial.Icon.gmd_feedback)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName("Rate").withIdentifier(3)
                                .withIcon(GoogleMaterial.Icon.gmd_rate_review)
                                .withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("More on custom tabs")
                                .withIcon(GoogleMaterial.Icon.gmd_open_in_new)
                                .withIdentifier(5)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName("Share")
                                .withIcon(GoogleMaterial.Icon.gmd_share)
                                .withDescription("Help Chromer grow!")
                                .withIdentifier(7)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName("Licenses")
                                .withIcon(GoogleMaterial.Icon.gmd_card_membership)
                                .withIdentifier(6)
                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == null)
                            return false;
                        int i = drawerItem.getIdentifier();
                        switch (i) {
                            case 1:
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Arunkumar")));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Arunkumar")));
                                }
                                break;
                            case 2:
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "arunk.beece@gmail.com", null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Chromer");
                                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                break;
                            case 3:
                                Util.openPlayStore(MainActivity.this, getPackageName());
                                break;
                            case 4:
                                startActivity(new Intent(MainActivity.this, AppIntroMy.class));
                                break;
                            case 5:
                                launchCustomTab(CUSTOM_TAB_URL);
                                break;
                            case 6:
                                showLicensesDialog();
                                break;
                            case 7:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey Checkout 'Chromer' for quick and secure browsing experience! Download here https://goo.gl/992ils");
                                shareIntent.setType("text/plain");
                                startActivity(Intent.createChooser(shareIntent, "Share via..."));
                        }
                        return false;
                    }
                })
                .build();
        drawer.setSelection(-1);
    }

    private void showLicensesDialog() {
        new LicensesDialog.Builder(this)
                .setNotices(Licenses.getNotices())
                .setTitle("Licenses")
                .build()
                .showAppCompat();
    }

    private void setupCustomTab() {
        mCustomTabActivityHelper = new MyCustomActivityHelper();
        mCustomTabActivityHelper.setConnectionCallback(
                new MyCustomActivityHelper.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {
                        Log.d(TAG, "Connect to custom tab");
                        try {
                            Log.d(TAG, "Gave may launch command");
                            mCustomTabActivityHelper.mayLaunchUrl(
                                    Uri.parse(GOOGLE_URL)
                                    , null, null);
                        } catch (Exception e) {
                            // Don't care. Yes.. You heard me.
                        }
                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                        Log.d(TAG, "Disconnect to custom tab");
                    }
                });
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        colorView.setBackgroundColor(selectedColor);
        sharedPreferences.edit().putInt("toolbar_color", selectedColor).apply();
    }
}
