package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Dictionary<I> {
    @SerialName("count")
    var count = 0

    @SerialName("chunks_count")
    var chunksCount = 0

    @SerialName("chunks_hash")
    var chunksHash: String? = null

    @SerialName("dictionary")
    var dictionary: ArrayList<I>? = null
}
