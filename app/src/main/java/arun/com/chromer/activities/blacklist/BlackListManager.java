package arun.com.chromer.activities.blacklist;

import android.support.annotation.NonNull;

/**
 * Utility class to manage interaction with DB for blacklist functionality.
 */
public class BlackListManager {
    private BlackListManager() {
        throw new RuntimeException("No instances");
    }

    public static boolean isPackageBlackListed(@NonNull String packageName) {
        return false;
        // return !BlacklistedApps.find(BlacklistedApps.class, "package_name = ?", packageName).isEmpty();
    }

    public static void setBlackListed(@NonNull String packageName) {
        /*final BlacklistedApps blacklistedApp = new BlacklistedApps(packageName);
        if (blacklistedApp.save() != -1) {
            Timber.d("Saved blacklist: %s", packageName);
        }*/
    }

    public static void deleteBlackListed(@NonNull String packageName) {
        /*for (BlacklistedApps app : BlacklistedApps.find(BlacklistedApps.class, "package_name = ?", packageName)) {
            if (app.delete()) {
                Timber.d("Deleted blacklist: %s", packageName);
            }
        }*/
    }
}
