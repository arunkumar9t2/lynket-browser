package arun.com.chromer.shared;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by Arun on 04/01/2016.
 */
public class Constants {
    public static final int NO_COLOR = -1;

    // URL
    public static final String GOOGLE_URL = "https://www.google.com/";
    public static final String CUSTOM_TAB_URL = "https://developer.chrome.com/multidevice/android/customtabs#whentouse";
    public static final String APP_TESTING_URL = "https://play.google.com/apps/testing/arun.com.chromer";
    public static final String G_COMMUNITY_URL = "https://plus.google.com/communities/109754631011301174504";
    public static final String G_SEARCH_URL = "https://www.google.com/search?q=";

    //Objects
    public static final Intent WEB_INTENT = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_URL));
    public static final Intent TEXT_SHARE_INTENT = new Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "");
    public static final Intent DUMMY_INTENT = new Intent("Namey McNameFace");


    // Misc
    public static final String MAILID = "arunk.beece@gmail.com";
    public static final String ME = "Arunkumar";
    public static final String LOCATION = "Tamilnadu, India";
    public static final String CHROME_PACKAGE = "com.android.chrome";

    // Intent Actions
    public static final String ACTION_TOOLBAR_COLOR_SET = "ACTION_TOOLBAR_COLOR_SET";
    public static final String ACTION_WEBHEAD_COLOR_SET = "ACTION_WEBHEAD_COLOR_SET";
    public static final String ACTION_CLOSE_ROOT = "ACTION_CLOSE_ROOT";
    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_STOP_WEBHEAD_SERVICE = "close_service";
    public static final String ACTION_REBIND_WEBHEAD_TAB_CONNECTION = "rebind_event";
    public static final String ACTION_CLOSE_WEBHEAD_BY_URL = "ACTION_CLOSE_WEBHEAD_BY_URL";
    public static final String ACTION_MINIMIZE = "ACTION_MINIMIZE";
    public static final String ACTION_EVENT_WEBSITE_UPDATED = "ACTION_EVENT_WEBSITE_UPDATED";
    public static final String ACTION_EVENT_WEBHEAD_DELETED = "ACTION_EVENT_WEBHEAD_DELETED";
    // Extra keys
    public static final String EXTRA_KEY_SHOULD_REFRESH_BINDING = "EXTRA_KEY_SHOULD_REFRESH_BINDING";
    public static final String EXTRA_KEY_FROM_WEBHEAD = "EXTRA_KEY_FROM_WEBHEAD";
    public static final String EXTRA_KEY_TOOLBAR_COLOR = "EXTRA_KEY_TOOLBAR_COLOR";
    public static final String EXTRA_KEY_WEBHEAD_COLOR = "EXTRA_KEY_WEBHEAD_COLOR";
    public static final String EXTRA_KEY_CLEAR_LAST_TOP_APP = "EXTRA_KEY_CLEAR_LAST_TOP_APP";
    public static final String EXTRA_KEY_REBIND_WEBHEAD_CXN = "EXTRA_KEY_REBIND_WEBHEAD_CXN";
    public static final String EXTRA_KEY_FROM_NEW_TAB = "EXTRA_KEY_FROM_NEW_TAB";
    public static final String EXTRA_KEY_WEBSITE = "EXTRA_KEY_WEBSITE";
    public static final String EXTRA_KEY_MINIMIZE = "EXTRA_KEY_MINIMIZE";
    // Request codes
    public static final int REQUEST_CODE_VOICE = 10001;


    private Constants() {
        throw new UnsupportedOperationException("Cannot instantiate");
    }
}
