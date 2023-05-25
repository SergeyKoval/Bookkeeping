package by.bk.bookkeeper.android.ui.association

import by.bk.bookkeeper.android.ui.BaseViewModel
import by.bk.bookkeeper.android.util.UserInputValidator

/**
 *  Created by Evgenia Grinkevich on 19, May, 2023
 **/
class PushAssociationViewModel : BaseViewModel() {

    fun validatePackageInput(input: String?): Boolean = UserInputValidator.isValidPackageName(input)

}