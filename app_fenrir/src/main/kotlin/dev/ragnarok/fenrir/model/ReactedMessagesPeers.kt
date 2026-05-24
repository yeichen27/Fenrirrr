package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper.Companion.readOwner
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper.Companion.writeOwner

class ReactedMessagesPeers : Parcelable {
    var reactionId = 0
        private set
    var ownerId = 0L
        private set
    var owner: Owner? = null
        private set

    constructor()
    internal constructor(parcel: Parcel) {
        reactionId = parcel.readInt()
        ownerId = parcel.readLong()
        owner = readOwner(parcel)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(reactionId)
        parcel.writeLong(ownerId)
        writeOwner(parcel, flags, owner)
    }

    fun setReactionId(reactionId: Int): ReactedMessagesPeers {
        this.reactionId = reactionId
        return this
    }

    fun setOwnerId(ownerId: Long): ReactedMessagesPeers {
        this.ownerId = ownerId
        return this
    }

    fun setOwner(owner: Owner?): ReactedMessagesPeers {
        this.owner = owner
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReactedMessagesPeers> {
        override fun createFromParcel(parcel: Parcel): ReactedMessagesPeers {
            return ReactedMessagesPeers(parcel)
        }

        override fun newArray(size: Int): Array<ReactedMessagesPeers?> {
            return arrayOfNulls(size)
        }
    }
}