package arun.com.chromer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.List;

import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment;

/**
 * Created by Arun on 18/12/2015.
 */
public class CustomTabHelperFragMine extends Fragment {

    private static final String FRAGMENT_TAG = CustomTabsHelperFragment.class.getName();

    private CusActivtyHelper mCusActivtyHelper = new CusActivtyHelper();

    /**
     * Ensure that an instance of this fragment is attached to an activity.
     *
     * @param activity The target activity.
     * @return An instance of this fragment.
     */

    public static CustomTabHelperFragMine attachTo(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        CustomTabHelperFragMine fragment = (CustomTabHelperFragMine) fragmentManager
                .findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new CustomTabHelperFragMine();
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
        }
        return fragment;
    }

    /**
     * Ensure that an instance of this fragment is attached to the host activity of a fragment.
     *
     * @param fragment The target fragment, which will be used to retrieve the host activity.
     * @return An instance of this fragment.
     */
    public static CustomTabHelperFragMine attachTo(Fragment fragment) {
        return attachTo(fragment.getActivity());
    }

    // Cannot get javadoc to compile, saying "reference not found".
    /*
     * @see CusActivtyHelper#openCustomTab(Activity, CustomTabsIntent, Uri, CusActivtyHelper.CustomTabsFallback)
     */
    public static void open(Activity activity, CustomTabsIntent intent, Uri uri,
                            CusActivtyHelper.CustomTabsFallback fallback) {
        CusActivtyHelper.openCustomTab(activity, intent, uri, fallback);
    }

    /**
     * Get the {@link CusActivtyHelper} this fragment manages.
     *
     * @return The {@link CusActivtyHelper}.
     */
    public CusActivtyHelper getHelper() {
        return mCusActivtyHelper;
    }

    /**
     * @see CusActivtyHelper#getSession()
     */
    public CustomTabsSession getSession() {
        return mCusActivtyHelper.getSession();
    }

    /**
     * @see CusActivtyHelper#setConnectionCallback(CusActivtyHelper.ConnectionCallback)
     */
    public void setConnectionCallback(
            CusActivtyHelper.ConnectionCallback connectionCallback) {
        mCusActivtyHelper.setConnectionCallback(connectionCallback);
    }

    /**
     * @see CusActivtyHelper#mayLaunchUrl(Uri, Bundle, List)
     */
    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        return mCusActivtyHelper.mayLaunchUrl(uri, extras, otherLikelyBundles);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setUserVisibleHint(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCusActivtyHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        mCusActivtyHelper.unbindCustomTabsService(getActivity());
    }
}
