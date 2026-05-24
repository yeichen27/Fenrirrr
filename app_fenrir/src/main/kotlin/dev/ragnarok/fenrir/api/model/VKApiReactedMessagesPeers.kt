package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VKApiReactedMessagesPeers {
    @SerialName("reaction_id")
    var reactionId: Int = 0

    @SerialName("user_id")
    var userId: Long = 0L
}
