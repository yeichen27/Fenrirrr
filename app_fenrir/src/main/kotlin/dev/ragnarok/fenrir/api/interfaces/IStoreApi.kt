package dev.ragnarok.fenrir.api.interfaces

import dev.ragnarok.fenrir.api.model.Dictionary
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiSticker
import dev.ragnarok.fenrir.api.model.VKApiStickerNotFull
import dev.ragnarok.fenrir.api.model.VKApiStickerSet
import dev.ragnarok.fenrir.api.model.VKApiStickersKeywords
import dev.ragnarok.fenrir.api.model.response.HasNewStickersResponse
import kotlinx.coroutines.flow.Flow

interface IStoreApi {
    fun getStickerKeywords(chunk: Int, chunksHash: String?): Flow<Dictionary<VKApiStickersKeywords>>
    val stickersSets: Flow<Items<VKApiStickerSet.Product>>
    val recentStickers: Flow<Items<VKApiSticker>>
    val hasNewStickers: Flow<HasNewStickersResponse>
    fun getStickers(stickers: List<VKApiStickerNotFull>): Flow<List<VKApiSticker>>
}