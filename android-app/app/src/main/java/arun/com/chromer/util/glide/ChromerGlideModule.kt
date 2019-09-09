/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.util.glide

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.util.glide.appicon.ApplicationIcon
import arun.com.chromer.util.glide.appicon.ApplicationIconDecoder
import arun.com.chromer.util.glide.appicon.ApplicationIconModelLoader
import arun.com.chromer.util.glide.favicon.WebsiteDecoder
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888
import com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565
import com.bumptech.glide.load.model.UnitModelLoader
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class ChromerGlideModule : AppGlideModule() {

    override fun isManifestParsingEnabled() = false

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultTransitionOptions(Drawable::class.java, DrawableTransitionOptions.withCrossFade())

        val defaultOptions = RequestOptions()
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultOptions.format(if (activityManager.isLowRamDevice) PREFER_RGB_565 else PREFER_ARGB_8888)
        }
        defaultOptions.disallowHardwareConfig()
        builder.setDefaultRequestOptions(defaultOptions)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.prepend(Uri::class.java, ApplicationIcon::class.java, ApplicationIconModelLoader.Factory())
        registry.append(ApplicationIcon::class.java, Bitmap::class.java, ApplicationIconDecoder(context, glide))

        registry.append(Website::class.java, Website::class.java, UnitModelLoader.Factory.getInstance<Website>())
        registry.append(Website::class.java, Bitmap::class.java, WebsiteDecoder(context, glide))
    }
}
