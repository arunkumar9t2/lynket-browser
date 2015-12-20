package arun.com.chromer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import arun.com.chromer.chrometabutilites.CustomTabHelperFragMine;
import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.extra.Licenses;
import arun.com.chromer.intro.AppIntroMy;
import de.psdev.licensesdialog.LicensesDialog;

public class MainActivity extends AppCompatActivity {
    private static final String GOOGLE_URL = "http://www.google.com/";

    private static final String CUSTOM_TAB_URL = "https://developer.chrome.com/multidevice/android/customtabs#whentouse";

    private CustomTabsIntent mCustomTabsIntent;
    private SharedPreferences preferences;
    private Drawer drawer;

    private boolean isFirstRun() {
        if (preferences.getBoolean("firstrun", true)) {
            preferences.edit().putBoolean("firstrun", false).commit();
            return true;
        }
        return false;
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

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preference_fragment, new PreferenceFragment())
                .commit();

        findViewById(R.id.set_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(GOOGLE_URL);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCustomTab(GOOGLE_URL);
            }
        });

        setupCustomTab();
    }

    private void launchCustomTab(String url) {
        mCustomTabsIntent = Util.getCutsomizedTabIntent(getApplicationContext(), url);
        CustomTabHelperFragMine.open(MainActivity.this, mCustomTabsIntent, Uri.parse(url),
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
                return true;
            }
        }
        return false;
    }

    private void setupDrawer(Toolbar toolbar) {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.chromer)
                        .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        .withDividerBelowHeader(true)
                        .build())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("About").withIdentifier(4)
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
        CustomTabHelperFragMine mCustomTabHelperFragMine = CustomTabHelperFragMine.attachTo(this);
        mCustomTabHelperFragMine.setConnectionCallback(
                new MyCustomActivityHelper.ConnectionCallback() {
                    @Override
                    public void onCustomTabsConnected() {

                    }

                    @Override
                    public void onCustomTabsDisconnected() {
                    }
                });
    }
}
