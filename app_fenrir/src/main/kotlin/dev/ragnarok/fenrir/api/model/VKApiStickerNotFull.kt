package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKApiStickerNotFull {
    @SerialName("pack_id")
    var packId: Int = 0

    @SerialName("sticker_id")
    var stickerId: Int = 0
}
