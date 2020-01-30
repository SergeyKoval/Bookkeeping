package by.bk.bookkeeper.android.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

abstract class BaseViewModel : ViewModel() {

    protected val subscriptions = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}