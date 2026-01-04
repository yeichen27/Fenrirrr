package dev.ragnarok.fenrir.api.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class HasNewStickersResponse {
    @SerialName("favorite_stickers_limit")
    var favoriteStickersLimit = 0

    @SerialName("favorite_stickers_version_hash")
    var favoriteStickersVersionHash: String? = null

    @SerialName("sticker_packs_chunk_size_limit")
    var stickerPacksChunkSizeLimit: Int = 0

    @SerialName("stickers_version_hash")
    var stickersVersionHash: String? = null
}