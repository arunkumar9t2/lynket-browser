package arun.com.chromer.browsing.icons

import android.graphics.Bitmap
import arun.com.chromer.data.website.model.Website
import io.reactivex.Single

data class WebsiteIconData(
  val website: Website,
  val icon: Bitmap,
  val color: Int
)

interface WebsiteIconsProvider {

  fun getBubbleIconAndColor(website: Website): Single<WebsiteIconData>
}