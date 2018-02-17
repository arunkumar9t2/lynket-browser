package arun.com.chromer.data.apps.model

import android.net.Uri

/**
 * Created by arunk on 17-02-2018.
 */
data class Provider(
        var packageName: String,
        var appName: String,
        var iconUri: Uri,
        var installed: Boolean = false,
        var features: CharSequence = ""
) {
}
