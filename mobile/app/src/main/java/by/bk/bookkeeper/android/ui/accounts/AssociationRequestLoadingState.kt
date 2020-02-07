package by.bk.bookkeeper.android.ui.accounts

import by.bk.bookkeeper.android.network.wrapper.DataStatus

/**
 *  Created by Evgenia Grinkevich on 01, February, 2020
 **/
sealed class AssociationRequestLoadingState(val dataStatus: DataStatus) {
    class AddAssociation(dataState: DataStatus) : AssociationRequestLoadingState(dataState)
    class RemoveAssociation(dataState: DataStatus) : AssociationRequestLoadingState(dataState)
    class EditAssociation(dataState: DataStatus) : AssociationRequestLoadingState(dataState)
}