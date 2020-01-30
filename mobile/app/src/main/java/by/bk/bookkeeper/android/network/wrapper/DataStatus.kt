package by.bk.bookkeeper.android.network.wrapper

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/
sealed class DataStatus {
    object Loading : DataStatus()
    object Empty : DataStatus()
    object Success : DataStatus()
    class Error(val failure: FailureWrapper) : DataStatus()
}