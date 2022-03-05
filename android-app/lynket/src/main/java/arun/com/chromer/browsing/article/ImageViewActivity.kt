/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.browsing.article

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arun.com.chromer.R
import arun.com.chromer.util.glide.GlideApp
import kotlinx.android.synthetic.main.activity_image_view.*

/**
 * Activity for viewing full size images from an article. Images are zoomable. You can pass in the
 * image url from an intent with .setData(url).
 */
class ImageViewActivity : AppCompatActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_image_view)
    val url = intent.dataString
    GlideApp.with(this)
      .load(url)
      .into(imageView)
  }
}
