package by.bk.bookkeeper.android.network.wrapper

import androidx.annotation.StringRes
import by.bk.bookkeeper.android.R

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

sealed class InputValidationError(@StringRes val messageStringRes: Int) {

    class EmptyEmail(messageStringRes: Int? = null) : InputValidationError(messageStringRes
            ?: R.string.err_empty_email)

    class InvalidEmail(messageStringRes: Int? = null) : InputValidationError(messageStringRes
            ?: R.string.err_invalid_email)

    class EmptyPassword(messageStringRes: Int? = null) : InputValidationError(messageStringRes
            ?: R.string.err_empty_password)
}
