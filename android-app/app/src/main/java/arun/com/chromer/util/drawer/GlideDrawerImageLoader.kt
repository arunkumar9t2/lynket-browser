package arun.com.chromer.util.drawer

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import arun.com.chromer.util.glide.GlideApp
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import javax.inject.Inject

class GlideDrawerImageLoader
@Inject
constructor() : AbstractDrawerImageLoader() {

    override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable) {
        GlideApp.with(imageView).load(uri).placeholder(placeholder).into(imageView)
    }

    override fun cancel(imageView: ImageView) {
        GlideApp.with(imageView).clear(imageView)
    }
}