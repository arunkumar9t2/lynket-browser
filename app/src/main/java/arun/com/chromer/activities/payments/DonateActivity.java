package arun.com.chromer.activities.payments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;
import arun.com.chromer.activities.payments.util.IabBroadcastReceiver;
import arun.com.chromer.activities.payments.util.IabHelper;
import arun.com.chromer.activities.payments.util.IabResult;
import arun.com.chromer.activities.payments.util.Inventory;
import arun.com.chromer.activities.payments.util.Purchase;
import arun.com.chromer.activities.payments.util.SkuDetails;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DonateActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener,
        DialogInterface.OnClickListener {
    private static final String COFEE_SKU = "coffee_small";
    private static final String LUNCH_SKU = "lunch_mega";
    private static final String PREMIUM_SKU = "premium_donation";
    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;
    private static final int ICON_SIZE_DP = 24;
    private static boolean mCoffeeDone = false;
    private static boolean mLunchDone = false;
    private static boolean mPremiumDone = false;
    private IabHelper mHelper;
    // Callback for when a purchase is finished
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Timber.d("Purchase finished: %s, purchase: %s", result, purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }
            Timber.d("Purchase successful.");
        }
    };
    // Listener that's called when we finish querying the items and subscriptions we own
    private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Timber.d("Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                return;
            }

            Timber.d("Query inventory was successful.");

            // Get coffee sku
            SkuDetails coffeeSku = inventory.getSkuDetails(COFEE_SKU);
            SkuDetails lunchSku = inventory.getSkuDetails(LUNCH_SKU);
            SkuDetails premiumSku = inventory.getSkuDetails(PREMIUM_SKU);

            List<SkuDetails> list = new ArrayList<>();
            list.add(coffeeSku);
            list.add(lunchSku);
            list.add(premiumSku);

            mCoffeeDone = inventory.getPurchase(COFEE_SKU) != null;
            mLunchDone = inventory.getPurchase(LUNCH_SKU) != null;
            mPremiumDone = inventory.getPurchase(PREMIUM_SKU) != null;

            if (mCoffeeDone | mLunchDone | mPremiumDone) {
                findViewById(R.id.thank_you).setVisibility(View.VISIBLE);
            }

            loadData(list);
        }
    };
    // Provides purchase notification while this app is running
    private IabBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate_activity);

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, BuildConfig.BASE_64);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        Timber.d("Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onIabSetupFinished(IabResult result) {
                Timber.d("Setup finished.");
                if (!result.isSuccess()) {
                    Timber.d("Problem setting up In-app Billing: %s", result);
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(DonateActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Timber.d("Setup successful. Querying inventory.");
                List additionalSku = new ArrayList();
                additionalSku.add(COFEE_SKU);
                additionalSku.add(LUNCH_SKU);
                additionalSku.add(PREMIUM_SKU);
                mHelper.queryInventoryAsync(true, additionalSku, mGotInventoryListener);
            }
        });
    }

    private void loadData(final List<SkuDetails> details) {
        final RecyclerView donateList = (RecyclerView) findViewById(R.id.donate_item_list);
        donateList.setLayoutManager(new LinearLayoutManager(this));
        donateList.setAdapter(new DonationAdapter(details));
    }

    class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {
        private List<SkuDetails> details = new ArrayList<>();

        DonationAdapter(final List<SkuDetails> details) {
            if (details != null) {
                this.details = details;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.about_fragment_listview_template, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final String error = getString(R.string.couldnt_load_price);
            switch (position) {
                case 0:
                    if (mCoffeeDone) setGreen(holder);
                    else setBlack(holder);
                    holder.title.setText(getString(R.string.coffee));
                    holder.subtitle.setText(details.get(0) != null ?
                            details.get(0).getPrice() : error);
                    holder.image.setBackground(new IconicsDrawable(getApplicationContext())
                            .icon(CommunityMaterial.Icon.cmd_coffee)
                            .color(ContextCompat.getColor(getApplicationContext(),
                                    R.color.coffee_color))
                            .sizeDp(ICON_SIZE_DP));
                    break;
                case 1:
                    if (mLunchDone) setGreen(holder);
                    else setBlack(holder);
                    holder.title.setText(getString(R.string.lunch));
                    holder.subtitle.setText(details.get(1) != null ?
                            details.get(1).getPrice() : error);
                    holder.image.setBackground(new IconicsDrawable(getApplicationContext())
                            .icon(CommunityMaterial.Icon.cmd_food)
                            .color(ContextCompat.getColor(getApplicationContext(),
                                    R.color.lunch_color))
                            .sizeDp(ICON_SIZE_DP));
                    break;
                case 2:
                    if (mPremiumDone) setGreen(holder);
                    else setBlack(holder);
                    holder.title.setText(getString(R.string.premium_donation));
                    holder.subtitle.setText(details.get(2) != null ?
                            details.get(2).getPrice() : error);
                    holder.image.setBackground(new IconicsDrawable(getApplicationContext())
                            .icon(CommunityMaterial.Icon.cmd_cash_usd)
                            .color(ContextCompat.getColor(getApplicationContext(),
                                    (R.color.premium_color)))
                            .sizeDp(ICON_SIZE_DP));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return details.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.about_row_item_image)
            ImageView image;
            @BindView(R.id.about_app_title)
            TextView title;
            @BindView(R.id.about_app_subtitle)
            TextView subtitle;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
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
        }
    }

    private void setGreen(DonationAdapter.ViewHolder holder) {
        if (holder != null) {
            int color = ContextCompat.getColor(this, R.color.donate_green);
            holder.title.setTextColor(color);
            holder.subtitle.setTextColor(color);
        }
    }

    private void setBlack(DonationAdapter.ViewHolder holder) {
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
        Timber.d("Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Timber.d("Received broadcast notification. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Timber.d("onActivityResult handled by IABUtil.");
        }
    }
}
