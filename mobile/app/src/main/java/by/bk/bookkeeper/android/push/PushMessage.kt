package by.bk.bookkeeper.android.push

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

@Parcelize
data class PushMessage(val packageName: String, val text: String, val timestamp: Long) : Parcelable
