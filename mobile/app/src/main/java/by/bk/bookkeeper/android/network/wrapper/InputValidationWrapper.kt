package by.bk.bookkeeper.android.network.wrapper

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/
sealed class InputValidationWrapper {
    object Valid : InputValidationWrapper()
    class Invalid(val validationError: InputValidationError) : InputValidationWrapper()
}