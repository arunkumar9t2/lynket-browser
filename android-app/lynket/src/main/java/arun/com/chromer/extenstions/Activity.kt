package arun.com.chromer.extenstions

import android.app.Activity
import arun.com.chromer.util.Utils

fun Activity.finishAndRemoveTaskCompat() {
  if (Utils.isLollipopAbove()) {
    finishAndRemoveTask()
  } else {
    finish()
  }
}