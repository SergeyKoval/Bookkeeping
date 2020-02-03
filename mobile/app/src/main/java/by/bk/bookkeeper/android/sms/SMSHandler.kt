package by.bk.bookkeeper.android.sms

import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.annotation.WorkerThread
import by.bk.bookkeeper.android.BookkeeperApp
import io.reactivex.Observable
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
object SMSHandler {

    private val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)

    fun allSmsObservable(): Observable<Map<String, List<SMS>>> = Observable.fromCallable {
        getAllSms()
    }

    // TODO make separate queries
    @WorkerThread
    fun getAllSms(): Map<String, List<SMS>> {
        Timber.d("Getting sms list in ${Thread.currentThread()}")
        val smsList: ArrayList<SMS> = arrayListOf()
        getInboxSmsCursor()?.use { cursor ->
            while (cursor.moveToNext()) {
                val sender = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS))
                smsList.add(SMS(senderAddress = sender,
                        body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)),
                        dateReceived = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE)),
                        displayablePersonName = getContactName(sender)))
            }
        }
        return smsList.groupBy { it.senderAddress }
    }

    @WorkerThread
    private fun getContactName(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        BookkeeperApp.getContext().contentResolver
                .query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
                    }
                }
        return null
    }

    private fun getInboxSmsCursor(): Cursor? = BookkeeperApp.getContext().contentResolver
            .query(Telephony.Sms.Inbox.CONTENT_URI, projection, null, null, null)
}