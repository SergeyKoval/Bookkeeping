package by.bk.bookkeeper.android.ui.association

import android.os.Parcel
import android.os.Parcelable

/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/

data class AccountInfoHolder(val accountName: String,
                             val subAccountName: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(accountName)
        parcel.writeString(subAccountName)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AccountInfoHolder> {
        override fun createFromParcel(parcel: Parcel): AccountInfoHolder = AccountInfoHolder(parcel)
        override fun newArray(size: Int): Array<AccountInfoHolder?> = arrayOfNulls(size)

    }
}