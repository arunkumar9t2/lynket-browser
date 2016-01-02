package arun.com.chromer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.adapter.ExtendedBaseAdapter;
import arun.com.chromer.fragments.AboutFragment;
import arun.com.chromer.payments.IabBroadcastReceiver;
import arun.com.chromer.payments.IabHelper;
import arun.com.chromer.payments.IabResult;
import arun.com.chromer.payments.Inventory;
import arun.com.chromer.payments.Purchase;
import arun.com.chromer.payments.SkuDetails;

public class DonateActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener,
        DialogInterface.OnClickListener {
    private static final String COFEE_SKU = "coffee_small";
    private static final String LUNCH_SKU = "lunch_mega";
    private static final String PREMIUM_SKU = "premium_donation";
    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;
    private static final String TAG = DonateActivity.class.getSimpleName();
    private static boolean mCoffeDone = false;
    private static boolean mLunchDone = false;
    private static boolean mPremiumDone = false;
    private IabHelper mHelper;
    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
            }
            Log.d(TAG, "End consumption flow.");
        }
    };
    // Callback for when a purchase is finished
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }
            Log.d(TAG, "Purchase successful.");
        }
    };
    // Listener that's called when we finish querying the items and subscriptions we own
    private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            // Get coffee sku
            SkuDetails coffeSku = inventory.getSkuDetails(COFEE_SKU);
            SkuDetails lunchSku = inventory.getSkuDetails(LUNCH_SKU);
            SkuDetails premiumSku = inventory.getSkuDetails(PREMIUM_SKU);

            List<SkuDetails> list = new ArrayList<>();
            list.add(coffeSku);
            list.add(lunchSku);
            list.add(premiumSku);

            mCoffeDone = inventory.getPurchase(COFEE_SKU) != null;
            mLunchDone = inventory.getPurchase(LUNCH_SKU) != null;
            mPremiumDone = inventory.getPurchase(PREMIUM_SKU) != null;

            loadData(list);
        }
    };
    // Provides purchase notification while this app is running
    private IabBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp0P1CS2wKf3jnu/BUnolJcejH84v4M1uAGKFcj3Cn+OzC1aOxxPk6rlVhOPQiExpyuWYXTKutDeHUIixdp5G1oaoa8Ak8caoYRSWCB3bfZzoQivVIvPvRl6fibukHqSAnlG7Ueq0qassHtaxGN7MaHUBDc2jNYJ6GnWlTeqczdfS8lVMddlU8rp6yzSVOvXOl5/Eao6PXjDl0dpCAu1gIx5TPJXioBkgCI+NgSSLEGWPpq+sqacYCi6R+YEgwi+5w1erwBcdIU4/70ROFTtGt5+PDPNkDWi6V9hjUmTBp7EajXccNVGkzqW3opSPaH1rUtRz+sFqUHLPJemXb29xIQIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");
                if (!result.isSuccess()) {
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(DonateActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                List additionalSku = new ArrayList();
                additionalSku.add(COFEE_SKU);
                additionalSku.add(LUNCH_SKU);
                additionalSku.add(PREMIUM_SKU);
                mHelper.queryInventoryAsync(true, additionalSku, mGotInventoryListener);
            }
        });
    }

    private void loadData(final List<SkuDetails> details) {
        ListView donateList = (ListView) findViewById(R.id.donate_item_list);

        final String error = getString(R.string.couldnt_load_price);

        donateList.setAdapter(new ExtendedBaseAdapter() {
            final Context context = getApplicationContext();

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                AboutFragment.ViewHolder holder;
                if (convertView == null) {
                    holder = new AboutFragment.ViewHolder();
                    convertView = mInflater.inflate(R.layout.fragment_about_listview_template, parent, false);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.about_row_item_image);
                    holder.subtitle = (TextView) convertView.findViewById(R.id.about_app_subtitle);
                    holder.title = (TextView) convertView.findViewById(R.id.about_app_title);
                    convertView.setTag(holder);
                } else {
                    holder = (AboutFragment.ViewHolder) convertView.getTag();
                }
                switch (position) {
                    case 0:
                        if (mCoffeDone) setGreen(holder);
                        else setBlack(holder);
                        holder.title.setText(getString(R.string.coffee));
                        holder.subtitle.setText(details.get(0) != null ?
                                details.get(0).getPrice() + " " + details.get(0).getPriceCurrencyCode()
                                : error);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_coffee)
                                .color(ContextCompat.getColor(getApplicationContext(),
                                        R.color.coffee_color))
                                .sizeDp(24));
                        break;
                    case 1:
                        if (mLunchDone) setGreen(holder);
                        else setBlack(holder);
                        holder.title.setText(getString(R.string.lunch));
                        holder.subtitle.setText(details.get(1) != null ?
                                details.get(1).getPrice() + " " + details.get(1).getPriceCurrencyCode()
                                : error);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_food)
                                .color(ContextCompat.getColor(getApplicationContext(),
                                        R.color.lunch_color))
                                .sizeDp(24));
                        break;
                    case 2:
                        if (mPremiumDone) setGreen(holder);
                        else setBlack(holder);
                        holder.title.setText(getString(R.string.premimum_donation));
                        holder.subtitle.setText(details.get(2) != null ?
                                details.get(2).getPrice() + " " + details.get(2).getPriceCurrencyCode()
                                : error);
                        holder.imageView.setBackground(new IconicsDrawable(context)
                                .icon(CommunityMaterial.Icon.cmd_cash_usd)
                                .color(ContextCompat.getColor(getApplicationContext(),
                                        (R.color.premium_color)))
                                .sizeDp(24));
                        break;
                }
                return convertView;
            }
        });

        donateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mHelper.launchPurchaseFlow(DonateActivity.this, COFEE_SKU, RC_REQUEST,
                                mPurchaseFinishedListener, "coffee");
                        return;
                    case 1:
                        mHelper.launchPurchaseFlow(DonateActivity.this, LUNCH_SKU, RC_REQUEST,
                                mPurchaseFinishedListener, "lunch");
                        return;
                    case 2:
                        mHelper.launchPurchaseFlow(DonateActivity.this, PREMIUM_SKU, RC_REQUEST,
                                mPurchaseFinishedListener, "premium");
                        return;
                }
            }
        });
    }

    private void setGreen(AboutFragment.ViewHolder holder) {
        if (holder != null) {
            int color = ContextCompat.getColor(this, R.color.donate_green);
            holder.title.setTextColor(color);
            holder.subtitle.setTextColor(color);
        }
    }

    private void setBlack(AboutFragment.ViewHolder holder) {
        if (holder != null) {
            int color = ContextCompat.getColor(this, R.color.material_dark_color);
            holder.title.setTextColor(color);
            holder.subtitle.setTextColor(color);
        }
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
}
