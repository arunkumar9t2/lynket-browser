package arun.com.chromer.extenstions

import android.net.Uri

/**
 * Created by arunk on 17-02-2018.
 */

fun String.toUri(): Uri = Uri.parse(this)