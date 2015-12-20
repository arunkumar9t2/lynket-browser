package arun.com.chromer;

import android.app.Activity;
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
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import arun.com.chromer.chrometabutilites.CustomTabHelperFragMine;
import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import arun.com.chromer.intro.AppIntroMy;

public class MainActivity extends AppCompatActivity {
    private static final String URL = "http://www.google.com/";
    private final MyCustomActivityHelper.CustomTabsFallback mCustomTabsFallback =
            new MyCustomActivityHelper.CustomTabsFallback() {
                @Override
                public void openUri(Activity activity, Uri uri) {
                    Toast.makeText(activity, "No custom tab compatible browsers found", Toast.LENGTH_SHORT)
                            .show();
                }
            };
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
                openWebPage(URL);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCustomTabsIntent = Util.getCutsomizedTabIntent(getApplicationContext(), URL);
                CustomTabHelperFragMine.open(MainActivity.this, mCustomTabsIntent, Uri.parse(URL),
                        mCustomTabsFallback);
            }
        });

        setupCustomTab();
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, webpage);
        ResolveInfo defaultViewHandlerInfo = getPackageManager().resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }
        if (defaultViewHandlerPackageName != null) {
            if (defaultViewHandlerPackageName.trim().equalsIgnoreCase(getPackageName())) {
                Toast.makeText(this, "Already set!", Toast.LENGTH_SHORT).show();
            } else {
                if (activityIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(activityIntent);
                }
            }
        }

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
                                .withIcon(GoogleMaterial.Icon.gmd_assignment),
                        new PrimaryDrawerItem().withName("More Apps").withIdentifier(1)
                                .withIcon(GoogleMaterial.Icon.gmd_android),
                        new PrimaryDrawerItem().withName("Feedback").withIdentifier(2)
                                .withIcon(GoogleMaterial.Icon.gmd_feedback),
                        new PrimaryDrawerItem().withName("Rate").withIdentifier(3)
                                .withIcon(GoogleMaterial.Icon.gmd_rate_review)
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
                                drawer.setSelection(-1);
                                break;
                            case 2:
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "arunk.beece@gmail.com", null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Chromer");
                                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                drawer.setSelection(-1);
                                break;
                            case 3:
                                Util.openPlayStore(MainActivity.this, getPackageName());
                                drawer.setSelection(-1);
                                break;
                            case 4:
                                startActivity(new Intent(MainActivity.this, AppIntroMy.class));
                                drawer.setSelection(-1);
                                break;
                        }
                        return false;
                    }
                })
                .build();
        drawer.setSelection(-1);
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
