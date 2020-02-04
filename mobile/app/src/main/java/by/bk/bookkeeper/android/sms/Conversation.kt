package by.bk.bookkeeper.android.sms

import android.os.Parcel
import android.os.Parcelable

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

data class Conversation(val threadId: Long = 0,
                        val sender: Sender
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readParcelable<Sender>(Sender::class.java.classLoader) as Sender
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(threadId)
        parcel.writeParcelable(sender, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation = Conversation(parcel)
        override fun newArray(size: Int): Array<Conversation?> = arrayOfNulls(size)

    }
}