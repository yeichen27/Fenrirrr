package dev.ragnarok.fenrir.api.services

import dev.ragnarok.fenrir.api.model.Dictionary
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiSticker
import dev.ragnarok.fenrir.api.model.VKApiStickerSet
import dev.ragnarok.fenrir.api.model.VKApiStickersKeywords
import dev.ragnarok.fenrir.api.model.response.BaseResponse
import dev.ragnarok.fenrir.api.model.response.HasNewStickersResponse
import dev.ragnarok.fenrir.api.rest.IServiceRest
import kotlinx.coroutines.flow.Flow

class IStoreService : IServiceRest() {
    fun getRecentStickers(): Flow<BaseResponse<Items<VKApiSticker>>> {
        return rest.request(
            "messages.getRecentStickers",
            null,
            items(VKApiSticker.serializer())
        )
    }

    fun getStickersSets(): Flow<BaseResponse<Items<VKApiStickerSet.Product>>> {
        return rest.request(
            "store.getProducts",
            form(
                "extended" to 1,
                "filters" to "active",
                "type" to "stickers"
            ),
            items(VKApiStickerSet.Product.serializer())
        )
    }

    fun getStickersKeywords(
        chunk: Int,
        chunksHash: String?
    ): Flow<BaseResponse<Dictionary<VKApiStickersKeywords>>> {
        return rest.request(
            "store.getStickersKeywords",
            form(
                "aliases" to 1,
                "all_products" to 1,
                "need_stickers" to 0,
                "chunk" to chunk,
                "chunks_hash" to chunksHash
            ),
            dictionary(VKApiStickersKeywords.serializer())
        )
    }

    fun hasNewStickers(): Flow<BaseResponse<HasNewStickersResponse>> {
        return rest.request(
            "store.hasNewItems",
            form(
                "type" to "stickers"
            ),
            base(HasNewStickersResponse.serializer())
        )
    }

    fun getStickers(stickerIds: String?): Flow<BaseResponse<List<VKApiSticker>>> {
        return rest.request(
            "store.getStickers",
            form(
                "sticker_ids" to stickerIds
            ),
            baseList(VKApiSticker.serializer())
        )
    }
}