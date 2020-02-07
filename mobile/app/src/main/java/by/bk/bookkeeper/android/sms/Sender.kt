package by.bk.bookkeeper.android.sms

import android.os.Parcel
import android.os.Parcelable

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

data class Sender(val id: String,
                  val address: String = "",
                  val addressBookDisplayableName: String = "",
                  val snippet: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(address)
        parcel.writeString(addressBookDisplayableName)
        parcel.writeString(snippet)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Sender> {
        override fun createFromParcel(parcel: Parcel): Sender = Sender(parcel)
        override fun newArray(size: Int): Array<Sender?> = arrayOfNulls(size)

    }
}