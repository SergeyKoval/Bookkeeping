package by.bk.bookkeeper.android.sms

import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Patterns
import androidx.annotation.WorkerThread
import by.bk.bookkeeper.android.BookkeeperApp
import io.reactivex.Observable
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
object SMSHandler {

    private val conversationUri: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
    private val recipientsUri: Uri = Uri.parse("content://mms-sms/canonical-addresses")

    private val conversationProjection = arrayOf(Telephony.Threads._ID, Telephony.Threads.RECIPIENT_IDS, Telephony.Threads.SNIPPET)
    private val smsMessageProjection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)

    fun allConversationsObservable(): Observable<List<Conversation>> = Observable.fromCallable {
        getAllConversations()
    }

    fun allSmsObservable(): Observable<Map<String, List<SMS>>> = Observable.fromCallable {
        getAllSms()
    }

    @WorkerThread
    fun getAllConversations(): List<Conversation> {
        Timber.d("Getting conversations in ${Thread.currentThread()}")
        val conversationsList: ArrayList<Conversation> = arrayListOf()
        getConversationsCursor()?.use { conversationCursor ->
            while (conversationCursor.moveToNext()) {
                var sender: Sender? = null
                val threadId = conversationCursor.getLong(conversationCursor.getColumnIndex(Telephony.Threads._ID))
                val snippet = conversationCursor.getString(conversationCursor.getColumnIndex(Telephony.Threads.SNIPPET))
                val recipientId: String = conversationCursor.getString(conversationCursor.getColumnIndex(Telephony.Threads.RECIPIENT_IDS))
                        .split(" ").first { it.isNotBlank() }
                getRecipientCursor(recipientId)?.use { recipientCursor ->
                    while (recipientCursor.moveToNext()) {
                        val address = recipientCursor.getString(recipientCursor.getColumnIndex(Telephony.Sms.ADDRESS))
                        val addressBookName = if (Patterns.PHONE.matcher(address).matches()) getContactName(address) else null
                        sender = Sender(id = recipientId, address = address,
                                addressBookDisplayableName = addressBookName ?: "", snippet = snippet ?: "")
                    }
                }
                conversationsList.add(Conversation(threadId, sender))
            }
        }
        return conversationsList
    }

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
            .query(Telephony.Sms.Inbox.CONTENT_URI, smsMessageProjection, null, null, null)

    private fun getConversationsCursor() = BookkeeperApp.getContext().contentResolver
            .query(conversationUri, conversationProjection, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)

    private fun getRecipientCursor(id: String) = BookkeeperApp.getContext().contentResolver
            .query(recipientsUri, null, "_id=?", arrayOf(id), null)

}