package dev.ragnarok.filegallery.view.media

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.media3.common.Player
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.media.music.MusicPlaybackController

class RepeatButton(context: Context, attrs: AttributeSet?) : AppCompatImageButton(
    context, attrs
), View.OnClickListener {
    override fun onClick(v: View) {
        MusicPlaybackController.cycleRepeat()
        updateRepeatState()
    }

    fun updateRepeatState() {
        when (MusicPlaybackController.repeatMode) {
            Player.REPEAT_MODE_ALL -> setImageDrawable(
                AppCompatResources.getDrawable(
                    context, R.drawable.repeat
                )
            )

            Player.REPEAT_MODE_ONE -> setImageDrawable(
                AppCompatResources.getDrawable(
                    context, R.drawable.repeat_once
                )
            )

            Player.REPEAT_MODE_OFF -> setImageDrawable(
                AppCompatResources.getDrawable(
                    context, R.drawable.repeat_off
                )
            )

            else -> {}
        }
    }

    init {
        setOnClickListener(this)
    }
}