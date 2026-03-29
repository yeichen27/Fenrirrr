package dev.ragnarok.fenrir.picasso

import android.os.Build
import android.provider.MediaStore
import com.squareup.picasso3.Picasso
import com.squareup.picasso3.Request
import com.squareup.picasso3.RequestHandler
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.nonNullNoEmpty

class PicassoLocalRequestHandler : RequestHandler() {
    override fun canHandleRequest(data: Request): Boolean {
        return data.uri?.path.nonNullNoEmpty() && data.uri?.lastPathSegment.nonNullNoEmpty() && "content" == data.uri?.scheme
    }

    override fun load(picasso: Picasso, request: Request, callback: Callback) {
        val requestUri = checkNotNull(request.uri) { "request.uri == null" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val target = Stores.instance.localMedia().getThumbnail(requestUri, 256, 256)
            if (target == null) {
                callback.onError(Throwable("Picasso Thumb Not Support"))
            } else {
                callback.onSuccess(Result.Bitmap(target, Picasso.LoadedFrom.DISK))
            }
        } else {
            val contentId = requestUri.lastPathSegment?.toLong()
                ?: throw UnsupportedOperationException("request.uri.lastPathSegment == null")
            @Content_Local val ret: Int = when {
                requestUri.toString()
                    .contains(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString()) -> {
                    Content_Local.VIDEO
                }

                requestUri.toString()
                    .contains(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()) -> {
                    Content_Local.PHOTO
                }

                requestUri.toString()
                    .contains(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()) -> {
                    Content_Local.AUDIO
                }

                else -> {
                    callback.onError(Throwable("Picasso Thumb Not Support"))
                    return
                }
            }
            val target = Stores.instance.localMedia().getOldThumbnail(ret, contentId)
            if (target == null) {
                callback.onError(Throwable("Picasso Thumb Not Support"))
                return
            }
            callback.onSuccess(Result.Bitmap(target, Picasso.LoadedFrom.DISK))
        }
    }
}