package by.bk.bookkeeper.android.ui.association

import AssociationInteraction
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.sms.SMSHandler
import by.bk.bookkeeper.android.ui.BaseViewModel
import by.bk.bookkeeper.android.ui.accounts.AssociationRequestLoadingState
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AssociationViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
        AssociationInteraction.Inputs, AssociationInteraction.Outputs {

    private val conversations: BehaviorSubject<List<Conversation>> = BehaviorSubject.create()
    override fun conversations(): Observable<List<Conversation>> = conversations

    private val conversationsLoadingState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun conversationsLoadingState(): Observable<DataStatus> = conversationsLoadingState

    private val associationRequestState: PublishSubject<AssociationRequestLoadingState> = PublishSubject.create()
    override fun associationRequestState(): Observable<AssociationRequestLoadingState> = associationRequestState

    init {
        loadAllConversations()
    }

    private fun loadAllConversations() {
        subscriptions.add(
                SMSHandler.allConversationsObservable()
                        .subscribeOn(Schedulers.io())
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

    override fun addAssociation(associationRequest: AssociationRequest) {
        subscriptions.add(
                bkService.associateWithAccount(associationRequest)
                        .subscribeOn(Schedulers.io())
                        .subscribe({ response ->
                            associationRequestState.onNext(
                                    if (BaseResponse.STATUS_SUCCESS == response.status)
                                        AssociationRequestLoadingState.AddAssociation(DataStatus.Success)
                                    else AssociationRequestLoadingState.AddAssociation(DataStatus.Error(
                                            FailureWrapper.Generic(R.string.err_association_adding_failed, null, response.message)))
                            )
                        }, { error ->
                            Timber.e(error)
                            associationRequestState.onNext(
                                    AssociationRequestLoadingState.AddAssociation(DataStatus.Error(FailureWrapper.getFailureType(error)))
                            )
                        })
        )
    }

    override fun reloadConversations() = loadAllConversations()
}