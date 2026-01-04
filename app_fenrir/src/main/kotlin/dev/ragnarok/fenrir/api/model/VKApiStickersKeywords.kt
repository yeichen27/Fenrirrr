package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKApiStickersKeywords {
    @SerialName("stickers")
    var stickers: List<VKApiStickerNotFull?>? = null

    @SerialName("words")
    var words: List<String?>? = null
}