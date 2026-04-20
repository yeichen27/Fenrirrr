package dev.ragnarok.filegallery.media.music

import androidx.annotation.IntDef

@IntDef(
    PlayerStatus.SERVICE_KILLED,
    PlayerStatus.SHUFFLE_MODE_CHANGED,
    PlayerStatus.REPEAT_MODE_CHANGED,
    PlayerStatus.UPDATE_METADATA,
    PlayerStatus.UPDATE_TRACK_INFO,
    PlayerStatus.UPDATE_PLAY_PAUSE,
    PlayerStatus.UPDATE_PLAY_LIST
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class PlayerStatus {
    companion object {
        const val SERVICE_KILLED = 0
        const val SHUFFLE_MODE_CHANGED = 1
        const val REPEAT_MODE_CHANGED = 2
        const val UPDATE_METADATA = 3
        const val UPDATE_TRACK_INFO = 4
        const val UPDATE_PLAY_PAUSE = 5
        const val UPDATE_PLAY_LIST = 6
    }
}
