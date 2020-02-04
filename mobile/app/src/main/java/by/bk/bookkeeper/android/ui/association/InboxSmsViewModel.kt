package by.bk.bookkeeper.android.ui.association

import SMSProviderInteraction
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.sms.SMS
import by.bk.bookkeeper.android.sms.SMSHandler
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class InboxSmsViewModel : BaseViewModel(),
        SMSProviderInteraction.Inputs, SMSProviderInteraction.Outputs {

    private val conversations: BehaviorSubject<List<Conversation>> = BehaviorSubject.create()
    override fun conversations(): Observable<List<Conversation>> = conversations

    private val sms: BehaviorSubject<List<SMS>> = BehaviorSubject.create()
    override fun sms(): Observable<List<SMS>> = sms

    private val conversationsLoadingState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun conversationsLoadingState(): Observable<DataStatus> = conversationsLoadingState

    private val smsLoadingState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun smsLoadingState(): Observable<DataStatus> = smsLoadingState

    init {
        loadAllConversations()
    }

    private fun loadAllConversations() {
        subscriptions.add(
                SMSHandler.allConversationsObservable()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe {
                            conversationsLoadingState.onNext(DataStatus.Loading)
                        }
                        .subscribe({ conversationsList ->
                            conversations.onNext(conversationsList)
                            conversationsLoadingState.onNext(if (conversationsList.isNotEmpty()) DataStatus.Success else DataStatus.Empty)
                        }, { error ->
                            conversationsLoadingState.onNext(DataStatus.Error(
                                    FailureWrapper.Generic(R.string.err_unable_to_get_conversations, error, null))
                            )
                            Timber.e(error)
                        })

        )
    }

    fun loadInboxSMSByThreadId(threadId: Long) {
        subscriptions.add(
                SMSHandler.threadSmsObservable(threadId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe {
                            smsLoadingState.onNext(DataStatus.Loading)
                        }
                        .subscribe({ smsList ->
                            sms.onNext(smsList)
                            smsLoadingState.onNext(if (smsList.isNotEmpty()) DataStatus.Success else DataStatus.Empty)
                        }, { error ->
                            smsLoadingState.onNext(DataStatus.Error(
                                    FailureWrapper.Generic(R.string.err_unable_to_get_sms, error, null))
                            )
                            Timber.e(error)
                        })
        )
    }

    override fun reloadConversations() = loadAllConversations()
}