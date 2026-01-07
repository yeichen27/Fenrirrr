package dev.ragnarok.fenrir.picasso

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import com.squareup.picasso3.Picasso
import com.squareup.picasso3.Request
import com.squareup.picasso3.RequestHandler
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.nonNullNoEmpty
import java.io.ByteArrayInputStream

class PicassoMediaMetadataHandler : RequestHandler() {
    override fun canHandleRequest(data: Request): Boolean {
        return data.uri?.path.nonNullNoEmpty() && data.uri?.lastPathSegment.nonNullNoEmpty() && data.uri?.scheme?.contains(
            "share_"
        ) == true
    }

    private fun getMetadataAudioThumbnail(uri: Uri): Bitmap? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(provideApplicationContext(), uri)
            val cover = mediaMetadataRetriever.embeddedPicture ?: return null
            BitmapFactory.decodeStream(ByteArrayInputStream(cover))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun load(picasso: Picasso, request: Request, callback: Callback) {
        val target =
            getMetadataAudioThumbnail(request.uri.toString().replace("share_", "").toUri())
        if (target == null) {
            callback.onError(Throwable("Picasso Thumb Not Support"))
            return
        }
        callback.onSuccess(Result.Bitmap(target, Picasso.LoadedFrom.DISK))
    }
}